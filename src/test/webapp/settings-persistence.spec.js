const { test, expect } = require('./fixtures');
const {
  targetUri,
  waitForPageReady,
  removeOverlay,
  loadProgram,
  openSettingsDialog,
  closeSettingsDialog,
} = require('./test-utils');

const STORAGE_PREFIX = 'edumips64:v1:';

/**
 * Clear all edumips64 localStorage keys before each test to ensure hermeticity.
 */
test.beforeEach(async ({ page }) => {
  await page.goto(targetUri);
  await page.evaluate((prefix) => {
    const keysToRemove = [];
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k?.startsWith(prefix)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => {
      window.localStorage.removeItem(k);
    });
  }, STORAGE_PREFIX);
});

async function setPipelineColor(page, key, value) {
  const input = page.getByTestId(`pipeline-color-${key}`);
  await input.evaluate((el, v) => {
    const setter = Object.getOwnPropertyDescriptor(
      window.HTMLInputElement.prototype,
      'value',
    ).set;
    setter.call(el, v);
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
  }, value);
}

/**
 * Test: Vi Mode and font size persist across page reloads.
 */
test('viMode and fontSize persist across page reloads', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  // Toggle Vi Mode on
  const viModeSwitch = page.getByLabel('Editor Vi Mode');
  await expect(viModeSwitch).not.toBeChecked();
  await viModeSwitch.click();
  await expect(viModeSwitch).toBeChecked();

  // Increase font size by 2 (default is 14, target is 16)
  const increaseFontBtn = page.getByLabel('Increase font size');
  await increaseFontBtn.click();
  await increaseFontBtn.click();

  // Verify localStorage was updated
  const viModeStored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}viMode`,
  );
  expect(viModeStored).toBe('true');

  const fontSizeStored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}fontSize`,
  );
  expect(fontSizeStored).toBe('16');

  // Reload the page
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  // Assert Vi Mode is still on
  const viModeSwitchAfterReload = page.getByLabel('Editor Vi Mode');
  await expect(viModeSwitchAfterReload).toBeChecked();

  // Assert font size is still 16
  const fontSizeDisplay = page.getByLabel('Current font size: 16');
  await expect(fontSizeDisplay).toBeVisible();
});

/**
 * Test: accordionAlerts persists across page reloads.
 */
test('accordionAlerts persists across page reloads', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  // Accordion Alerts is on by default; toggle it off
  const alertsSwitch = page.getByLabel('Accordion Change Alerts');
  await expect(alertsSwitch).toBeChecked();
  await alertsSwitch.click();
  await expect(alertsSwitch).not.toBeChecked();

  // Verify localStorage
  const stored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}accordionAlerts`,
  );
  expect(stored).toBe('false');

  // Reload and verify
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  const alertsSwitchAfterReload = page.getByLabel('Accordion Change Alerts');
  await expect(alertsSwitchAfterReload).not.toBeChecked();
});

/**
 * Test: stepStride and executionDelayMs persist across page reloads.
 */
test('stepStride and executionDelayMs persist across page reloads', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  // Change Multi Step Size from the default (500) to 250.
  const strideInput = page.getByLabel('Multi Step Size');
  await strideInput.fill('250');
  await strideInput.blur();

  // Change Execution Delay from the default (0) to 100 ms.
  const delayInput = page.getByLabel('Execution Delay (ms)');
  await delayInput.fill('100');
  await delayInput.blur();

  // Verify localStorage was updated.
  const strideStored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}stepStride`,
  );
  expect(strideStored).toBe('250');

  const delayStored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}executionDelayMs`,
  );
  expect(delayStored).toBe('100');

  // Reload and assert the values survived.
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  await expect(page.getByLabel('Multi Step Size')).toHaveValue('250');
  await expect(page.getByLabel('Execution Delay (ms)')).toHaveValue('100');

  // The Settings dialog is modal, so it must be closed before interacting
  // with the editor / Load button behind it.
  await closeSettingsDialog(page);

  // The Multi Step button is only rendered in READY state (contextual run
  // controls).  Load a minimal program so the toolbar transitions from EMPTY
  // to READY and the button becomes visible for the tooltip assertion.
  const simpleProgram = `.code\nDADDI r1, r0, 1\nSYSCALL 0\n`;
  await loadProgram(page, simpleProgram);

  // The Multi Step button is now icon-only (aria-label="Multi Step"); the
  // stride is surfaced via the MUI Tooltip.  Hover to trigger the tooltip and
  // assert it contains the configured count, confirming the component reads
  // from the persisted setting.
  await page.waitForSelector('#multi-step-button');
  await page.hover('#multi-step-button');
  await expect(
    page
      .locator('.MuiTooltip-tooltip')
      .filter({ hasText: 'Run 250 steps of simulation' }),
  ).toBeVisible();
});

/**
 * Test: pipelineColors persist across page reloads, and the in-page Pipeline
 * widget actually picks up the customized values.
 */
test('pipelineColors persist across page reloads', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  // Set IF and Stall colors via the native color inputs. React tracks
  // input values via a hidden internal property, so we have to use the
  // native setter to make sure the synthetic onChange fires.
  await setPipelineColor(page, 'IF', '#123456');
  await setPipelineColor(page, 'Stall', '#abcdef');

  // The persisted value is written by `useLocalStorage`'s effect, which
  // fires asynchronously after the render — wait for the storage write to
  // land before asserting on it.
  await expect
    .poll(() =>
      page.evaluate(
        (key) => JSON.parse(window.localStorage.getItem(key) || 'null'),
        `${STORAGE_PREFIX}pipelineColors`,
      ),
    )
    .toMatchObject({ IF: '#123456', Stall: '#abcdef' });

  // Verify localStorage was updated with our overrides on top of the defaults.
  const stored = await page.evaluate(
    (key) => JSON.parse(window.localStorage.getItem(key) || 'null'),
    `${STORAGE_PREFIX}pipelineColors`,
  );
  expect(stored).not.toBeNull();
  expect(stored.IF).toBe('#123456');
  expect(stored.Stall).toBe('#abcdef');
  // Untouched stages must keep their defaults.
  expect(stored.MEM).toBe('#4caf50');

  // Reload and assert the inputs come back with the persisted values.
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  const ifInput = page.getByTestId('pipeline-color-IF');
  await expect(ifInput).toHaveValue('#123456');
  const stallInput = page.getByTestId('pipeline-color-Stall');
  await expect(stallInput).toHaveValue('#abcdef');
});

test('pipelineColors also drive Monaco stage highlights', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');
  await setPipelineColor(page, 'IF', '#123456');
  await closeSettingsDialog(page);

  await loadProgram(
    page,
    `.code
  daddi r1,r0,1
  daddi r2,r0,2
  syscall 0
