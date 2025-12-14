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
  // Wait for the Stop button to become disabled (indicates simulation ended)
  await page.waitForSelector('#stop-button[disabled]', {
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

  // Click on the Monaco editor to focus it
  await page.click('.monaco-editor');

  // Select all text using cross-platform modifier (ControlOrMeta works on both Win/Linux and Mac)
  await page.keyboard.press('ControlOrMeta+a');
  await page.keyboard.type(program);

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

  // Click the Run All button
  await page.click('#run-button');

  // Wait for execution to complete
  await waitForSimulationComplete(page);
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
