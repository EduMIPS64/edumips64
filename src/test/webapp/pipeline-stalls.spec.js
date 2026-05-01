const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
} = require('./test-utils');

/**
 * Pipeline-widget unit tests: verify that each stall type produced by the
 * simulator (RAW, WAW, structural Divider) shows up in the right pipeline
 * stage, with the right instruction mnemonic, the right stall label, and
 * the dedicated *Stall* fill colour from `DEFAULT_PIPELINE_COLORS`.
 *
 * The data extracted from the SVG goes through `data-stage` / `data-instruction`
 * / `data-stall` / `data-fill` attributes added to each `<g>` rendered by
 * `Pipeline.js` (see `StageBox`); reading those is a lot more robust than
 * parsing rendered text positions.
 */

const STORAGE_PREFIX = 'edumips64:v1:';

// Mirrors `DEFAULT_PIPELINE_COLORS.Stall` in `src/webapp/settings/schema.js`.
const STALL_COLOR = '#9e9e9e';
// Mirrors the per-stage colours used by `Pipeline.js` when no stall is
// present. These are the integer-stage entries from `DEFAULT_PIPELINE_COLORS`.
const STAGE_COLOR = {
  IF: '#ebeb3b',
  ID: '#2196f3',
  EX: '#f44336',
  MEM: '#4caf50',
  WB: '#ab47bc',
};

async function clearSettings(page) {
  await page.evaluate((prefix) => {
    const keysToRemove = [];
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k && k.startsWith(prefix)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => window.localStorage.removeItem(k));
  }, STORAGE_PREFIX);
}

test.beforeEach(async ({ page }) => {
  await page.goto(targetUri);
  await clearSettings(page);
});

test.afterEach(async ({ page }) => {
  await clearSettings(page);
});

/**
 * Ensure the General Settings accordion is expanded and toggle the
 * "CPU Forwarding" switch to the desired state. Mirrors the helpers in
 * `forwarding.spec.js` so this spec stays self-contained.
 */
async function setForwarding(page, enabled) {
  const summary = page.getByRole('button', { name: /General Settings/ });
  await summary.waitFor({ state: 'visible' });
  if ((await summary.getAttribute('aria-expanded')) !== 'true') {
    await summary.click();
  }
  await expect(summary).toHaveAttribute('aria-expanded', 'true');
  const forwardingSwitch = page.getByLabel('CPU Forwarding');
  await forwardingSwitch.waitFor({ state: 'visible' });
  if ((await forwardingSwitch.isChecked()) !== enabled) {
    await forwardingSwitch.click();
  }
  await expect(forwardingSwitch).toBeChecked({ checked: enabled });
}

/**
 * Click "Single Step" once and wait for the Step button to be re-enabled
 * (i.e. the worker has finished processing the cycle and React has rendered
 * the resulting pipeline state).
 */
async function singleStep(page) {
  await page.waitForSelector('#step-button:not([disabled])');
  await page.click('#step-button');
  // The button is briefly disabled while the cycle runs; wait for it to
  // come back so the next read sees the post-step pipeline.
  await page.waitForSelector('#step-button:not([disabled])');
}

/**
 * Read the pipeline widget's per-stage state out of the SVG `data-*`
 * attributes. Returns a map keyed by stage label
 * (`IF`, `ID`, `EX`, `MEM`, `WB`, `M1`..`M7`, `A1`..`A4`, `FP Divider`).
 */
async function readPipeline(page) {
  return await page.evaluate(() => {
    const out = {};
    document
      .querySelectorAll('#pipeline g[data-stage]')
      .forEach((g) => {
        const stage = g.getAttribute('data-stage');
        out[stage] = {
          instruction: g.getAttribute('data-instruction') || '',
          stall: g.getAttribute('data-stall') || '',
          fill: (g.getAttribute('data-fill') || '').toLowerCase(),
        };
      });
    return out;
  });
}

/**
 * Step the simulator until the predicate returns true, or `maxSteps` cycles
 * have elapsed. Returns the pipeline snapshot at the matching cycle.
 */
async function stepUntil(page, predicate, maxSteps = 30) {
  for (let i = 0; i < maxSteps; i++) {
    await singleStep(page);
    const pipeline = await readPipeline(page);
    if (predicate(pipeline)) {
      return pipeline;
    }
  }
  throw new Error(
    `Predicate not satisfied within ${maxSteps} cycles. ` +
      `Last pipeline: ${JSON.stringify(await readPipeline(page))}`
  );
}

/**
 * Read a numeric statistic out of the Statistics panel. Used as a
 * cross-check that the pipeline widget's stall classification agrees with
 * the simulator's stall counters (see also `forwarding.spec.js`).
 */
