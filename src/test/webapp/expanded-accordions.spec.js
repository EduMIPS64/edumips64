const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

const STORAGE_PREFIX = 'edumips64:v1:';
const EXPANDED_KEY = `${STORAGE_PREFIX}expandedAccordions`;

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
 * The MUI AccordionSummary renders as a `<button role="button">` whose
 * accessible name is the panel title. The button carries `aria-expanded`,
 * which is the source of truth for whether the accordion is open. We always
 * locate panels by role+name and assert `aria-expanded`, never by reading
 * the wrapping div's class list, so the tests survive MUI implementation
 * changes.
 */
function panelButton(page, name) {
  return page.getByRole('button', { name }).first();
}

async function expectExpanded(page, name, expanded) {
  const btn = panelButton(page, name);
  await btn.waitFor({ state: 'visible' });
  await expect(btn).toHaveAttribute(
    'aria-expanded',
    expanded ? 'true' : 'false',
  );
}

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

  await expectExpanded(page, /^Stats/, true);
  await expectExpanded(page, /^Pipeline/, true);
  await expectExpanded(page, /^Registers/, true);
  await expectExpanded(page, /^Memory/, false);
  await expectExpanded(page, /^Standard Output/, false);
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
  await panelButton(page, /^Pipeline/).click();
  await expectExpanded(page, /^Pipeline/, false);

  await panelButton(page, /^Memory/).click();
  await expectExpanded(page, /^Memory/, true);

  // Verify localStorage actually got written. The shape comes from
  // `SETTINGS_SCHEMA[EXPANDED_ACCORDIONS]` in `src/webapp/settings/schema.js`.
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

  await expectExpanded(page, /^Pipeline/, false);
  await expectExpanded(page, /^Memory/, true);
  // Untouched panels keep their defaults.
  await expectExpanded(page, /^Stats/, true);
  await expectExpanded(page, /^Registers/, true);
});
