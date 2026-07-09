const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

const STORAGE_PREFIX = 'edumips64:v1:';
const EXPANDED_KEY = `${STORAGE_PREFIX}expandedAccordions`;

/**
 * Tests for the DashboardCard collapse/expand behavior.
 *
 * Each dashboard panel (Stats, Pipeline, Registers, Memory, Standard Output)
 * is a `DashboardCard` whose header is a real `<button>` carrying
 * `aria-expanded` and an `aria-label` of the form "Collapse <Title>" /
 * "Expand <Title>" (see `DashboardCard.tsx`). Clicking it (or activating it
 * via keyboard) collapses the card body with an MUI `<Collapse>` while
 * leaving the header visible, so we always locate panels by role+name and
 * assert `aria-expanded`, never by reading the wrapping div's class list —
 * that way the tests survive styling changes.
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
      if (k && k.startsWith(prefix)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => window.localStorage.removeItem(k));
  }, STORAGE_PREFIX);
});

/**
 * The header toggle's accessible name changes with its state ("Collapse X"
 * when expanded, "Expand X" when collapsed), so match on the title alone.
 */
function panelToggle(page, title) {
  return page.getByRole('button', { name: new RegExp(`^(Collapse|Expand) ${title}$`) });
}

async function expectExpanded(page, title, expanded) {
  const btn = panelToggle(page, title);
  await btn.waitFor({ state: 'visible' });
  await expect(btn).toHaveAttribute('aria-expanded', expanded ? 'true' : 'false');
  await expect(btn).toHaveAccessibleName(expanded ? `Collapse ${title}` : `Expand ${title}`);
}

/**
 * Verify that all dashboard panel headers are present and visible on page
 * load, regardless of whether their body is currently expanded or collapsed.
 */
test('all simulator panels are visible on load', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await expect(page.locator('#stats-card')).toBeVisible();
  await expect(page.locator('#pipeline-card')).toBeVisible();
  await expect(page.locator('#registers-card')).toBeVisible();
  await expect(page.locator('#memory-card')).toBeVisible();
  await expect(page.locator('#stdout-card')).toBeVisible();
});

/**
 * Issue #1697 — defaults reflect the simulator's primary purpose:
 * Stats / Pipeline / Registers expanded; Memory / Standard Output
 * collapsed. Cache configuration and general settings used to be collapsible
 * panels here too, but now live in the Settings dialog (opened via the gear
 * button), so they are no longer part of this expand/collapse model.
 */
test('default panel expansion highlights pipeline and registers', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await expectExpanded(page, 'Stats', true);
  await expectExpanded(page, 'Pipeline', true);
  await expectExpanded(page, 'Registers', true);
  await expectExpanded(page, 'Memory', false);
  await expectExpanded(page, 'Standard Output', false);
});

/**
 * A collapsed card is just its header: verify the body is actually gone from
 * the accessibility/layout tree (not merely styled to look collapsed).
 */
test('collapsing a panel hides its body', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await expect(page.locator('#stats-card')).toBeVisible();

  await panelToggle(page, 'Stats').click();
  await expectExpanded(page, 'Stats', false);

  // Statistics content lives inside the Stats card; once collapsed it must
  // no longer be rendered.
  await expect(page.locator('#stat-cycles')).toHaveCount(0);

  // Expanding it again brings the content back.
  await panelToggle(page, 'Stats').click();
  await expectExpanded(page, 'Stats', true);
  await expect(page.locator('#stat-cycles')).toBeVisible();
});

/**
 * Issue #1697 (persistence half) — every user expand/collapse is written to
 * localStorage, and the layout the user left is restored verbatim on the
 * next visit. We toggle three panels in different directions to make sure
 * we're not accidentally testing "everything goes to one default value".
 */
test('panel expansion is persisted and restored across reloads', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  // Collapse Pipeline (default: open) and expand Memory (default: closed).
  // These two changes should round-trip through localStorage.
  await panelToggle(page, 'Pipeline').click();
  await expectExpanded(page, 'Pipeline', false);

  await panelToggle(page, 'Memory').click();
  await expectExpanded(page, 'Memory', true);

  // Verify localStorage actually got written. The shape comes from
  // `SETTINGS_SCHEMA[EXPANDED_ACCORDIONS]` in `src/webapp/settings/schema.ts`.
  const stored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    EXPANDED_KEY,
  );
  expect(stored).not.toBeNull();
  const parsed = JSON.parse(stored);
  expect(parsed.pipeline).toBe(false);
  expect(parsed.memory).toBe(true);

  // Reload and confirm the toggled state is restored, not the defaults.
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await expectExpanded(page, 'Pipeline', false);
  await expectExpanded(page, 'Memory', true);
  // Untouched panels keep their defaults.
  await expectExpanded(page, 'Stats', true);
  await expectExpanded(page, 'Registers', true);
});

/**
 * The toggle must be keyboard-operable: focus it and activate with the
 * keyboard (a native <button> handles Enter/Space activation for free), not
 * just via pointer click.
 */
test('panel toggle is keyboard operable', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  const toggle = panelToggle(page, 'Registers');
  await toggle.focus();
  await expect(toggle).toBeFocused();

  await page.keyboard.press('Enter');
  await expectExpanded(page, 'Registers', false);

  await page.keyboard.press('Space');
  await expectExpanded(page, 'Registers', true);
});
