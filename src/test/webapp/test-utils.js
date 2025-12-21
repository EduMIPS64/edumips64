const { expect } = require('@playwright/test');

const targetUri = process.env.PLAYWRIGHT_TARGET_URL || 'http://localhost:8080';

/**
 * Helper function to remove webpack dev server overlay if present.
 * The overlay can intercept pointer events and cause test failures.
 * @param {import('@playwright/test').Page} page - Playwright page object
 */
async function removeOverlay(page) {
  await page.evaluate(() => {
    const overlay = document.getElementById(
      'webpack-dev-server-client-overlay'
    );
    if (overlay) {
      overlay.remove();
    }
  });
}

/**
 * Helper function to wait for the page to be ready
 * @param {import('@playwright/test').Page} page - Playwright page object
 */
async function waitForPageReady(page) {
  await page.waitForSelector('#load-button');
  await page.waitForSelector('.monaco-editor');

  // Close the webpack dev server overlay if present
  await removeOverlay(page);
}

/**
 * Helper function to wait for simulator to enter RUNNING state
 * @param {import('@playwright/test').Page} page - Playwright page object
 */
async function waitForRunningState(page) {
  // Wait for the Single Step button to become enabled (indicates RUNNING state)
  await page.waitForSelector('#step-button:not([disabled])', {
    timeout: 10000,
  });
}

/**
 * Helper function to wait for simulator to finish execution
 * @param {import('@playwright/test').Page} page - Playwright page object
 */
async function waitForSimulationComplete(page) {
  // Wait for the simulation to have run at least one cycle
  // This ensures we don't check for completion before the simulation has actually started
  // We use a regex to ensure it's a positive number (not 0, not empty)
  await expect(page.locator('#stat-cycles')).toHaveText(/^[1-9][0-9]*$/, { timeout: 30000 });

  // Wait for the Clear Code button to become enabled (indicates simulation ended)
  await page.waitForSelector('#clear-code-button:not([disabled])', {
    timeout: 30000,
  });
}

/**
 * Helper function to load a MIPS program into the editor
 * @param {import('@playwright/test').Page} page - Playwright page object
 * @param {string} program - MIPS assembly code
 */
async function loadProgram(page, program) {
  // Remove the overlay if present before interacting
  await removeOverlay(page);

  const inputArea = page.locator('.monaco-editor textarea.inputarea');

  // Focus Monaco's hidden textarea (the real input target)
  // Use force: true because Monaco's text layer intercepts pointer events
  await inputArea.click({ force: true });

  // Clear existing text
  await page.keyboard.press('ControlOrMeta+a');
  await page.keyboard.press('Backspace');

  // Insert text in one go (more reliable than typing, preserves newlines)
  await page.keyboard.insertText(program);

  // Remove overlay again before clicking Load
  await removeOverlay(page);

  // Wait for Load button to be enabled (syntax valid) - this implicitly waits for syntax check
  await page.waitForSelector('#load-button:not([disabled])', { timeout: 10000 });

  // Click Load button using the id selector for precision
  await page.click('#load-button');

  // Wait for the simulator to enter RUNNING state
  await waitForRunningState(page);
}

/**
 * Helper function to run the simulation to completion
 * @param {import('@playwright/test').Page} page - Playwright page object
 */
async function runToCompletion(page) {
  // Remove overlay before clicking
  await removeOverlay(page);

  // Wait for the Run button to be enabled before clicking
  await page.waitForSelector('#run-button:not([disabled])');

  // Add a small delay to ensure the UI is fully settled after the button becomes enabled
  // This helps avoid race conditions where the click might be ignored during a re-render
  await page.waitForTimeout(500);

  // Click the Run All button
  await page.click('#run-button');

  // Wait for execution to complete (stop button disabled)
  await waitForSimulationComplete(page);

  // Also wait for cycles to be updated in the UI (confirms stats have been rendered)
  // This prevents race conditions where the button is disabled but React hasn't re-rendered stats yet
  const cyclesCell = page.locator('#stat-cycles');
  await expect(cyclesCell).toHaveText(/^[1-9][0-9]*$/, { timeout: 10000 });
}

module.exports = {
  targetUri,
  removeOverlay,
  waitForPageReady,
  waitForRunningState,
  waitForSimulationComplete,
  loadProgram,
  runToCompletion
};
