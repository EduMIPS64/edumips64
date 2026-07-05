const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

/**
 * Coverage for the Settings dialog itself: the gear button in the header
 * opens a modal dialog (mirroring the Help dialog) with settings grouped
 * into UI / CPU / Execution / Cache tabs. The individual controls inside
 * each tab are covered by settings-persistence.spec.js, forwarding.spec.js
 * and cache-simulator.spec.js; this spec only checks the dialog shell and
 * navigation between tabs.
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

  // All four tabs are present.
  await page.waitForSelector('#settings-tab-0');
  await page.waitForSelector('#settings-tab-1');
  await page.waitForSelector('#settings-tab-2');
  await page.waitForSelector('#settings-tab-3');
  expect(await page.textContent('#settings-tab-0')).toBe('UI');
  expect(await page.textContent('#settings-tab-1')).toBe('CPU');
  expect(await page.textContent('#settings-tab-2')).toBe('Execution');
  expect(await page.textContent('#settings-tab-3')).toBe('Cache');

  // The dialog opens on the UI tab by default, showing its controls.
  await expect(page.getByLabel('Editor Vi Mode')).toBeVisible();

  await page.close();
});

test('settings dialog tabs are logically grouped by settings type', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await page.click('#settings-button');
  await page.waitForSelector('.settings-title');

  // UI tab: editor/appearance settings.
  await expect(page.getByLabel('Editor Vi Mode')).toBeVisible();
  await expect(page.getByLabel('Accordion Change Alerts')).toBeVisible();
  await expect(page.getByTestId('theme-mode-toggle')).toBeVisible();

  // CPU tab: pipeline behavior settings.
  await page.click('#settings-tab-1');
  await expect(page.getByLabel('CPU Forwarding')).toBeVisible();
  await expect(page.getByLabel('Branch Delay Slot')).toBeVisible();
  // UI-tab controls are not part of this panel.
  await expect(page.getByLabel('Editor Vi Mode')).not.toBeVisible();

  // Execution tab: run pacing settings.
  await page.click('#settings-tab-2');
  await expect(page.getByLabel('Multi Step Size')).toBeVisible();
  await expect(page.getByLabel('Execution Delay (ms)')).toBeVisible();
  await expect(page.getByLabel('CPU Forwarding')).not.toBeVisible();

  // Cache tab: L1 cache configuration.
  await page.click('#settings-tab-3');
  await expect(page.locator('text=L1 Instruction Cache')).toBeVisible();
  await expect(page.locator('text=L1 Data Cache')).toBeVisible();
  await expect(page.getByLabel('Multi Step Size')).not.toBeVisible();

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
