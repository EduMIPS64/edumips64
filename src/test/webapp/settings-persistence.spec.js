const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

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
      if (k && k.startsWith(prefix)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => window.localStorage.removeItem(k));
  }, STORAGE_PREFIX);
});

/**
 * Helper: ensure the General Settings accordion is expanded.
 *
 * The MUI `AccordionSummary` renders as a button whose accessible name is the
 * text it contains ("General Settings"). We target it by role so that
 * `aria-expanded` is read from the correct element — an earlier version of
 * this helper took `text=... >> ..` which landed on the Typography's parent,
 * not on the button, so `aria-expanded` was always `null` and the helper
 * ended up *toggling* (collapsing) the accordion instead of ensuring it was
 * open, which made the switches invisible and timed the tests out.
 */
async function openSettingsAccordion(page) {
  const summary = page.getByRole('button', { name: /General Settings/ });
  await summary.waitFor({ state: 'visible' });
  if ((await summary.getAttribute('aria-expanded')) !== 'true') {
    await summary.click();
  }
  // Wait until the accordion reports as expanded before returning, so the
  // switches inside have had a chance to become visible.
  await expect(summary).toHaveAttribute('aria-expanded', 'true');
}

/**
 * Test: Vi Mode and font size persist across page reloads.
 */
test('viMode and fontSize persist across page reloads', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);

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
    `${STORAGE_PREFIX}viMode`
  );
  expect(viModeStored).toBe('true');

  const fontSizeStored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}fontSize`
  );
  expect(fontSizeStored).toBe('16');

  // Reload the page
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);

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

  await openSettingsAccordion(page);

  // Accordion Alerts is on by default; toggle it off
  const alertsSwitch = page.getByLabel('Accordion Change Alerts');
  await expect(alertsSwitch).toBeChecked();
  await alertsSwitch.click();
  await expect(alertsSwitch).not.toBeChecked();

  // Verify localStorage
  const stored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}accordionAlerts`
  );
  expect(stored).toBe('false');

  // Reload and verify
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);

  const alertsSwitchAfterReload = page.getByLabel('Accordion Change Alerts');
  await expect(alertsSwitchAfterReload).not.toBeChecked();
});

/**
 * Test: expandedAccordions persists across page reloads.
 */
test('expandedAccordions persists across page reloads', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  // The Pipeline accordion is collapsed by default; expand it. Use the
  // accordion's button role (its accessible name is exactly "Pipeline") so
  // we don't accidentally match the "Pipeline Colors" section in Settings.
  const pipelineSummary = page.getByRole('button', { name: 'Pipeline' });
  await pipelineSummary.click();
  await page.waitForTimeout(300);

  // Verify localStorage contains the updated accordions object with pipeline: true
  const stored = await page.evaluate(
    (key) => JSON.parse(window.localStorage.getItem(key) || 'null'),
    `${STORAGE_PREFIX}expandedAccordions`
  );
  expect(stored).not.toBeNull();
  expect(stored.pipeline).toBe(true);

  // Reload and verify Pipeline accordion is still expanded
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  const storedAfterReload = await page.evaluate(
    (key) => JSON.parse(window.localStorage.getItem(key) || 'null'),
    `${STORAGE_PREFIX}expandedAccordions`
  );
  expect(storedAfterReload).not.toBeNull();
  expect(storedAfterReload.pipeline).toBe(true);
});

/**
 * Test: stepStride and executionDelayMs persist across page reloads.
 */
test('stepStride and executionDelayMs persist across page reloads', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);

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
    `${STORAGE_PREFIX}stepStride`
  );
  expect(strideStored).toBe('250');

  const delayStored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}executionDelayMs`
  );
  expect(delayStored).toBe('100');

  // Reload and assert the values survived.
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);

  await expect(page.getByLabel('Multi Step Size')).toHaveValue('250');
  await expect(page.getByLabel('Execution Delay (ms)')).toHaveValue('100');

  // The Multi Step button's tooltip reflects the configured stride, so the
  // header really does read from the persisted setting.
  await expect(
    page.getByRole('button', { name: /Run 250 steps of simulation/ })
  ).toBeVisible();
});

/**
 * Test: pipelineColors persist across page reloads, and the in-page Pipeline
 * widget actually picks up the customized values.
 */
test('pipelineColors persist across page reloads', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);

  // Set IF and Stall colors via the native color inputs. React tracks
  // input values via a hidden internal property, so we have to use the
  // native setter to make sure the synthetic onChange fires.
  const setColor = async (key, value) => {
    const input = page.getByTestId(`pipeline-color-${key}`);
    await input.evaluate((el, v) => {
      const setter = Object.getOwnPropertyDescriptor(
        window.HTMLInputElement.prototype,
        'value'
      ).set;
      setter.call(el, v);
      el.dispatchEvent(new Event('input', { bubbles: true }));
      el.dispatchEvent(new Event('change', { bubbles: true }));
    }, value);
  };
  await setColor('IF', '#123456');
  await setColor('Stall', '#abcdef');

  // The persisted value is written by `useLocalStorage`'s effect, which
  // fires asynchronously after the render — wait for the storage write to
  // land before asserting on it.
  await expect
    .poll(() =>
      page.evaluate(
        (key) => JSON.parse(window.localStorage.getItem(key) || 'null'),
        `${STORAGE_PREFIX}pipelineColors`
      )
    )
    .toMatchObject({ IF: '#123456', Stall: '#abcdef' });

  // Verify localStorage was updated with our overrides on top of the defaults.
  const stored = await page.evaluate(
    (key) => JSON.parse(window.localStorage.getItem(key) || 'null'),
    `${STORAGE_PREFIX}pipelineColors`
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

  await openSettingsAccordion(page);

  const ifInput = page.getByTestId('pipeline-color-IF');
  await expect(ifInput).toHaveValue('#123456');
  const stallInput = page.getByTestId('pipeline-color-Stall');
  await expect(stallInput).toHaveValue('#abcdef');
});

/**
 * Test: the Pipeline Colors "Reset to defaults" button restores every entry
 * to the schema defaults.
 */
test('pipelineColors reset restores schema defaults', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);

  // Pick a non-default value for IF to make sure reset really overwrites.
  const ifInput = page.getByTestId('pipeline-color-IF');
  await ifInput.evaluate((el) => {
    const setter = Object.getOwnPropertyDescriptor(
      window.HTMLInputElement.prototype,
      'value'
    ).set;
    setter.call(el, '#000000');
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
  });
  await expect(ifInput).toHaveValue('#000000');

  await page.getByTestId('pipeline-colors-reset').click();

  // Schema default for IF (mirrors Swing UI's `IF_COLOR`).
  await expect(ifInput).toHaveValue('#ebeb3b');
});

/**
 * Cleanup: clear edumips64 localStorage keys after each test.
 */
test.afterEach(async ({ page }) => {
  await page.evaluate((prefix) => {
    const keysToRemove = [];
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k && k.startsWith(prefix)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => window.localStorage.removeItem(k));
  }, STORAGE_PREFIX);
});
