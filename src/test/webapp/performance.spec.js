const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  waitForRunningState,
  loadProgram,
} = require('./test-utils');

test('default sample program completes within a reasonable time', async ({
  page,
}) => {
  // The default sample program runs for ~5020 cycles. The web worker should
  // finish well under 60 seconds even on slow CI runners under load.
  test.setTimeout(90000);

  await page.goto(targetUri);
  await waitForPageReady(page);

  // The sample program is already in the editor on load; just click Load.
  await removeOverlay(page);
  await page.click('#load-button');
  await waitForRunningState(page);

  const startTime = Date.now();

  await removeOverlay(page);
  await page.click('#run-button');

  // Wait for completion with a generous timeout for CI environments
  await expect(page.locator('#stat-cycles')).toHaveText(/^[1-9][0-9]*$/, { timeout: 60000 });
  await page.waitForSelector('#clear-code-button:not([disabled])', { timeout: 60000 });

  const elapsed = Date.now() - startTime;

  const cycles = parseInt(
    (await page.locator('#stat-cycles').textContent()) || '0',
    10
  );

  // The sample program should run for approximately 5020 cycles
  expect(cycles).toBeGreaterThanOrEqual(5000);

  // It should complete in under 60 seconds (very generous budget for CI)
  expect(elapsed).toBeLessThan(60000);

  // Log the performance measurement for informational purposes
  console.log(
    `Sample program: ${cycles} cycles completed in ${elapsed}ms (${(cycles / elapsed * 1000).toFixed(0)} cycles/s)`
  );

  await page.close();
});

test('loop program completes within a time budget', async ({ page }) => {
  test.setTimeout(60000);

  await page.goto(targetUri);
  await waitForPageReady(page);

  // A tight loop that iterates 500 times — enough to exercise the worker
  // without taking too long.
  const loopProgram = `.code
DADDI r1, r0, 500
loop:
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;

  await loadProgram(page, loopProgram);

  const startTime = Date.now();

  await removeOverlay(page);
  await page.click('#run-button');

  // Wait for completion with a generous timeout for CI environments
  await expect(page.locator('#stat-cycles')).toHaveText(/^[1-9][0-9]*$/, { timeout: 45000 });
  await page.waitForSelector('#clear-code-button:not([disabled])', { timeout: 45000 });

  const elapsed = Date.now() - startTime;

  const cycles = parseInt(
    (await page.locator('#stat-cycles').textContent()) || '0',
    10
  );
  const instructions = parseInt(
    (await page.locator('#stat-instructions').textContent()) || '0',
    10
  );

  // Sanity checks: the loop should have executed many instructions
  expect(cycles).toBeGreaterThan(100);
  expect(instructions).toBeGreaterThan(100);

  // Should complete in under 45 seconds (very generous for CI under load)
  expect(elapsed).toBeLessThan(45000);

  console.log(
    `Loop program: ${cycles} cycles, ${instructions} instructions in ${elapsed}ms`
  );

  await page.close();
});
