const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
} = require('./test-utils');

/**
 * Pipeline-widget tests: verify that the per-instruction `CycleState` tag
 * (surfaced via `data-cycle-stage` on each pipeline stage `<g>`) is updated
 * for the cycle in which `cpu.step()` throws an exception.
 *
 * `Simulator.step()` runs `cycleBuilder.step()` from a `finally` block so the
 * builder always advances alongside the CPU, even when `cpu.step()` raises
 * `BreakException` (BREAK instruction) or `SynchronousException` (e.g.
 * integer overflow). Without that `finally` clause the builder would stay
 * one cycle behind whenever an exception fired mid-step, and the per-
 * instruction `Stage` field surfaced on the pipeline `<g>` would be empty
 * for the instruction that produced the exception (because the builder
 * would not have seen its serial number yet at the previous cycle).
 *
 * The assertion is therefore: after the exception is reported by the
 * simulator, every occupied pipeline stage carries a non-empty, valid
 * `data-cycle-stage` value drawn from the `CycleState` enum.
 */

const STORAGE_PREFIX = 'edumips64:v1:';

// Mirrors the `CycleState` enum in
// `src/main/java/org/edumips64/client/CycleState.java`.
const VALID_CYCLE_STATES = new Set([
  'IF', 'ID', 'EX', 'MEM', 'WB',
  'RAW', 'WAW',
  'StDiv', 'StEx', 'StFun', 'Str', 'StAdd', 'StMul',
  'A1', 'A2', 'A3', 'A4',
  'M1', 'M2', 'M3', 'M4', 'M5', 'M6', 'M7',
  'DIV', 'DIV_COUNT',
]);

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

async function readPipeline(page) {
  return await page.evaluate(() => {
    const out = {};
    document.querySelectorAll('#pipeline g[data-stage]').forEach((g) => {
      const stage = g.getAttribute('data-stage');
      out[stage] = {
        instruction: g.getAttribute('data-instruction') || '',
        stall: g.getAttribute('data-stall') || '',
        cycleStage: g.getAttribute('data-cycle-stage') || '',
      };
    });
    return out;
  });
}

/**
 * Returns the list of (stage label, cycleStage) pairs for *occupied*
 * pipeline boxes (i.e. boxes that are showing an instruction). An occupied
 * stage with no recorded `cycleStage` would be a hard signal that
 * `CycleBuilder` has not seen the instruction at the latest cycle — which
 * is exactly what the `finally` clause in `Simulator.step()` is meant to
 * prevent when an exception is thrown.
 */
function occupiedCycleStages(snap) {
  return Object.entries(snap)
    .filter(([, s]) => s.instruction)
    .map(([stage, s]) => ({ stage, cycleStage: s.cycleStage }));
}

/**
 * BREAK throws a `BreakException` from its `IF()` stage. This test runs a
 * program that ends in BREAK and asserts that, once BREAK is visible in
 * the pipeline, every occupied stage reports a non-empty, valid
 * `data-cycle-stage`. That confirms `cycleBuilder.step()` ran in the
 * `finally` block of `Simulator.step()` for the cycle in which the
 * BreakException was thrown — without it, BREAK's `data-cycle-stage` would
 * be empty (because the builder would never have seen its serial number).
 */
test('BreakException: cycleBuilder stays in sync (every occupied stage has a CycleState)', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await loadProgram(
    page,
    `.code
DADD  R1, R2, R3
DSUB  R4, R5, R6
BREAK
SYSCALL 0
`
  );

  await page.click('#run-button');

  // Wait until BREAK appears in some pipeline stage. The Web UI keeps the
  // pipeline state visible after the BreakException is reported (no reset
  // is issued in that path), so this snapshot is stable.
  await expect
    .poll(
      async () => {
        const snap = await readPipeline(page);
        return Object.values(snap).some((s) => s.instruction === 'BREAK');
      },
      { timeout: 15000 }
    )
    .toBe(true);

  const snap = await readPipeline(page);
  const occupied = occupiedCycleStages(snap);
  expect(occupied.length, `pipeline was empty: ${JSON.stringify(snap)}`).toBeGreaterThan(0);
  for (const { stage, cycleStage } of occupied) {
    expect(cycleStage, `occupied stage ${stage} has no CycleState (cycleBuilder lagged the CPU)`).not.toBe('');
    expect(VALID_CYCLE_STATES.has(cycleStage), `unknown CycleState "${cycleStage}" at ${stage}`).toBe(true);
  }
  // BREAK in particular must have a CycleState recorded for the cycle the
  // BreakException was thrown — that is the assertion that goes red if
  // `cycleBuilder.step()` is moved out of the `finally` clause.
  const breakBox = Object.values(snap).find((s) => s.instruction === 'BREAK');
  expect(breakBox.cycleStage, 'BREAK has no CycleState (finally clause likely missing)').not.toBe('');
});

