const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  waitForSimulationComplete,
} = require('./test-utils');

/**
 * Regression test for the "default sample produces no Standard Output" bug:
 * loading the default sample program preloaded in the editor, clicking Load
 * and then Run All, must result in the printf'd string being visible in the
 * Standard Output panel after the simulation completes.
 *
 * The default sample uses SYSCALL 5 (printf) to write a string containing
 * the substring "being tested!" and then SYSCALL 0 to halt.
 */
test('default sample program writes to standard output', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // The default sample is preloaded — no edits needed.
  await removeOverlay(page);

  // Load the program into the simulator.
  await page.waitForSelector('#load-button:not([disabled])', { timeout: 10000 });
  await page.click('#load-button');
  await page.mouse.move(0, 0);

  // After load, the simulator enters READY: #run-button becomes enabled.
  await page.waitForSelector('#run-button:not([disabled])', { timeout: 10000 });

  // Run the whole program to completion.
  await page.click('#run-button');
  await waitForSimulationComplete(page);

  // Open the Standard Output accordion so the textarea becomes interactive.
  await page.locator('text=Standard Output').click();

  // The default sample's printf output contains "being tested!".
  await expect(page.locator('#stdout-view')).toContainText('being tested!');
});
