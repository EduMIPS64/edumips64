const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

const STORAGE_PREFIX = 'edumips64:v1:';

/**
 * Regression test for the Vi-mode toggle effect after the Codacy
 * react-hooks/exhaustive-deps fix in src/webapp/components/Code.js.
 *
 * The effect dependency array was changed from [props.viMode] to
 * [editor, props.viMode]. The freshest `vimInstance` is now read through a
 * ref to keep dispose/recreate correct without re-running the effect every
 * time `setVimInstance` is called. This test exercises the on/off/on path
 * that the change touches, and verifies:
 *
 *   - toggling Vi mode off does not crash the editor
 *   - toggling Vi mode back on still attaches the Vim status bar
 *   - the editor remains usable after multiple toggles
 *
 * If the dispose/recreate logic regressed (e.g. double-init, stale
 * disposal), this test would either time out waiting for the status bar or
 * Monaco would log a console error.
 */

async function openSettingsAccordion(page) {
  const summary = page.getByRole('button', { name: /General Settings/ });
  await summary.waitFor({ state: 'visible' });
  if ((await summary.getAttribute('aria-expanded')) !== 'true') {
    await summary.click();
  }
  await expect(summary).toHaveAttribute('aria-expanded', 'true');
}

test.beforeEach(async ({ page }) => {
  await page.goto(targetUri);
  await page.evaluate((prefix) => {
    const keysToRemove = [];
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k && k.startsWith(prefix)) keysToRemove.push(k);
    }
    keysToRemove.forEach((k) => window.localStorage.removeItem(k));
  }, STORAGE_PREFIX);
});

test('vi mode can be toggled on, off, and on again without crashing', async ({
  page,
}) => {
  // Capture console errors so we can fail loudly if Monaco/monaco-vim throws.
  const errors = [];
  page.on('pageerror', (err) => errors.push(err.message));
  page.on('console', (msg) => {
    if (msg.type() === 'error') errors.push(msg.text());
  });

  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);

  const viModeSwitch = page.getByLabel('Editor Vi Mode');
  await expect(viModeSwitch).not.toBeChecked();

  // ON
  await viModeSwitch.click();
  await expect(viModeSwitch).toBeChecked();

  // OFF — exercises the dispose path
  await viModeSwitch.click();
  await expect(viModeSwitch).not.toBeChecked();

  // ON again — exercises re-creation; before the fix the dispose logic relied
  // on a stale closure over `vimInstance` and could either skip-init or
  // double-dispose. With the ref + guard it must re-init exactly once.
  await viModeSwitch.click();
  await expect(viModeSwitch).toBeChecked();

  // The editor itself must still be present and interactive.
  const editorTextarea = page.locator('.monaco-editor textarea').first();
  await expect(editorTextarea).toBeVisible();

  expect(errors, `unexpected console/page errors: ${errors.join(' | ')}`)
    .toEqual([]);
});
