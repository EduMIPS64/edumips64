const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  waitForSimulationComplete,
} = require('./test-utils');

/**
 * Regression test for the "Standard Output text invisible (white-on-light)"
 * bug: when the OS is in dark mode but the user forces the app to the light
 * theme, the global CSS media-query rule (`@media prefers-color-scheme: dark
 * { textarea { color: white } }`) used to make the stdout text white on a
 * light-theme background — invisible to the user.
 *
 * The fix makes StdOut.js theme-aware via MUI's useTheme() so its inline
 * color/backgroundColor always follow the active MUI palette (overriding the
 * stylesheet media-query rule) regardless of the OS colour scheme.
 *
 * This test reproduces the exact failure scenario:
 *   OS = dark, app theme forced to 'light' via localStorage.
 * It asserts both that the textarea VALUE contains "being tested!" AND that
 * the rendered text colour contrasts with the background (i.e. is not
 * white-on-light / invisible).
 */
test('default sample stdout text is visible in light theme with dark OS', async ({ page }) => {
  // Emulate OS dark preference while the app is forced to light theme.
  await page.emulateMedia({ colorScheme: 'dark' });

  // Seed localStorage BEFORE the app loads so useSetting picks it up.
  // The key follows the edumips64:v1:<name> namespace; the value is
  // JSON-serialised (JSON.stringify('light') === '"light"').
  await page.addInitScript(() => {
    window.localStorage.setItem('edumips64:v1:themeMode', '"light"');
  });

  await page.goto(targetUri);
  await waitForPageReady(page);

  await removeOverlay(page);

  // Load the default sample (preloaded in the editor).
  await page.waitForSelector('#load-button:not([disabled])', { timeout: 10000 });
  await page.click('#load-button');
  await page.mouse.move(0, 0);

  // Wait for READY state, then Run All.
  await page.waitForSelector('#run-button:not([disabled])', { timeout: 10000 });
  await page.click('#run-button');
  await page.mouse.move(0, 0);
  await waitForSimulationComplete(page);

  // Open the Standard Output accordion.
  await page.locator('text=Standard Output').click();

  // Sanity: the value must contain the expected string.
  await expect(page.locator('#stdout-view')).toContainText('being tested!');

  // Visibility: read computed styles and assert the text is NOT effectively
  // white (i.e. invisible on the light-theme background).
  // When the bug is present (OS dark, app forced light, no theme-aware fix),
  // the CSS media-query fires `textarea { color: white }`, making text
  // invisible.  The fix applies inline MUI palette colours that win over the
  // stylesheet, giving dark text (~luminance 0) in the light theme.
  const styles = await page.locator('#stdout-view').evaluate((el) => {
    const s = getComputedStyle(el);
    return { color: s.color, background: s.backgroundColor };
  });

  // Parse rgb(r, g, b[, a]) → perceived luminance (0 = black, 255 = white).
  function parseBrightness(cssColor) {
    const m = cssColor.match(/\d+/g);
    if (!m || m.length < 3) return null;
    const [r, g, b] = m.map(Number);
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
  }

  const textBrightness = parseBrightness(styles.color);

  // The text must NOT be near-white (luminance ≈ 255 means invisible on a
  // light background).  Dark text in the MUI light theme palette is
  // rgba(0,0,0,0.87) → luminance < 50.  We require strictly < 150 to catch
  // any near-white colour while leaving room for mid-grey values.
  expect(textBrightness).toBeLessThan(150);
});

