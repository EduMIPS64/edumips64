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

  // The Pipeline accordion is collapsed by default; expand it
  const pipelineSummary = page.locator('text=Pipeline').locator('..');
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
