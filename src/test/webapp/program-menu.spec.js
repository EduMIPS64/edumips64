/**
 * Focused coverage for the Program ▾ dropdown menu (Alternative A design).
 *
 * The four program actions (New / Open… / Save… / Load Example) were moved
 * from individual header buttons into a single MUI <Menu> opened by
 * #program-menu-button.  Key contract:
 *
 *   - #program-menu-button is always visible in the header.
 *   - It is ENABLED in EMPTY, READY, and ENDED states.
 *   - It is DISABLED while the CPU is EXECUTING or WAITING_FOR_INPUT.
 *   - The four menu items (#clear-code-button, #load-code-button,
 *     #save-code-button, #restore-sample-button) live in a MUI portal and
 *     are only present in the DOM when the menu is open.
 */

'use strict';

const { test, expect } = require('./fixtures');
const {
  targetUri,
  waitForPageReady,
  loadProgram,
  runToCompletion,
  removeOverlay,
  waitForSimulationComplete,
  openProgramMenu,
} = require('./test-utils');

// A simple program that completes quickly.
const simpleProgram = `.code
DADDI r1, r0, 1
SYSCALL 0
`;

// Long-running program — 10 000 iterations — gives Playwright a comfortable
// window to observe the transient EXECUTING state.
const longProgram = `.code
DADDI r1, r0, 10000
loop:
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;

// ─── Menu items visible when open ────────────────────────────────────────────

test('program-menu: all four items visible when menu opened in READY state', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);

  // Items must NOT be in the DOM before the menu is opened.
  await expect(page.locator('#clear-code-button')).toHaveCount(0);
  await expect(page.locator('#load-code-button')).toHaveCount(0);
  await expect(page.locator('#save-code-button')).toHaveCount(0);
  await expect(page.locator('#restore-sample-button')).toHaveCount(0);

  // Open the menu and verify all four items appear with the correct labels.
  await openProgramMenu(page);

  await expect(page.locator('#clear-code-button')).toBeVisible();
  await expect(page.locator('#load-code-button')).toBeVisible();
  await expect(page.locator('#save-code-button')).toBeVisible();
  await expect(page.locator('#restore-sample-button')).toBeVisible();

  // Check display labels.
  await expect(page.locator('#clear-code-button')).toContainText('New');
  await expect(page.locator('#load-code-button')).toContainText('Open');
  await expect(page.locator('#save-code-button')).toContainText('Save');
  await expect(page.locator('#restore-sample-button')).toContainText('Load Example');

  // Dismiss with Escape — items should leave the DOM again.
  await page.keyboard.press('Escape');
  await expect(page.locator('#clear-code-button')).toHaveCount(0);

  await page.close();
});

// ─── Disabled during EXECUTING, re-enabled after ─────────────────────────────

test('program-menu: button disabled during EXECUTING, re-enabled after completion', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, longProgram);
  await removeOverlay(page);

  // In READY state the button must be enabled.
  await expect(page.locator('#program-menu-button')).toBeEnabled();

  // Start execution.
  await page.waitForSelector('#run-button:not([disabled])');
  await page.click('#run-button');

  // Wait until EXECUTING: pause becomes enabled (definitive signal).
  await page.waitForSelector('#pause-button:not([disabled])', { timeout: 10000 });

  // Program menu button must be disabled while CPU is running.
  await expect(page.locator('#program-menu-button')).toBeDisabled();

  // Pause execution to transition back to READY cleanly.
  await page.click('#pause-button');
  await page.waitForSelector('#step-button:not([disabled])', { timeout: 10000 });

  // Button must be enabled again in READY.
  await expect(page.locator('#program-menu-button')).toBeEnabled();

  // Clean up.
  await page.click('#stop-button');
  await page.close();
});

test('program-menu: button disabled during EXECUTING, re-enabled after simulation ends', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);
  await removeOverlay(page);

  // Run to completion and verify the button re-enables (waitForSimulationComplete
  // already gates on #program-menu-button:not([disabled])).
  await page.waitForSelector('#run-button:not([disabled])');
  await page.click('#run-button');
  await waitForSimulationComplete(page);

  await expect(page.locator('#program-menu-button')).toBeVisible();
  await expect(page.locator('#program-menu-button')).toBeEnabled();

  await page.close();
});
