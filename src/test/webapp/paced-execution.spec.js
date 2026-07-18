const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, loadProgram } = require('./test-utils');

// Regression test for the paced-execution bug reported by Davide Patti:
// with a non-zero execution delay, the UI showed a single update per
// 50-cycle internal batch instead of one update per cycle, so the pipeline
// colors in the editor never appeared to change.  The fix shrinks the
// worker batch size to 1 whenever the execution delay is non-zero, matching
// the Swing UI (CPUSwingWorker), which steps and repaints one cycle at a
// time in verbose mode.

// Long enough that a multi-step run crosses several cycles, short enough to
// keep the test fast.
const PROGRAM = `.code
daddi r10, r0, 30
loop:
daddi r10, r10, -1
bne r10, r0, loop
syscall 0
`;

// Poll #stat-cycles and collect the distinct values observed while the
// simulator executes.  With per-cycle batches each cycle produces its own
// UI update; with the old batched behavior only the final value would be
// visible.
async function collectCycleReadings(page, durationMs, everyMs) {
  const readings = new Set();
  const t0 = Date.now();
  while (Date.now() - t0 < durationMs) {
    const text = await page.locator('#stat-cycles').textContent();
    if (text && /^\d+$/.test(text)) {
      readings.add(text);
    }
    await page.waitForTimeout(everyMs);
  }
  return readings;
}

test('multi-step with execution delay updates the UI once per cycle', async ({
  page,
}) => {
  // Seed settings before the app boots: multi-step size 4, 300 ms delay.
  await page.addInitScript(() => {
    localStorage.setItem('edumips64:v1:stepStride', '4');
    localStorage.setItem('edumips64:v1:executionDelayMs', '300');
  });

  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, PROGRAM);

  await page.click('#multi-step-button');

  // 4 steps paced at 300 ms each finish in ~0.9 s (no delay before the first
  // batch); sample well past that.  Each paced cycle is visible for ~300 ms,
  // so 50 ms polling cannot miss it.  Expect at least 3 distinct readings;
  // the old batched behavior showed exactly 1 (the final state).
  const readings = await collectCycleReadings(page, 2000, 50);
  expect(readings.size).toBeGreaterThanOrEqual(3);
});

test('run-all with execution delay updates the UI once per cycle', async ({
  page,
}) => {
  await page.addInitScript(() => {
    localStorage.setItem('edumips64:v1:executionDelayMs', '100');
  });

  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, PROGRAM);

  await page.click('#run-button');

  // With per-cycle pacing at 100 ms we observe many consecutive cycle
  // values; the old behavior jumped 50 cycles per update, so within 2 s at
  // most a couple of distinct values were visible.  Require a healthy
  // margin above that.
  const readings = await collectCycleReadings(page, 2000, 30);
  expect(readings.size).toBeGreaterThanOrEqual(5);

  // Stop the paced run so the page teardown doesn't race a long execution.
  await page.click('#pause-button');
});