/**
 * Integer overflow on `ADD` raises a `SynchronousException` from `EX`. The
 * simulator surfaces this as an `alert()`; we capture the pipeline state
 * *while the alert is still pending* (before `stopCode()` resets the worker
 * and clears the pipeline view), then dismiss the dialog. Every occupied
 * stage must carry a non-empty, valid `data-cycle-stage`.
 */
test('SynchronousException (INTOVERFLOW): cycleBuilder stays in sync', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await loadProgram(
    page,
    `.data
big:  .word 0x7FFFFFFF
one:  .word 0x00000001
.code
LW    R8, big(R0)
LW    R9, one(R0)
ADD   R10, R8, R9
SYSCALL 0
`
  );

  // Capture the pipeline state at the very moment the simulator surfaces
  // its synchronous-exception alert, before `stopCode()` resets the worker
  // and clears the pipeline view. We do this by monkey-patching
  // `window.alert` *in the page* so the snapshot is taken synchronously
  // from the same closure that fires the alert.
  await page.evaluate(() => {
    window.__alertCount = 0;
    window.__pipelineSnap = null;
    const origAlert = window.alert;
    window.alert = function (msg) {
      window.__alertCount += 1;
      try {
        const out = {};
        document.querySelectorAll('#pipeline g[data-stage]').forEach((g) => {
          const stage = g.getAttribute('data-stage');
          out[stage] = {
            instruction: g.getAttribute('data-instruction') || '',
            stall: g.getAttribute('data-stall') || '',
            cycleStage: g.getAttribute('data-cycle-stage') || '',
          };
        });
        window.__pipelineSnap = out;
      } catch (_) {
        /* ignore */
      }
      // Suppress the actual alert (it would block subsequent page.evaluate
      // calls); the test no longer needs the dialog to fire.
      return origAlert ? undefined : undefined;
    };
  });

  await page.click('#run-button');
  await expect
    .poll(async () => await page.evaluate(() => window.__alertCount), {
      timeout: 15000,
    })
    .toBeGreaterThan(0);

  const snapAtAlert = await page.evaluate(() => window.__pipelineSnap);

  expect(snapAtAlert, 'no pipeline snapshot was captured at alert time').toBeTruthy();
  const occupied = occupiedCycleStages(snapAtAlert);
  expect(occupied.length, `pipeline was empty: ${JSON.stringify(snapAtAlert)}`).toBeGreaterThan(0);
  for (const { stage, cycleStage } of occupied) {
    expect(cycleStage, `occupied stage ${stage} has no CycleState after overflow`).not.toBe('');
    expect(VALID_CYCLE_STATES.has(cycleStage), `unknown CycleState "${cycleStage}" at ${stage}`).toBe(true);
  }
  // ADD is the trapping instruction; if it is still observable in the
  // pipeline at the moment the alert fired, its `cycleStage` must be set —
  // that is the assertion that goes red if `cycleBuilder.step()` is moved
  // out of the `finally` clause. (The alert is fired from the result-
  // handling closure; depending on where in the run loop the result is
  // delivered, ADD may already have moved past WB by the time the alert
  // is rendered, so we only assert when ADD is actually visible.)
  const addBox = Object.values(snapAtAlert).find((s) => s.instruction === 'ADD');
  if (addBox) {
    expect(addBox.cycleStage, 'ADD has no CycleState (finally clause likely missing)').not.toBe('');
  }
});
