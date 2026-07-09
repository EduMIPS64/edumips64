/**
 * Focused coverage for the Program ▾ dropdown menu (Alternative A design).
 *
 * The four program actions (New / Open… / Save… / Load Example) were moved
 * from individual header buttons into a single MUI <Menu> opened by
 * #program-menu-button.  Key contract:
 *
 *   - #program-menu-button is always visible in the header.
 *   - It is ENABLED only in EMPTY (no program loaded) and ENDED (program
 *     finished) states.
 *   - It is DISABLED whenever a program is loaded into the simulator:
 *     READY (loaded, paused), EXECUTING (running), and WAITING_FOR_INPUT.
 *   - The four menu items (#clear-code-button, #load-code-button,
 *     #save-code-button, #restore-sample-button) live in a MUI portal and
 *     are only present in the DOM when the menu is open.
 */

const { test, expect } = require('./fixtures');
const {
  targetUri,
  waitForPageReady,
  loadProgram,
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

test('program-menu: all four items visible when menu opened in EMPTY state', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  // Do NOT load a program — the page stays in EMPTY state (button enabled).

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
  await expect(page.locator('#restore-sample-button')).toContainText(
    'Load Example',
  );

  // Dismiss with Escape — items should leave the DOM again.
  await page.keyboard.press('Escape');
  await expect(page.locator('#clear-code-button')).toHaveCount(0);

  await page.close();
});

// ─── Disabled during EXECUTING, re-enabled after ─────────────────────────────

test('program-menu: button disabled during READY and EXECUTING, re-enabled after stop (→ EMPTY)', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, longProgram);
  await removeOverlay(page);

  // In READY state the button must be DISABLED (program is loaded into simulator).
  await expect(page.locator('#program-menu-button')).toBeDisabled();

  // Start execution.
  await page.waitForSelector('#run-button:not([disabled])');
  await page.click('#run-button');

  // Wait until EXECUTING: pause becomes enabled (definitive signal).
  await page.waitForSelector('#pause-button:not([disabled])', {
    timeout: 10000,
  });

  // Program menu button must be disabled while CPU is running.
  await expect(page.locator('#program-menu-button')).toBeDisabled();

  // Pause execution to transition back to READY cleanly.
  await page.click('#pause-button');
  await page.waitForSelector('#step-button:not([disabled])', {
    timeout: 10000,
  });

  // Back in READY — still disabled (program is still loaded).
  await expect(page.locator('#program-menu-button')).toBeDisabled();

  // Click stop to reset the simulator back to EMPTY.
  await page.click('#stop-button');

  // In EMPTY state the button must be ENABLED again.
  await page.waitForSelector('#program-menu-button:not([disabled])', {
    timeout: 10000,
  });
  await expect(page.locator('#program-menu-button')).toBeEnabled();

  await page.close();
});

test('program-menu: button disabled during READY and EXECUTING, re-enabled after simulation ends (ENDED)', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);
  await removeOverlay(page);

  // In READY state the button must be DISABLED (program is loaded into simulator).
  await expect(page.locator('#program-menu-button')).toBeDisabled();

  // Run to completion — ENDED state re-enables the button.
  // waitForSimulationComplete gates on #program-menu-button:not([disabled]),
  // which is valid because ENDED is one of the two enabled states.
  await page.waitForSelector('#run-button:not([disabled])');
  await page.click('#run-button');
  await waitForSimulationComplete(page);

  await expect(page.locator('#program-menu-button')).toBeVisible();
  await expect(page.locator('#program-menu-button')).toBeEnabled();

  await page.close();
});
