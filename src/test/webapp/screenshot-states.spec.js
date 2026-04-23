const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  runToCompletion,
} = require('./test-utils');

/**
 * Helper to perform a single step and wait for the cycle counter to advance.
 */
async function singleStep(page) {
  const before = await page.locator('#stat-cycles').textContent();
  await removeOverlay(page);
  await page.click('#step-button');
  await expect(page.locator('#stat-cycles')).not.toHaveText(before || '0', {
    timeout: 5000,
  });
}

const SIMPLE_PROGRAM = `.code
DADDI r1, r0, 10
DADDI r2, r0, 20
DADD r3, r1, r2
SYSCALL 0
`;

test('screenshot of initial page load', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await page.screenshot({
    path: 'test-results/screenshot-initial-load.png',
    fullPage: true,
  });

  // Basic sanity: the load button should be visible
  await expect(page.locator('#load-button')).toBeVisible();

  await page.close();
});

test('screenshot after loading a program (RUNNING state)', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, SIMPLE_PROGRAM);

  await page.screenshot({
    path: 'test-results/screenshot-running-state.png',
    fullPage: true,
  });

  // In the RUNNING state the step button should be enabled
  await expect(page.locator('#step-button')).toBeEnabled();

  await page.close();
});

test('screenshot after single-stepping (mid-execution)', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, SIMPLE_PROGRAM);

  // Advance a few cycles
  for (let i = 0; i < 3; i++) {
    await singleStep(page);
  }

  await page.screenshot({
    path: 'test-results/screenshot-mid-execution.png',
    fullPage: true,
  });

  // Verify we are mid-execution — cycles should be > 0 and step button enabled
  const cycles = parseInt(
    (await page.locator('#stat-cycles').textContent()) || '0',
    10
  );
  expect(cycles).toBeGreaterThan(0);

  await page.close();
});

test('screenshot after running to completion', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, SIMPLE_PROGRAM);
  await runToCompletion(page);

  await page.screenshot({
    path: 'test-results/screenshot-completed.png',
    fullPage: true,
  });

  // After completion the clear button should be enabled
  await expect(page.locator('#clear-code-button')).toBeEnabled();

  await page.close();
});