`,
  );

  await page.click('#step-button');

  await expect
    .poll(() =>
      page.evaluate(() => {
        const codeWrapper = document.querySelector(
          '[data-testid="code-editor"]',
        );
        const fill = document
          .querySelector('#pipeline g[data-stage="IF"]')
          ?.getAttribute('data-fill');
        const hasDecoration = window.editor
          .getModel()
          .getAllDecorations()
          .some((d) => d.options.className === 'stageIf');
        if (!codeWrapper || !fill) {
          return null;
        }
        const style = getComputedStyle(codeWrapper);
        return {
          fill,
          highlightColor: style.getPropertyValue('--pipeline-stage-if').trim(),
          hasDecoration,
        };
      }),
    )
    .toMatchObject({
      fill: '#123456',
      highlightColor: '#12345680',
      hasDecoration: true,
    });
});

/**
 * Test: the Pipeline Colors "Reset to defaults" button restores every entry
 * to the schema defaults.
 */
/**
 * Test: "Reset UI to defaults" only touches the UI tab's settings (which
 * now includes Execution), leaving Simulation-tab settings (CPU, cache)
 * untouched. Mirrors the Swing UI's `Config.RESET`, but scoped by type per
 * review feedback: applying Simulation defaults resets the CPU, so the two
 * are kept independent.
 */
test('Reset UI to defaults resets only the UI tab, not Simulation', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');

  const viModeSwitch = page.getByLabel('Editor Vi Mode');
  await viModeSwitch.click();
  await expect(viModeSwitch).toBeChecked();

  const ifInput = page.getByTestId('pipeline-color-IF');
  await ifInput.evaluate((el) => {
    const setter = Object.getOwnPropertyDescriptor(
      window.HTMLInputElement.prototype,
      'value',
    ).set;
    setter.call(el, '#000000');
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
  });
  await expect(ifInput).toHaveValue('#000000');

  // A non-default value on the Simulation tab, which "Reset UI" must not touch.
  await page.getByRole('tab', { name: 'Simulation' }).click();
  const forwardingSwitch = page.getByLabel('CPU Forwarding');
  await forwardingSwitch.click();
  await expect(forwardingSwitch).toBeChecked();

  // Back to UI to click its reset button. The button asks for confirmation
  // via window.confirm(); accept it.
  await page.getByRole('tab', { name: 'UI' }).click();
  page.once('dialog', (dialog) => dialog.accept());
  await page.click('#settings-reset-ui-button');

  await expect(page.getByLabel('Editor Vi Mode')).not.toBeChecked();
  // Schema default for IF (mirrors Swing UI's `IF_COLOR`).
  await expect(page.getByTestId('pipeline-color-IF')).toHaveValue('#ebeb3b');

  // Simulation-tab setting survives.
  await page.getByRole('tab', { name: 'Simulation' }).click();
  await expect(page.getByLabel('CPU Forwarding')).toBeChecked();
});

/**
 * Test: "Reset Simulation to defaults" only touches CPU/cache settings,
 * leaving UI-tab settings untouched.
 */
test('Reset Simulation to defaults resets only the Simulation tab, not UI', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsDialog(page, 'UI');
  const viModeSwitch = page.getByLabel('Editor Vi Mode');
  await viModeSwitch.click();
  await expect(viModeSwitch).toBeChecked();

  await page.getByRole('tab', { name: 'Simulation' }).click();
  const forwardingSwitch = page.getByLabel('CPU Forwarding');
  await forwardingSwitch.click();
  await expect(forwardingSwitch).toBeChecked();

  page.once('dialog', (dialog) => dialog.accept());
  await page.click('#settings-reset-simulation-button');

  await expect(forwardingSwitch).not.toBeChecked();

  // UI-tab setting survives.
  await page.getByRole('tab', { name: 'UI' }).click();
  await expect(page.getByLabel('Editor Vi Mode')).toBeChecked();
});

/**
 * Cleanup: clear edumips64 localStorage keys after each test.
 */
test.afterEach(async ({ page }) => {
  await page.evaluate((prefix) => {
    const keysToRemove = [];
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k?.startsWith(prefix)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => {
      window.localStorage.removeItem(k);
    });
  }, STORAGE_PREFIX);
});
