const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

/**
 * Coverage for the Settings dialog itself: the gear button in the header
 * opens a modal dialog (mirroring the Help dialog) with settings grouped
 * into a UI tab and a Simulation tab (CPU / Execution / Cache sections).
 * The individual controls inside each tab are covered by
 * settings-persistence.spec.js, forwarding.spec.js and
 * cache-simulator.spec.js; this spec only checks the dialog shell,
 * navigation between tabs, and the dialog-wide Reset to defaults button.
 */

test('settings dialog opens from the gear button next to the Program menu', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // The gear button is always visible in the header, right after the
  // Program menu button and before the Help button.
  const settingsButton = page.locator('#settings-button');
  await expect(settingsButton).toBeVisible();

  await settingsButton.click();

  // Wait for the dialog to appear.
  await page.waitForSelector('.settings-title');
  const title = await page.textContent('.settings-title');
  expect(title).toContain('Settings');

  // Both tabs are present.
  await page.waitForSelector('#settings-tab-0');
  await page.waitForSelector('#settings-tab-1');
  expect(await page.textContent('#settings-tab-0')).toBe('UI');
  expect(await page.textContent('#settings-tab-1')).toBe('Simulation');

  // The dialog opens on the UI tab by default, showing its controls.
  await expect(page.getByLabel('Editor Vi Mode')).toBeVisible();

  await page.close();
});

test('the Simulation tab groups CPU, Execution and Cache into labeled sections', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await page.click('#settings-button');
  await page.waitForSelector('.settings-title');

  // UI tab controls are visible by default...
  await expect(page.getByLabel('Editor Vi Mode')).toBeVisible();

  // ...and not part of the Simulation tab's panel.
  await page.click('#settings-tab-1');
  await expect(page.getByLabel('Editor Vi Mode')).not.toBeVisible();

  // Scope section-heading lookups to the Simulation tabpanel: the
  // right-hand Statistics panel behind the (still-rendered, just visually
  // dimmed) dialog also has an "Execution" heading of its own.
  const simulationPanel = page.locator('#settings-tabpanel-1');

  // All three sections are visible together, in a single tab, in order.
  await expect(simulationPanel.getByText('CPU', { exact: true })).toBeVisible();
  await expect(page.getByLabel('CPU Forwarding')).toBeVisible();
  await expect(page.getByLabel('Branch Delay Slot')).toBeVisible();

  await expect(simulationPanel.getByText('Execution', { exact: true })).toBeVisible();
  await expect(page.getByLabel('Multi Step Size')).toBeVisible();
  await expect(page.getByLabel('Execution Delay (ms)')).toBeVisible();

  await expect(simulationPanel.getByText('Cache', { exact: true })).toBeVisible();
  await expect(page.locator('text=L1 Instruction Cache')).toBeVisible();
  await expect(page.locator('text=L1 Data Cache')).toBeVisible();

  const [cpuBox, executionBox, cacheBox] = await Promise.all([
    simulationPanel.getByText('CPU', { exact: true }).boundingBox(),
    simulationPanel.getByText('Execution', { exact: true }).boundingBox(),
    simulationPanel.getByText('Cache', { exact: true }).boundingBox(),
  ]);
  expect(cpuBox.y).toBeLessThan(executionBox.y);
  expect(executionBox.y).toBeLessThan(cacheBox.y);

  await page.close();
});

test('settings dialog closes with the Close button', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await page.click('#settings-button');
  await page.waitForSelector('.settings-title');

  await page.click('#settings-close-button');
  await expect(page.locator('.settings-title')).toBeHidden();

  // The gear button remains available to reopen it.
  await expect(page.locator('#settings-button')).toBeVisible();
});

test('Reset to defaults asks for confirmation and is cancelable', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await page.click('#settings-button');
  await page.waitForSelector('.settings-title');

  const viModeSwitch = page.getByLabel('Editor Vi Mode');
  await viModeSwitch.click();
  await expect(viModeSwitch).toBeChecked();

  // Dismiss the confirmation instead of accepting it.
  page.once('dialog', (dialog) => dialog.dismiss());
  await page.click('#settings-reset-button');

  // The setting must be unchanged: the reset was canceled.
  await expect(viModeSwitch).toBeChecked();

  await page.close();
});

test('cache configuration and general settings are no longer separate accordions', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // Both panels used to live inline in the right-hand rail; they are now
  // exclusively inside the Settings dialog.
  await expect(
    page.getByRole('button', { name: /Cache Configuration/ }),
  ).toHaveCount(0);
  await expect(
    page.getByRole('button', { name: /General Settings/ }),
  ).toHaveCount(0);
});
