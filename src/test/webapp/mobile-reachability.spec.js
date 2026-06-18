const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  openProgramMenu,
} = require('./test-utils');

/**
 * Mobile-browser reachability tests (issue #588, "testing on multiple
 * browsers including mobile browsers").
 *
 * The existing suite only runs at a desktop viewport, so nothing guarantees
 * the core controls remain reachable on the small, touch-based viewports of
 * phones. These tests emulate a representative phone (Pixel 5 portrait, a
 * narrow 393×851 viewport with touch input) and assert that:
 *   - the page loads and the editor becomes ready,
 *   - the primary header controls (Load, Program ▾, Help) are reachable —
 *     visible, in the viewport, and large enough to tap,
 *   - the Program ▾ dropdown opens and its items are reachable,
 *   - a program can actually be loaded via touch, exposing the run toolbar.
 *
 * `isMobile`/`hasTouch` are Chromium-only, which matches the single Chromium
 * project configured in playwright.config.js.
 */

// Pixel 5 portrait. Hard-coded rather than pulled from Playwright's `devices`
// registry so the test does not depend on a specific device staying in that
// registry across Playwright upgrades.
const MOBILE_VIEWPORT = { width: 393, height: 851 };

// Minimum height (px) we treat as a tappable target on touch devices.
const MIN_TAP_TARGET_PX = 24;

// Fraction of an element that must fall inside the viewport for it to count as
// reachable. Below 1.0 to tolerate the sub-pixel overflow MUI popup surfaces
// can exhibit while remaining fully usable.
const MIN_IN_VIEWPORT_RATIO = 0.9;

test.use({
  viewport: MOBILE_VIEWPORT,
  isMobile: true,
  hasTouch: true,
  userAgent:
    'Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 ' +
    '(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36',
});

// A short, deterministic program so the load path can be exercised quickly.
const MOBILE_PROGRAM = `.code
  daddi r1, r0, 1
  syscall 0
`;

/**
 * Assert that an element is reachable on a touch device: visible, actually
 * rendered inside the visible viewport, and at least 24px tall (a
 * conservative lower bound for a tappable target).
 *
 * `toBeInViewport` confirms the element intersects the viewport rectangle, so
 * it catches controls pushed off-screen by horizontal overflow without
 * depending on brittle sub-pixel width arithmetic (MUI popup surfaces can
 * extend a fraction of a pixel past the viewport edge while remaining fully
 * usable).
 */
async function expectReachable(page, selector) {
  const locator = page.locator(selector);
  await expect(locator, `${selector} should be visible`).toBeVisible();
  await expect(
    locator,
    `${selector} should be within the viewport`,
  ).toBeInViewport({ ratio: MIN_IN_VIEWPORT_RATIO });

  const box = await locator.boundingBox();
  expect(box, `${selector} should have a bounding box`).not.toBeNull();

  // The control must start within the viewport (not clipped off the left edge)
  // and be tall enough to tap.
  expect(box.x).toBeGreaterThanOrEqual(0);
  expect(box.height).toBeGreaterThanOrEqual(MIN_TAP_TARGET_PX);
}

test('core header controls are reachable on a mobile viewport', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // The editor itself must be present on the small viewport.
  await expect(page.locator('.monaco-editor')).toBeVisible();

  // Primary header controls a user needs to drive the simulator.
  await expectReachable(page, '#load-button');
  await expectReachable(page, '#program-menu-button');
  await expectReachable(page, '#help-button');
});

test('the Program menu opens and its items are reachable on a mobile viewport', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await openProgramMenu(page);

  // Every Program-menu item must be reachable once the menu is open.
  await expectReachable(page, '#clear-code-button');
  await expectReachable(page, '#load-code-button');
  await expectReachable(page, '#save-code-button');
  await expectReachable(page, '#restore-sample-button');
});

test('a program can be loaded and the run toolbar is reachable on a mobile viewport', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // loadProgram drives the editor + Load button; succeeding here proves the
  // load path is reachable on a phone-sized, touch-enabled viewport.
  await loadProgram(page, MOBILE_PROGRAM);

  // After loading, the single-step control of the run toolbar must be reachable.
  await expectReachable(page, '#step-button');
});
