const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady } = require('./test-utils');

/**
 * Regression suite for the batch of accessibility quick-wins:
 *   - #1700  <html lang>, document <h1>, skip-link
 *   - #1699  Help IconButton accessible name
 *   - #1705  CPU status pill announced as a live region
 *   - #1698  Sphinx ¶ headerlinks hidden inside the embedded help iframe
 */

test.describe('accessibility quick wins', () => {
  test('document has lang, single h1, and a skip link', async ({ page }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    // <html lang="en"> — needed so screen readers pick the right voice
    // and so automated a11y tools (axe, Lighthouse) don't flag the page.
    const lang = await page.evaluate(() => document.documentElement.lang);
    expect(lang).toBe('en');

    // Exactly one document-level <h1>. The visible UI uses h3 panel
    // headings, so the page-level h1 is visually hidden.
    await expect(page.locator('h1')).toHaveCount(1);
    await expect(page.locator('h1')).toHaveText(/EduMIPS64/i);

    // Skip link is present and points at the React mount node.
    const skip = page.locator('a.skip-link');
    await expect(skip).toHaveCount(1);
    await expect(skip).toHaveAttribute('href', '#simulator');
  });

  test('help button has an accessible name', async ({ page }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    // Locating by role+name proves the IconButton exposes an accessible
    // name to assistive tech (rather than just "button" with no label).
    const helpBtn = page.getByRole('button', { name: /open help/i });
    await expect(helpBtn).toBeVisible();
  });

  test('CPU status chip is exposed as a polite live region', async ({ page }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    // The chip is inside a tooltip wrapper; we target by role+name so
    // any nesting changes don't break the test.
    const status = page.getByRole('status', { name: /cpu status/i });
    await expect(status).toBeVisible();
    await expect(status).toHaveAttribute('aria-live', 'polite');
  });

  test('help iframe does not render Sphinx pilcrow permalinks', async ({ page }) => {
    await page.goto(targetUri);
    await waitForPageReady(page);

    await page.getByRole('button', { name: /open help/i }).click();

    // Wait for the help iframe to attach and load its index page.
    const iframeLocator = page.locator('#help-iframe');
    await expect(iframeLocator).toBeVisible();
    const frame = page.frameLocator('#help-iframe');

    // Heading should still render; Sphinx anchors should be hidden.
    await expect(frame.locator('h1').first()).toBeVisible();
    const visibleHeaderlinks = await frame.locator('a.headerlink:visible').count();
    expect(visibleHeaderlinks).toBe(0);
  });
});
