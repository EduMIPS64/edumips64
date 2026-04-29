const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

/**
 * Regression test for the "default sample triggers a permanent Issues 1
 * badge" report: a brand-new visitor should land on a clean Issues panel.
 * The default sample must therefore contain no warnings or errors.
 */
test('default sample loads with zero issues on a fresh page', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // No `.error-list-item` rows should be rendered, and the Issues accordion
  // header must not show a count chip (i.e. just "Issues", not "Issues 1").
  await expect(page.locator('.error-list-item')).toHaveCount(0);
  await expect(
    page.getByRole('heading', { name: /^Issues\s*\d+/ })
  ).toHaveCount(0);
});
