const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

const STORAGE_PREFIX = 'edumips64:v1:';
const ORDER_KEY = `${STORAGE_PREFIX}widgetOrder`;

const DEFAULT_ORDER = ['stats', 'pipeline', 'registers', 'memory', 'stdout'];
const DEFAULT_CARD_IDS = DEFAULT_ORDER.map((id) => `${id}-card`);

// A wide, tall viewport so every reorderable card (including Registers,
// which sits below the fold at the default Playwright viewport) is
// actually laid out on-screen rather than merely present-but-clipped inside
// the right panel's scroll container — `boundingBox()` reports an
// element's true layout position even when it is clipped by `overflow`, so
// pointer coordinates derived from an off-screen box land on whatever
// happens to be visually rendered underneath instead of the intended
// target.
test.use({ viewport: { width: 1600, height: 1000 } });

/**
 * Tests for drag-and-drop reordering of the dashboard cards (Stats,
 * Pipeline, Registers, Memory, Standard Output) in the right panel.
 *
 * The whole card header is the pointer drag handle (see
 * `SortableDashboardCard.tsx`): grabbing a header and moving past the
 * MouseSensor's activation distance starts a drag, while a plain click
 * still toggles collapse/expand (exercised by
 * `expanded-accordions.spec.js`). Keyboard reordering goes through a
 * dedicated visually-hidden "Reorder <Title>" button next to each header:
 * focus it, Space/Enter lifts the card, arrow keys move it among its
 * siblings, and Space/Enter drops it — that's the interaction most of
 * these tests drive, since it's far more deterministic in CI than
 * simulating raw pointer-drag event sequences past dnd-kit's
 * activation-distance threshold.
 */

/**
 * Clear all edumips64 localStorage keys before each test so first-run
 * defaults are exercised, not state left over from a previous test.
 */
test.beforeEach(async ({ page }) => {
  await page.goto(targetUri);
  await page.evaluate((prefix) => {
    const keysToRemove = [];
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k?.startsWith(prefix)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => {
      window.localStorage.removeItem(k);
    });
  }, STORAGE_PREFIX);
});

/**
 * Keyboard drag-handle button locator, keyed by the card's visible title.
 * The button is visually hidden (1x1, revealed on keyboard focus) but
 * always present in the accessibility tree.
 */
function dragHandle(page, title) {
  return page.getByRole('button', { name: `Reorder ${title}` });
}

/** Header collapse-toggle button locator — also the pointer drag handle. */
function cardHeader(page, title) {
  return page.getByRole('button', {
    name: new RegExp(`^(Collapse|Expand) ${title}$`),
  });
}

/**
 * Read the current DOM order of the reorderable dashboard cards, as the
 * `id` attributes of elements inside `#dashboard` (`stats-card`,
 * `pipeline-card`, ...), in document order. Each `SortableDashboardCard`
 * wraps its `DashboardCard` in an extra (id-less) `<div>` for dnd-kit's
 * `setNodeRef`, so the card's `id` is not a *direct* child of `#dashboard`
 * — `[id]` (any descendant) with a filter to the known card ids sidesteps
 * that nesting and also excludes the (non-reorderable) `issues-card`.
 */
async function dashboardCardOrder(page) {
  const ids = await page.$$eval('#dashboard [id]', (nodes) =>
    nodes.map((n) => n.id),
  );
  return ids.filter((id) => DEFAULT_CARD_IDS.includes(id));
}

test('default dashboard card order matches the schema default', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  expect(await dashboardCardOrder(page)).toEqual(DEFAULT_CARD_IDS);
});

test('reordering a card via keyboard changes DOM order and persists across reload', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  expect(await dashboardCardOrder(page)).toEqual(DEFAULT_CARD_IDS);

  // Move "Stats" (index 0) down past "Pipeline" (index 1) using dnd-kit's
  // keyboard sensor: focus the handle, lift with Space, move down with
  // ArrowDown, drop with Space. dnd-kit only visually transforms items
  // during the drag (via CSS) and reorders the actual DOM/array on drop, and
  // each key's React state update needs a tick to land, so give the lift and
  // the move a moment to register before the next key — otherwise the drop
  // can race the lift and silently no-op.
  const statsHandle = dragHandle(page, 'Stats');
  await statsHandle.focus();
  await expect(statsHandle).toBeFocused();
  await page.keyboard.press('Space');
  await page.waitForTimeout(200);
  await page.keyboard.press('ArrowDown');
  await page.waitForTimeout(200);
  await page.keyboard.press('Space');

  const expectedOrder = [
    'pipeline-card',
    'stats-card',
    'registers-card',
    'memory-card',
    'stdout-card',
  ];
  await expect
    .poll(async () => dashboardCardOrder(page))
    .toEqual(expectedOrder);

  // The new order must be written to localStorage under the `widgetOrder`
  // setting, as the array of widget ids (not the DOM/HTML ids).
  const stored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    ORDER_KEY,
  );
  expect(stored).not.toBeNull();
  expect(JSON.parse(stored)).toEqual([
    'pipeline',
    'stats',
    'registers',
    'memory',
    'stdout',
  ]);

  // Reload and confirm the reordered layout is restored, not the defaults.
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  expect(await dashboardCardOrder(page)).toEqual(expectedOrder);
});

