const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  openProgramMenu,
  runToCompletion,
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
 *   - a program can actually be loaded via touch, exposing the run toolbar,
 *   - after fully running a small program, the simulator state (registers and
 *     memory) can be inspected via the mobile UI and is semantically correct.
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

// A program that writes a known value to both a register and a memory cell so
// the test can verify simulator state is inspectable from the mobile UI.
//   r1 <- 5
//   r2 <- r1 + 10 = 15 (0xF)
//   memory at `result` <- r2 = 15
const MOBILE_STATE_PROGRAM = `.data
result: .word 0

.code
  daddi r1, r0, 5
  daddi r2, r1, 10
  sd r2, result(r0)
  syscall 0
`;

/**
 * Read a register's value from the Registers table by its name (e.g. "R1").
 *
 * The Registers table (id="registers") renders each register as three
 * consecutive cells: name, alias, hex value. The name → alias → value ordering
 * is identical for GPR/FPU/special registers, so walking the sibling cells
 * works uniformly. The hex string has no "0x" prefix (e.g.
 * "0000000000000005"), so we parse it as base-16 and return a Number.
 */
async function readRegister(page, name) {
  const hex = await page.evaluate((regName) => {
    const cells = Array.from(
      document.querySelectorAll('#registers td.registerName'),
    );
    for (const cell of cells) {
      if (cell.textContent.trim() === regName) {
        const aliasCell = cell.nextElementSibling;
        const valueCell = aliasCell ? aliasCell.nextElementSibling : null;
        return valueCell ? valueCell.textContent.trim() : null;
      }
    }
    return null;
  }, name);
  expect(hex, `register ${name} should be present in the table`).not.toBeNull();
  return parseInt(hex, 16);
}

/**
 * Read a memory cell's value from the Memory table by its label (e.g.
 * "result"). The Memory table (id="memory") renders each cell as a row of
 * Address, Value, Label, Code, Comment; the label lives in `td.elementLabel`
 * and the value is the row's second cell. The hex value has no "0x" prefix, so
 * we parse it as base-16 and return a Number.
 */
async function readMemoryByLabel(page, label) {
  const hex = await page.evaluate((cellLabel) => {
    const labelCells = Array.from(
      document.querySelectorAll('#memory td.elementLabel'),
    );
    for (const labelCell of labelCells) {
      if (labelCell.textContent.trim() === cellLabel) {
        const row = labelCell.closest('tr');
        const valueCell = row ? row.querySelectorAll('td')[1] : null;
        return valueCell ? valueCell.textContent.trim() : null;
      }
    }
    return null;
  }, label);
  expect(
    hex,
    `memory cell labelled ${label} should be present in the table`,
  ).not.toBeNull();
  return parseInt(hex, 16);
}

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
  await expectReachable(page, '#settings-button');
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

test('simulator state (registers and memory) is inspectable from a mobile viewport', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // Load and fully run a small program that writes a known value to both a
  // register and a memory cell.
  await loadProgram(page, MOBILE_STATE_PROGRAM);
  await runToCompletion(page);

  // Inspect the registers via the mobile UI. The Registers accordion is
  // expanded by default, so its table is rendered on a phone. The table is
  // taller than the viewport, so we assert it is visible (rendered, not
  // hidden) and scroll the value we care about into view rather than
  // requiring the whole table to fit on screen.
  const registersTable = page.locator('#registers');
  await expect(registersTable).toBeVisible();
  await registersTable.scrollIntoViewIfNeeded();
  expect(await readRegister(page, 'R1')).toBe(5);
  expect(await readRegister(page, 'R2')).toBe(15);

  // Memory is always visible in the DashboardCard layout; wait for the table.
  await page.waitForSelector('#memory tbody tr', { timeout: 5000 });
  const memoryTable = page.locator('#memory');
  await expect(memoryTable).toBeVisible();
  await memoryTable.scrollIntoViewIfNeeded();

  // The `result` cell must hold the value the program stored there (15 = 0xF).
  expect(await readMemoryByLabel(page, 'result')).toBe(15);
});
