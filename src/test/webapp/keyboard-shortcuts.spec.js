'use strict';

/**
 * Keyboard shortcuts spec — issue #1706.
 *
 * Tests the global keyboard shortcuts for run-control actions:
 *   F2  — Load program
 *   F8  — Run All (READY) / Pause (EXECUTING)
 *   F9  — Single Step (READY)
 *   F10 — Multi Step (READY)
 *   Esc — Stop & reset (READY)
 *
 * Uses a short program to avoid long runtimes in CI.
 */

const { test, expect } = require('./fixtures');
const {
  targetUri,
  waitForPageReady,
  loadProgram,
  removeOverlay,
  waitForRunningState,
} = require('./test-utils');

// Short program — just two instructions, finishes in a handful of cycles.
const shortProgram = `.code
DADDI r1, r0, 42
SYSCALL 0
`;

// ─── F2: Load program ─────────────────────────────────────────────────────────

test('F2 loads the program and shows the run-controls toolbar', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // Type the short program into the editor.
  const inputArea = page.locator('.monaco-editor textarea.inputarea');
  await inputArea.click({ force: true });
  await page.keyboard.press('ControlOrMeta+a');
  await page.keyboard.press('Backspace');
  await page.keyboard.insertText(shortProgram);

  // Wait for the Load button to be enabled (syntax valid).
  await page.waitForSelector('#load-button:not([disabled])', { timeout: 10000 });

  // Press F2 to load.
  await page.keyboard.press('F2');
  await page.mouse.move(0, 0);

  // The run-controls toolbar should now be visible (simulator is in READY state).
  await page.waitForSelector('#run-controls-toolbar', { state: 'visible', timeout: 10000 });
  await expect(page.locator('#run-controls-toolbar')).toBeVisible();

  await page.close();
});

// ─── F9: Single Step ─────────────────────────────────────────────────────────

test('F9 advances a single simulation step after loading', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, shortProgram);

  // Read the initial cycle count.
  const initialCycles = await page.locator('#stat-cycles').textContent();

  // Press F9 to advance one step.
  await page.keyboard.press('F9');

  // The cycle counter should increment.
  await expect(page.locator('#stat-cycles')).not.toHaveText(initialCycles, { timeout: 5000 });

  await page.close();
});

// ─── F8: Run All / Esc: Stop ─────────────────────────────────────────────────

test('F8 starts Run All and Esc stops and resets', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, shortProgram);

  // F8 — run all (short program finishes quickly).
  await page.keyboard.press('F8');

  // After the program ends the toolbar disappears (ENDED state).
  // Wait for the toolbar to be hidden.
  await page.waitForSelector('#run-controls-toolbar', { state: 'hidden', timeout: 10000 });

  // The program menu button (disabled while RUNNING) should now be enabled.
  await page.waitForSelector('#program-menu-button:not([disabled])', { timeout: 10000 });

  // Reload so we have a fresh READY state for the Esc test.
  await page.click('#load-button');
  await page.mouse.move(0, 0);
  await waitForRunningState(page);

  // Esc — stop and reset.
  await page.keyboard.press('Escape');

  // Toolbar should disappear and program menu button should be re-enabled.
  await page.waitForSelector('#run-controls-toolbar', { state: 'hidden', timeout: 10000 });
  await page.waitForSelector('#program-menu-button:not([disabled])', { timeout: 10000 });

  await page.close();
});

// ─── Help dialog: Shortcuts tab ───────────────────────────────────────────────

test('Help dialog has a Shortcuts tab that lists all keys', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // Open the help dialog.
  await page.click('#help-button');
  await page.waitForSelector('.help-title', { state: 'visible' });

  // The Shortcuts tab should exist at index 1.
  await page.waitForSelector('#help-tab-1');
  await page.click('#help-tab-1');

  // The shortcuts panel should be visible.
  await page.waitForSelector('#help-shortcuts', { state: 'visible', timeout: 5000 });

  // All five keys should appear in the panel text.
  const shortcutsPanel = page.locator('#help-shortcuts');
  await expect(shortcutsPanel).toContainText('F2');
  await expect(shortcutsPanel).toContainText('F8');
  await expect(shortcutsPanel).toContainText('F9');
  await expect(shortcutsPanel).toContainText('F10');
  await expect(shortcutsPanel).toContainText('Esc');

  // Close the dialog.
  await page.click('#help-close-button');

  await page.close();
});
