const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

/**
 * Happy-path test for the AppLoader loading gate.
 *
 * The loading gate (#app-loading) is a transient spinner that is shown while
 * the Web Worker initialises.  Once the worker sends its first message the
 * spinner is replaced by the Simulator component.
 *
 * We deliberately do NOT assert that the spinner is visible during loading
 * because it may disappear before Playwright has a chance to observe it on
 * fast machines.  Instead we verify:
 *   1. The page reaches the fully-ready state (waitForPageReady checks for
 *      #load-button, .monaco-editor, and window.monaco).
 *   2. The error panel (#app-load-error) was never shown.
 *   3. The loading spinner (#app-loading) is no longer in the DOM once ready.
 */
test('app loading gate resolves and simulator becomes ready', async ({
  page,
}) => {
  await page.goto(targetUri);

  // The app must reach the ready state (simulator fully loaded).
  await waitForPageReady(page);

  // Confirm the error panel was never shown.
  await expect(page.locator('#app-load-error')).not.toBeVisible();

  // Confirm the loading spinner is gone once the simulator is ready.
  await expect(page.locator('#app-loading')).not.toBeVisible();
});
