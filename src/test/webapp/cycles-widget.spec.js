const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
} = require('./test-utils');

/**
 * Cycles-widget tests (roadmap item U.7): verify that the temporal
 * instruction-vs-cycle diagram fills up as the program executes, one row
 * per fetched instruction and one colored cell per cycle, including the
 * RAW-stall tagging that the worker-side CycleBuilder computes.
 *
 * The widget exposes its data through `data-*` attributes (`data-time` on
 * the root, `data-name` on each row, `data-cycle`/`data-state` on each
 * cell), mirroring the approach used by the Pipeline widget tests.
 */

// Mirrors `DEFAULT_PIPELINE_COLORS` in `src/webapp/settings/schema.ts`.
const IF_COLOR = 'rgb(235, 235, 59)';
const STALL_COLOR = 'rgb(158, 158, 158)';

// R3 depends on R2, which is produced by the immediately preceding DADDI:
// with forwarding disabled (the default) the DADD is RAW-stalled in ID.
const RAW_PROGRAM = `.code
DADDI R1, R0, 1
DADDI R2, R0, 2
DADD R3, R1, R2
SYSCALL 0
`;

test.beforeEach(async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);
});

test('cycles widget renders an empty diagram before any program is loaded', async ({
  page,
}) => {
  // On a fresh page the CPU is in READY with zero cycles executed. The widget
  // is always present in the bottom region, showing an empty grid (no rows)
  // rather than a placeholder message.
  await expect(page.getByTestId('cycles-widget')).toBeVisible();
  await expect(page.getByTestId('cycles-widget')).toHaveAttribute(
    'data-time',
    '0',
  );
  await expect(page.getByTestId('cycles-row')).toHaveCount(0);
});

test('stepping fills the temporal diagram row by row', async ({ page }) => {
  await loadProgram(page, RAW_PROGRAM);

  // Loading already fetched the first instruction (cycle 1); four more
  // single steps bring us to cycle 5, by which point the first DADDI has
  // traversed the whole integer pipeline (IF ID EX MEM WB).
  for (let i = 0; i < 4; i++) {
    await page.click('#step-button');
  }

  const widget = page.getByTestId('cycles-widget');
  await expect(widget).toHaveAttribute('data-time', '5');

  // Rows appear in fetch order.
  const rows = page.getByTestId('cycles-row');
  const firstRow = rows.first();
  await expect(firstRow).toHaveAttribute('data-name', /DADDI R1/);
  await expect(rows.nth(1)).toHaveAttribute('data-name', /DADDI R2/);
  await expect(rows.nth(2)).toHaveAttribute('data-name', /DADD R3/);

  // The first instruction's cells sit at cycles 1..5 with the canonical
  // stage progression.
  const expectedStages = ['IF', 'ID', 'EX', 'MEM', 'WB'];
  for (let cycle = 1; cycle <= 5; cycle++) {
    await expect(firstRow.locator(`[data-cycle="${cycle}"]`)).toHaveAttribute(
      'data-state',
      expectedStages[cycle - 1],
    );
  }

  // Cell colors come from the shared pipeline palette.
  await expect(firstRow.locator('[data-cycle="1"]')).toHaveCSS(
    'background-color',
    IF_COLOR,
  );
});

test('a RAW hazard is tagged and painted with the stall color', async ({
  page,
}) => {
  await loadProgram(page, RAW_PROGRAM);

  // Run enough cycles for the RAW stall on the DADD to materialize (it
  // waits in ID until the second DADDI writes R2 back).
  for (let i = 0; i < 8; i++) {
    await page.click('#step-button');
  }

  const dadd = page.locator('[data-testid="cycles-row"][data-name*="DADD R3"]');
  const rawCells = dadd.locator('[data-state="RAW"]');
  await expect(rawCells.first()).toBeVisible();
  await expect(rawCells.first()).toHaveCSS('background-color', STALL_COLOR);
});

test('the diagram survives a run to completion and resets on reload', async ({
  page,
}) => {
  await loadProgram(page, RAW_PROGRAM);

  await page.click('#run-button');
  // When execution finishes, the widget must report the final cycle count
  // (matching the Stats panel) and still contain one row per instruction
  // fetch. The exact count depends on stall timing, so just require > 5.
  //
  // The widget's `data-time` attribute and the Stats panel's `#stat-cycles`
  // text are updated by separate re-renders, so reading them as two
  // one-shot snapshots races: one can be captured mid-run (e.g. "1") while
  // the other has already reached the final count (e.g. "10"). Re-read both
  // together and retry until they agree, so the comparison only happens once
  // the run has well and truly settled.
  const widget = page.getByTestId('cycles-widget');
  const stats = page.locator('#stat-cycles');
  let statCycles;
  await expect(async () => {
    const widgetTime = await widget.getAttribute('data-time');
    statCycles = (await stats.textContent()).trim();
    expect(widgetTime).toMatch(/^\d+$/);
    expect(widgetTime).toBe(statCycles);
  }).toPass({ timeout: 30000 });

  // Re-loading the program resets the CycleBuilder: the diagram restarts
  // from the first fetch (cycle 1, one row) rather than keeping the history
  // of the previous run.
  await loadProgram(page, RAW_PROGRAM);
  await expect(page.getByTestId('cycles-widget')).toHaveAttribute(
    'data-time',
    '1',
  );
  await expect(page.getByTestId('cycles-row')).toHaveCount(1);
});
