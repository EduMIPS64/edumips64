const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  runToCompletion,
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
 *
 * A short SYSCALL 5 program (a handful of cycles) is used instead of the
 * default 5020-cycle sample to keep CI fast under the Playwright coverage
 * instrumentation, which slows JS execution enough that a long program
 * exceeds the 30 s waitForSimulationComplete timeout.
 */

// Short MIPS64 program that prints "being tested!" via SYSCALL 5 (printf).
// Mirrors the default sample's mechanism: store the format-string address at
// fs_addr, point r14 at fs_addr, then syscall 5.  Runs in ~10 cycles.
const STDOUT_PROGRAM = `.data
format_str: .asciiz "being tested!"
fs_addr:    .space 4
.code
  daddi r5, r0, format_str
  sw    r5, fs_addr(r0)
  daddi r14, r0, fs_addr
  syscall 5
  syscall 0
`;

test('stdout text is visible in light theme with dark OS', async ({ page }) => {
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

  // Load the short program and run it to completion using the CI-proven helpers.
  await loadProgram(page, STDOUT_PROGRAM);
  await runToCompletion(page);

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

/**
 * Regression test for the BROADER root cause: raw-element CSS was gated on
 * the OS `prefers-color-scheme` media query, desyncing from the user-selected
 * app theme.  The fix publishes the resolved MUI palette mode to
 * `html[data-theme]` (via useLayoutEffect in Simulator.js) and gates all
 * raw-element rules on that attribute instead.
 *
 * This test validates the OPPOSITE direction:
 *   OS = light, app theme forced to 'dark' via localStorage.
 * Before the fix, the OS-light media query would NOT apply the dark textarea
 * rule, so the textarea text would default to the base dark colour
 * (rgba(0,0,0,0.87)) — nearly invisible on the dark-theme background.
 * After the fix, `html[data-theme='dark']` fires regardless of OS and the
 * textarea text is white (luminance ≈ 255).
 */
test('stdout text is visible in dark theme with light OS', async ({ page }) => {
  // Emulate OS light preference while the app is forced to dark theme.
  await page.emulateMedia({ colorScheme: 'light' });

  await page.addInitScript(() => {
    window.localStorage.setItem('edumips64:v1:themeMode', '"dark"');
  });

  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await loadProgram(page, STDOUT_PROGRAM);
  await runToCompletion(page);

  await page.locator('text=Standard Output').click();

  await expect(page.locator('#stdout-view')).toContainText('being tested!');

  // Assert the app published the resolved palette to the DOM.
  const dataTheme = await page.evaluate(() =>
    document.documentElement.getAttribute('data-theme')
  );
  expect(dataTheme).toBe('dark');

  const styles = await page.locator('#stdout-view').evaluate((el) => {
    const s = getComputedStyle(el);
    return { color: s.color, background: s.backgroundColor };
  });

  function parseBrightness(cssColor) {
    const m = cssColor.match(/\d+/g);
    if (!m || m.length < 3) return null;
    const [r, g, b] = m.map(Number);
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
  }

  const textBrightness = parseBrightness(styles.color);

  // The text must be near-white (luminance > 150) — visible on the dark background.
  // MUI dark palette text is white (rgb(255,255,255)) → luminance = 255.
  // The StdOut.js theme-aware inline style guarantees this.
  expect(textBrightness).toBeGreaterThan(150);
});

