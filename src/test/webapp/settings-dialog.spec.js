const { test, expect } = require('./fixtures');
const {
  targetUri,
  waitForPageReady,
  removeOverlay,
  loadProgram,
} = require('./test-utils');

/**
 * Coverage for the Settings dialog itself: the gear button in the header
 * opens a modal dialog (mirroring the Help dialog) with settings grouped
 * into a UI tab (General / Editor / Pipeline Colors sections) and a
 * Simulation tab (CPU / Cache sections — disabled while a program is
 * running). The individual controls inside each tab are covered by
 * settings-persistence.spec.js, forwarding.spec.js and
 * cache-simulator.spec.js; this spec only checks the dialog shell,
 * navigation between tabs, the RUNNING-disabled Simulation tab, and the two
 * Reset to defaults buttons.
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

test('the UI tab groups General, Editor and Pipeline Colors into labeled sections', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await page.click('#settings-button');
  await page.waitForSelector('.settings-title');

  const uiPanel = page.locator('#settings-tabpanel-0');

  // General: theme, accordion alerts, and the two Execution-pacing fields.
  await expect(uiPanel.getByText('General', { exact: true })).toBeVisible();
  await expect(page.getByTestId('theme-mode-toggle')).toBeVisible();
  await expect(page.getByLabel('Accordion Change Alerts')).toBeVisible();
  await expect(page.getByLabel('Multi Step Size')).toBeVisible();
  await expect(page.getByLabel('Execution Delay (ms)')).toBeVisible();

  // Editor: Vi mode and font size.
  await expect(uiPanel.getByText('Editor', { exact: true })).toBeVisible();
  await expect(page.getByLabel('Editor Vi Mode')).toBeVisible();
  await expect(page.getByLabel('Increase font size')).toBeVisible();

  // Pipeline Colors: the per-stage color pickers.
  await expect(uiPanel.getByText('Pipeline Colors', { exact: true })).toBeVisible();
  await expect(page.getByTestId('pipeline-color-IF')).toBeVisible();

  const [generalBox, editorBox, colorsBox] = await Promise.all([
    uiPanel.getByText('General', { exact: true }).boundingBox(),
    uiPanel.getByText('Editor', { exact: true }).boundingBox(),
    uiPanel.getByText('Pipeline Colors', { exact: true }).boundingBox(),
  ]);
  expect(generalBox.y).toBeLessThan(editorBox.y);
  expect(editorBox.y).toBeLessThan(colorsBox.y);

  await page.close();
});

test('the Simulation tab groups CPU and Cache into labeled sections', async ({
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
  // Execution now belongs to the UI tab, not Simulation.
  await expect(page.getByLabel('Multi Step Size')).not.toBeVisible();

  // Scope section-heading lookups to the Simulation tabpanel: the
  // right-hand Statistics panel behind the (still-rendered, just visually
  // dimmed) dialog also has headings of its own.
  const simulationPanel = page.locator('#settings-tabpanel-1');

  await expect(simulationPanel.getByText('CPU', { exact: true })).toBeVisible();
  await expect(page.getByLabel('CPU Forwarding')).toBeVisible();
  await expect(page.getByLabel('Branch Delay Slot')).toBeVisible();

  await expect(simulationPanel.getByText('Cache', { exact: true })).toBeVisible();
  await expect(page.locator('text=L1 Instruction Cache')).toBeVisible();
  await expect(page.locator('text=L1 Data Cache')).toBeVisible();

  const [cpuBox, cacheBox] = await Promise.all([
    simulationPanel.getByText('CPU', { exact: true }).boundingBox(),
    simulationPanel.getByText('Cache', { exact: true }).boundingBox(),
  ]);
  expect(cpuBox.y).toBeLessThan(cacheBox.y);

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

test('Reset UI to defaults asks for confirmation and is cancelable', async ({
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
  await page.click('#settings-reset-ui-button');

  // The setting must be unchanged: the reset was canceled.
  await expect(viModeSwitch).toBeChecked();

  await page.close();
});

test('Reset Simulation to defaults asks for confirmation and is cancelable', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await page.click('#settings-button');
  await page.getByRole('tab', { name: 'Simulation' }).click();

  const forwardingSwitch = page.getByLabel('CPU Forwarding');
  await forwardingSwitch.click();
  await expect(forwardingSwitch).toBeChecked();

  page.once('dialog', (dialog) => dialog.dismiss());
  await page.click('#settings-reset-simulation-button');

  await expect(forwardingSwitch).toBeChecked();

  await page.close();
});

test('the Simulation tab and its reset button are disabled while a program is running', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // A program that will not finish on its own in the short window we need
  // to observe the RUNNING state.
  const longProgram = `.code
DADDI r1, r0, 100
loop:
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;
  await loadProgram(page, longProgram);

  await page.click('#settings-button');
  await page.waitForSelector('.settings-title');

  // The Simulation tab itself must be unselectable while RUNNING — its CPU
  // and cache settings, and its "Reset Simulation to defaults" button,
  // would reset the CPU if applied now.
  await expect(page.locator('#settings-tab-1')).toBeDisabled();
  // The dialog opens on the UI tab, which stays fully usable, including its
  // own reset button.
  await expect(page.getByLabel('Editor Vi Mode')).toBeVisible();
  await expect(page.getByLabel('Editor Vi Mode')).toBeEnabled();
  await expect(page.locator('#settings-reset-ui-button')).toBeEnabled();

  // The Simulation tab's content (including its reset button) lives inside
  // that tab's panel, which isn't rendered while the disabled tab can't be
  // selected — so it isn't just disabled, it's unreachable.
  await expect(page.locator('#settings-reset-simulation-button')).toHaveCount(0);

  await page.click('#settings-close-button');
  await page.click('#stop-button');

  // Once stopped, the Simulation tab is reachable again, and its reset
  // button is enabled.
  await page.click('#settings-button');
  await expect(page.locator('#settings-tab-1')).toBeEnabled();
  await page.getByRole('tab', { name: 'Simulation' }).click();
  await expect(page.locator('#settings-reset-simulation-button')).toBeEnabled();
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
