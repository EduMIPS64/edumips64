// test-utils.js does not define any tests, so only `expect` is needed here.
const { expect } = require('./fixtures');

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
  await page.waitForFunction(() => window.monaco);

  // Close the webpack dev server overlay if present
  await removeOverlay(page);
}

/**
 * Helper function to wait for simulator to enter RUNNING state (logical READY).
 *
 * With the always-present toolbar model, #step-button is rendered whenever the
 * toolbar is visible (READY or EXECUTING), but is DISABLED during EXECUTING.
 * The `:not([disabled])` filter ensures we only match READY state, where step
 * is enabled.  No change to the selector is required from the previous model.
 *
 * @param {import('@playwright/test').Page} page - Playwright page object
 */
async function waitForRunningState(page) {
  // #step-button is always rendered when the toolbar is visible, but disabled
  // in EXECUTING.  `:not([disabled])` ensures this only matches READY.
  await page.waitForSelector('#step-button:not([disabled])', {
    timeout: 10000,
  });
}

/**
 * Helper function to open the Program ▾ dropdown menu.
 * MUI <Menu> renders its items in a portal only when open, so callers must
 * invoke this before interacting with any of the four program-menu items.
 * @param {import('@playwright/test').Page} page - Playwright page object
 */
async function openProgramMenu(page) {
  await page.click('#program-menu-button');
  await page.waitForSelector('#program-menu', { state: 'visible' });
}

/**
 * Helper function to click a Program-menu item by id.
 * Opens the menu (if closed) then clicks the item.  Clicking the item closes
 * the menu automatically (MUI default behaviour).
 * @param {import('@playwright/test').Page} page - Playwright page object
 * @param {string} id - CSS id selector of the menu item, e.g. '#clear-code-button'
 */
async function clickProgramMenuItem(page, id) {
  await openProgramMenu(page);
  await page.click(id);
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

  // Wait for the Program menu button to become enabled — it is disabled whenever
  // a program is loaded into the simulator (READY/EXECUTING/WAITING_FOR_INPUT)
  // and re-enables only in EMPTY and ENDED.
  // (The individual menu items live in a MUI portal and are absent from the DOM
  // when the menu is closed, so we cannot use #clear-code-button here.)
  await page.waitForSelector('#program-menu-button:not([disabled])', {
    timeout: 30000,
  });
}

/**
 * Helper function to reset the simulator back to EMPTY state.
 * Clicks #stop-button (if visible and enabled) and waits for the Program menu
 * button to become enabled, which is the definitive signal that the simulator
 * has returned to EMPTY.
 * @param {import('@playwright/test').Page} page - Playwright page object
 */
async function resetSimulator(page) {
  const stopBtn = page.locator('#stop-button');
  const isVisible = await stopBtn.isVisible().catch(() => false);
  if (isVisible) {
    const isEnabled = await stopBtn.isEnabled().catch(() => false);
    if (isEnabled) {
      await stopBtn.click();
    }
  }
  // After stop (or if already EMPTY/ENDED), wait for the menu button to be enabled.
  await page.waitForSelector('#program-menu-button:not([disabled])', {
    timeout: 10000,
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

  // Move the mouse off the Load button so its hover tooltip dismisses.
  // Otherwise the tooltip stays open for the rest of the test and, because it
  // can flip to the `bottom` placement, its popper overlaps the accordions
  // just below the header and intercepts pointer events on them.
  await page.mouse.move(0, 0);

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
  resetSimulator,
  openProgramMenu,
  clickProgramMenuItem,
  loadProgram,
  runToCompletion
};