async function readStat(page, id) {
  const txt = await page.locator(`#${id}`).textContent();
  return parseInt(txt || '0', 10);
}

/**
 * RAW stall — Read-After-Write hazard.
 *
 * With forwarding disabled, `DSUB R4, R1, R5` cannot read R1 until the
 * preceding `DADD R1, R2, R3` has finished WB, so it stalls in ID for two
 * cycles. We step the simulator until the RAW tag appears on ID and then
 * verify the rendered widget:
 *   - the stalled instruction is `DSUB`, in `ID`;
 *   - the stall tag is `RAW`;
 *   - the stage is filled with the dedicated `Stall` colour;
 *   - the simulator's RAW-stall counter agrees.
 */
test('RAW hazard: DSUB stalls in ID with the RAW tag (forwarding off)', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);
  await setForwarding(page, false);

  await loadProgram(
    page,
    `.code
DADD  R1, R2, R3
DSUB  R4, R1, R5
SYSCALL 0
`
  );

  const snap = await stepUntil(
    page,
    (p) => p.ID && p.ID.stall === 'RAW' && p.ID.instruction === 'DSUB'
  );

  expect(snap.ID.instruction).toBe('DSUB');
  expect(snap.ID.stall).toBe('RAW');
  expect(snap.ID.fill).toBe(STALL_COLOR);

  // Sanity check: a non-stalled stage in the same snapshot keeps its
  // per-stage colour (or the empty `white` fill for idle stages), never
  // the Stall colour.
  Object.entries(snap).forEach(([stage, s]) => {
    if (s.stall) return;
    expect(s.fill).not.toBe(STALL_COLOR);
    if (s.instruction && STAGE_COLOR[stage]) {
      expect(s.fill).toBe(STAGE_COLOR[stage]);
    }
  });

  // The simulator's RAW counter must have been incremented at least once.
  expect(await readStat(page, 'stat-raw-stalls')).toBeGreaterThanOrEqual(1);
});

/**
 * WAW stall — Write-After-Write hazard.
 *
 * `MUL.D F0, F2, F4` enters the 7-stage FP Multiplier; the immediately
 * following `ADD.D F0, F6, F8` enters the 4-stage FP Adder and, since both
 * write to F0, the adder's instruction would reach WB before the
 * multiplier's. The simulator detects this at ID and inserts WAW stalls
 * until the order is safe.
 */
test('WAW hazard: ADD.D stalls in ID with the WAW tag', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);
  // Forwarding does not affect WAW classification, but we set it
  // explicitly so the test is hermetic w.r.t. the persisted setting.
  await setForwarding(page, true);

  await loadProgram(
    page,
    `.data
x:    .double 1.0
y:    .double 2.0
z:    .double 3.0
.code
L.D   F2, x(R0)
L.D   F4, y(R0)
L.D   F6, z(R0)
L.D   F8, x(R0)
MUL.D F0, F2, F4
ADD.D F0, F6, F8
SYSCALL 0
`
  );

  const snap = await stepUntil(
    page,
    (p) => p.ID && p.ID.stall === 'WAW' && p.ID.instruction === 'ADD.D',
    50
  );

  expect(snap.ID.instruction).toBe('ADD.D');
  expect(snap.ID.stall).toBe('WAW');
  expect(snap.ID.fill).toBe(STALL_COLOR);

  expect(await readStat(page, 'stat-waw-stalls')).toBeGreaterThanOrEqual(1);
});

/**
 * Structural-Divider stall.
 *
 * The FP Divider is a single non-pipelined unit. Issuing two `DIV.D`
 * back-to-back means the second one cannot enter the divider while the
 * first is still busy, and the simulator tags it `StDiv` in ID.
 */
test('Structural Divider hazard: second DIV.D stalls in ID with the Struct: Div tag', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);
  await setForwarding(page, true);

  await loadProgram(
    page,
    `.data
x:    .double 6.0
y:    .double 2.0
.code
L.D   F2, x(R0)
L.D   F4, y(R0)
DIV.D F0, F2, F4
DIV.D F6, F2, F4
SYSCALL 0
`
  );

  const snap = await stepUntil(
    page,
    (p) => p.ID && p.ID.stall === 'Struct: Div' && p.ID.instruction === 'DIV.D',
    60
  );

  expect(snap.ID.instruction).toBe('DIV.D');
  expect(snap.ID.stall).toBe('Struct: Div');
  expect(snap.ID.fill).toBe(STALL_COLOR);

  // Note: the Statistics panel's "Structural Stall" counter only counts
  // memory-structural stalls (`getStructuralStallsMemory()`), not divider
  // stalls, so we don't assert anything about it here. The SVG check above
  // is the authoritative verification that the Web UI surfaced the StDiv
  // tag for the second DIV.D.
});