test('an invalid stored widget order falls back to the default order', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  // Write a garbage value directly to localStorage (wrong type entirely),
  // then reload: `sanitize()` in `settings/schema.ts` must detect the type
  // mismatch and fall back to `DEFAULT_WIDGET_ORDER` rather than crash the
  // dashboard.
  await page.evaluate((key) => {
    window.localStorage.setItem(key, JSON.stringify('not-an-array'));
  }, ORDER_KEY);

  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  expect(await dashboardCardOrder(page)).toEqual(DEFAULT_CARD_IDS);

  // Same for an array containing an unknown/duplicate id: current schema
  // validation rejects duplicates outright and falls back to the default.
  await page.evaluate((key) => {
    window.localStorage.setItem(
      key,
      JSON.stringify(['stats', 'stats', 'pipeline']),
    );
  }, ORDER_KEY);

  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  expect(await dashboardCardOrder(page)).toEqual(DEFAULT_CARD_IDS);
});

test('a stored order with an unknown id drops it and keeps known ids in place', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  // A plausible "future widget" scenario: the stored order references a
  // widget id that no longer/doesn't yet exist ("bogus-widget"), alongside a
  // valid reordering of the known ids. `sanitize()` should silently drop the
  // unknown id rather than reject the whole stored value.
  await page.evaluate((key) => {
    window.localStorage.setItem(
      key,
      JSON.stringify([
        'pipeline',
        'bogus-widget',
        'stats',
        'registers',
        'memory',
        'stdout',
      ]),
    );
  }, ORDER_KEY);

  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  expect(await dashboardCardOrder(page)).toEqual([
    'pipeline-card',
    'stats-card',
    'registers-card',
    'memory-card',
    'stdout-card',
  ]);
});

test('the drag handle does not interfere with the header collapse toggle', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  // Sanity check that both controls exist independently and clicking the
  // header (which is both the collapse toggle and the pointer drag handle)
  // still toggles: the MouseSensor's activation distance means a plain
  // click never starts a drag.
  const toggle = cardHeader(page, 'Stats');
  const handle = dragHandle(page, 'Stats');

  await expect(toggle).toBeVisible();
  await expect(handle).toBeVisible();

  await expect(toggle).toHaveAttribute('aria-expanded', 'true');
  await toggle.click();
  await expect(toggle).toHaveAttribute('aria-expanded', 'false');
  // The card must still be draggable while collapsed.
  await expect(handle).toBeVisible();
  await expect(handle).toBeEnabled();
});

test('reordering a card via mouse drag changes DOM order', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  expect(await dashboardCardOrder(page)).toEqual(DEFAULT_CARD_IDS);

  // Collapse every card first so all five headers sit within the viewport
  // regardless of how tall the expanded card bodies are (expanded cards are
  // tall enough to push the lower headers below the fold, where pointer
  // coordinates derived from `boundingBox()` land on whatever is visually
  // rendered instead of the intended target). Collapsed cards remain fully
  // draggable — that's asserted separately by the collapse-toggle test
  // above.
  for (const title of [
    'Stats',
    'Pipeline',
    'Registers',
    'Memory',
    'Standard Output',
  ]) {
    const toggle = cardHeader(page, title);
    if ((await toggle.getAttribute('aria-expanded')) === 'true') {
      await toggle.click();
    }
  }

  // The pointer drag handle is the card header itself. dnd-kit's
  // MouseSensor only starts a drag after the pointer has moved past its
  // activation distance, so a single `dragTo()` (which Playwright
  // implements as one hover-then-move) is not always reliable — drive the
  // pointer manually instead: press on the "Registers" header, move in
  // several small steps well past the activation threshold (so dnd-kit sees
  // intermediate pointermove events and can compute collisions), then
  // release near the "Stats" card (the first card in the list). Exactly
  // where `closestCenter` resolves the drop among the cards the pointer
  // passed over is an implementation detail of dnd-kit's collision
  // detection — what this test cares about is that Registers (originally
  // 3rd) ends up ahead of its original position, proving the drag actually
  // reordered the list rather than merely visually animating.
  const registersHeader = cardHeader(page, 'Registers');
  const statsCard = page.locator('#stats-card');

  const handleBox = await registersHeader.boundingBox();
  const targetBox = await statsCard.boundingBox();
  if (!handleBox || !targetBox) {
    throw new Error('Could not measure drag handle or drop target.');
  }

  const startX = handleBox.x + handleBox.width / 2;
  const startY = handleBox.y + handleBox.height / 2;
  const endX = targetBox.x + targetBox.width / 2;
  const endY = targetBox.y + targetBox.height / 2;

  await page.mouse.move(startX, startY);
  await page.mouse.down();
  // Move in several small steps, well past dnd-kit's activation distance,
  // so intermediate pointermove events are dispatched.
  const steps = 8;
  for (let i = 1; i <= steps; i++) {
    const x = startX + ((endX - startX) * i) / steps;
    const y = startY + ((endY - startY) * i) / steps;
    await page.mouse.move(x, y);
  }
  await page.mouse.up();

  // Registers (index 2 by default) must have moved earlier in the list,
  // without over-specifying exactly which slot `closestCenter` chose.
  await expect
    .poll(async () =>
      (await dashboardCardOrder(page)).indexOf('registers-card'),
    )
    .toBeLessThan(2);

  // The rest of the cards are otherwise untouched: memory and stdout keep
  // their relative order, still after registers.
  const finalOrder = await dashboardCardOrder(page);
  expect(finalOrder).toContain('registers-card');
  expect(finalOrder.indexOf('memory-card')).toBeLessThan(
    finalOrder.indexOf('stdout-card'),
  );

  // Dragging by the header must not have toggled the dragged card's
  // collapse state as a side effect (the click a browser synthesizes after
  // a same-element press/release is suppressed by `SortableDashboardCard`).
  await expect(registersHeader).toHaveAttribute('aria-expanded', 'false');
});
