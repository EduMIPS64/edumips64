const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

/**
 * Tests for simulator panel visibility.
 *
 * The simulator uses a DashboardCard layout where all panels are always
 * visible (Stats, Pipeline, Registers, Memory, Standard Output). There is
 * no expand/collapse behavior — every card is rendered unconditionally.
 */

/**
 * Verify that all dashboard panels are present and visible on page load.
 */
test('all simulator panels are visible on load', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await expect(page.locator('#stats-card')).toBeVisible();
  await expect(page.locator('#pipeline-card')).toBeVisible();
  await expect(page.locator('#registers-card')).toBeVisible();
  await expect(page.locator('#memory-card')).toBeVisible();
  await expect(page.locator('#stdout-card')).toBeVisible();
});
