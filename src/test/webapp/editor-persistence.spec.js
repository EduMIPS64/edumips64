const { test, expect } = require('./fixtures');
const { targetUri, removeOverlay, waitForPageReady } = require('./test-utils');

const STORAGE_PREFIX = 'edumips64:v1:';
const EDITOR_CODE_KEY = `${STORAGE_PREFIX}editorCode`;

/**
 * Clear all edumips64 localStorage keys before each test to ensure hermeticity.
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
 * Helper: read the current Monaco editor contents.
 */
async function getEditorContent(page) {
  return page.evaluate(() => {
    const model = window.monaco.editor.getModels()[0];
    return model ? model.getValue() : '';
  });
}

/**
 * Test: Editor contents are persisted to localStorage and restored after a
 * page reload. This is the core requirement of the issue: an accidental
 * reload should no longer wipe a user's in-progress program.
 */
test('editor contents persist across page reload', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  const userProgram = `.data
mynum: .word 7

.code
LD r1, mynum(r0)
DADDI r2, r1, 35
SYSCALL 0
`;

  // Type the user's program into the editor.
  const inputArea = page.locator('.monaco-editor textarea.inputarea');
  await inputArea.click({ force: true });
  await page.keyboard.press('ControlOrMeta+a');
  await page.keyboard.press('Backspace');
  await page.keyboard.insertText(userProgram);

  // Wait until the persisted localStorage entry catches up with the typed
  // value, so we know the write committed before we reload.
  await page.waitForFunction(
    ({ key, expected }) => {
      const raw = window.localStorage.getItem(key);
      if (raw === null) return false;
      try {
        return JSON.parse(raw) === expected;
      } catch {
        return false;
      }
    },
    { key: EDITOR_CODE_KEY, expected: userProgram },
    { timeout: 5000 },
  );

  // Reload and verify the editor restores the user's program (not the bundled
  // sample).
  await page.reload();
  await waitForPageReady(page);

  const restored = await getEditorContent(page);
  expect(restored).toBe(userProgram);

  await page.close();
});

/**
 * Test: The "Restore default sample" button replaces the editor contents
 * with the bundled sample program and resets the simulator state.
 */
test('restore default sample button restores the bundled sample', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // Capture the bundled sample (whatever the editor displays on a fresh load
  // when no localStorage value is set).
  const defaultSample = await getEditorContent(page);
  expect(defaultSample.length).toBeGreaterThan(0);

  // Replace the editor with a different program.
  const inputArea = page.locator('.monaco-editor textarea.inputarea');
  await inputArea.click({ force: true });
  await page.keyboard.press('ControlOrMeta+a');
  await page.keyboard.press('Backspace');
  await page.keyboard.insertText('.code\nSYSCALL 0\n');

  await page.waitForFunction(() => {
    const model = window.monaco.editor.getModels()[0];
    return model && model.getValue().includes('SYSCALL 0') &&
      !model.getValue().includes('EduMIPS64 Web test program');
  }, null, { timeout: 5000 });

  // Click "Restore default sample".
  await removeOverlay(page);
  await page.click('#restore-sample-button');

  // The editor should now match the original bundled sample again.
  await page.waitForFunction(
    (expected) => {
      const model = window.monaco.editor.getModels()[0];
      return model && model.getValue() === expected;
    },
    defaultSample,
    { timeout: 5000 },
  );

  const afterRestore = await getEditorContent(page);
  expect(afterRestore).toBe(defaultSample);

  // localStorage should also be reset to the default (i.e. either removed or
  // holding the default sample) so a subsequent reload picks up the sample.
  await page.reload();
  await waitForPageReady(page);
  const afterReload = await getEditorContent(page);
  expect(afterReload).toBe(defaultSample);

  await page.close();
});
