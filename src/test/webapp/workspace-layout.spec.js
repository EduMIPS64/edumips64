const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
} = require('./test-utils');

/**
 * Workspace-layout tests: the resizable / collapsible three-region shell
 * (code editor + right widgets column in the upper region, full-width Cycles
 * diagram at the bottom).
 *
 * These exercise the collapse toggles, the drag-to-resize handles, and the
 * persistence of the chosen geometry across reloads. Geometry is read from
 * the live bounding boxes of #left-panel / #right-panel / #bottom-panel.
 */

const STORAGE_PREFIX = 'edumips64:v1:';

async function clearSettings(page) {
  await page.evaluate((prefix) => {
    const keys = [];
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k && k.startsWith(prefix)) keys.push(k);
    }
    keys.forEach((k) => window.localStorage.removeItem(k));
  }, STORAGE_PREFIX);
}

async function box(page, selector) {
  return page.locator(selector).boundingBox();
}

test.beforeEach(async ({ page }) => {
  await page.goto(targetUri);
  await clearSettings(page);
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);
});

test.afterEach(async ({ page }) => {
  await clearSettings(page);
});

test('the three regions are laid out: widgets on the right, cycles across the bottom', async ({
  page,
}) => {
  const left = await box(page, '#left-panel');
  const right = await box(page, '#right-panel');
  const bottom = await box(page, '#bottom-panel');

  // Right column sits to the right of the code editor, at the same height.
  expect(right.x).toBeGreaterThan(left.x + left.width / 2);
  // Bottom region spans (roughly) the full width — wider than either upper
  // column — and sits below them.
  expect(bottom.width).toBeGreaterThan(right.width + 50);
  expect(bottom.y).toBeGreaterThan(left.y + left.height - 5);
});

test('collapsing the right panel widens the editor and hides the widgets', async ({
  page,
}) => {
  const leftBefore = await box(page, '#left-panel');

  await page.getByTestId('collapse-right').click();

  // The widgets column is gone; an expand affordance remains.
  await expect(page.locator('#right-panel')).toHaveCount(0);
  await expect(page.getByTestId('expand-right')).toBeVisible();

  // The editor took over the freed width.
  const leftAfter = await box(page, '#left-panel');
  expect(leftAfter.width).toBeGreaterThan(leftBefore.width);

  // Expanding restores the widgets column.
  await page.getByTestId('expand-right').click();
  await expect(page.locator('#right-panel')).toBeVisible();
});

test('collapsing the bottom region gives the upper region more height', async ({
  page,
}) => {
  const upperBefore = await box(page, '#left-panel');

  await page.getByTestId('toggle-bottom').click();

  // The Cycles grid is hidden, but the region header (with the expand toggle)
  // stays, and the upper region grew taller.
  await expect(page.getByTestId('cycles-widget')).toHaveCount(0);
  const upperAfter = await box(page, '#left-panel');
  expect(upperAfter.height).toBeGreaterThan(upperBefore.height);

  // Expanding brings the diagram back.
  await page.getByTestId('toggle-bottom').click();
  await expect(page.getByTestId('cycles-widget')).toBeVisible();
});

test('dragging the vertical handle resizes the widgets column and persists', async ({
  page,
}) => {
  const before = await box(page, '#right-panel');
  const handle = page.getByTestId('resize-vertical');
  const hb = await handle.boundingBox();

  // Drag the handle left by ~150px to widen the right column.
  await page.mouse.move(hb.x + hb.width / 2, hb.y + hb.height / 2);
  await page.mouse.down();
  await page.mouse.move(hb.x - 150, hb.y + hb.height / 2, { steps: 10 });
  await page.mouse.up();

  const after = await box(page, '#right-panel');
  expect(after.width).toBeGreaterThan(before.width + 50);

  // The new width survives a reload (persisted to localStorage).
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);
  const afterReload = await box(page, '#right-panel');
  expect(Math.abs(afterReload.width - after.width)).toBeLessThan(20);
});

test('the Cycles region keeps its header on a narrow (stacked) viewport', async ({
  page,
}) => {
  await page.setViewportSize({ width: 500, height: 900 });
  // The stacked layout still labels the Cycles region with its header bar
  // (title + collapse toggle), matching the other stacked widgets.
  const bottom = page.locator('#bottom-panel');
  await bottom.scrollIntoViewIfNeeded();
  await expect(bottom.getByText('Cycles')).toBeVisible();
  await expect(page.getByTestId('toggle-bottom')).toBeVisible();
});

test('the widgets column cannot be dragged below its minimum width', async ({
  page,
}) => {
  const handle = page.getByTestId('resize-vertical');
  const hb = await handle.boundingBox();
  const grid = await box(page, '#main-grid');

  // Drag the handle far to the right, past the edge, trying to shrink the
  // right column to nothing.
  await page.mouse.move(hb.x + hb.width / 2, hb.y + hb.height / 2);
  await page.mouse.down();
  await page.mouse.move(grid.x + grid.width, hb.y + hb.height / 2, {
    steps: 10,
  });
  await page.mouse.up();

  // It clamps at the 20% minimum, so the column keeps a usable width.
  const right = await box(page, '#right-panel');
  expect(right.width).toBeGreaterThan(grid.width * 0.18);
});
