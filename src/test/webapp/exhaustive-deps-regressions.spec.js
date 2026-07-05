const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  runToCompletion,
  openSettingsDialog,
  closeSettingsDialog,
} = require('./test-utils');

const STORAGE_PREFIX = 'edumips64:v1:';

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

async function setEditorContent(page, code) {
  await removeOverlay(page);
  const inputArea = page.locator('.monaco-editor textarea.inputarea');
  await inputArea.click({ force: true });
  await page.keyboard.press('ControlOrMeta+a');
  await page.keyboard.press('Backspace');
  await page.keyboard.insertText(code);
}

/**
 * Toggle "Editor Vi Mode" to the desired state via the (modal) Settings
 * dialog's UI tab, then close the dialog so the editor is reachable again.
 */
async function setViMode(page, enabled) {
  await openSettingsDialog(page, 'UI');
  const viModeSwitch = page.getByLabel('Editor Vi Mode');
  if ((await viModeSwitch.isChecked()) !== enabled) {
    await viModeSwitch.click();
  }
  await expect(viModeSwitch).toBeChecked({ checked: enabled });
  await closeSettingsDialog(page);
}

/**
 * Set cache configuration values via the Settings dialog's Cache tab, then
 * close the (modal) dialog.
 */
async function setCacheConfig(page, cacheType, config) {
  await openSettingsDialog(page, 'Simulation');
  const cacheSection = page.locator(`text=${cacheType}`).locator('..');

  if (config.size !== undefined) {
    await cacheSection
      .locator('input[type="number"]')
      .nth(0)
      .fill(String(config.size));
  }
  if (config.blockSize !== undefined) {
    await cacheSection
      .locator('input[type="number"]')
      .nth(1)
      .fill(String(config.blockSize));
  }
  if (config.associativity !== undefined) {
    await cacheSection
      .locator('input[type="number"]')
      .nth(2)
      .fill(String(config.associativity));
  }

  await closeSettingsDialog(page);
}

async function getCacheStats(page) {
  const l1iReadsCell = page.locator('#stat-l1i-reads');
  await expect(l1iReadsCell).not.toHaveText('0', { timeout: 10000 });

  return {
    l1iMisses: parseInt(
      (await page.locator('#stat-l1i-misses').textContent()) || '0',
      10,
    ),
    l1dWriteMisses: parseInt(
      (await page.locator('#stat-l1d-write-misses').textContent()) || '0',
      10,
    ),
  };
}

test('syntax errors update Monaco error markers', async ({ page }) => {
  await waitForPageReady(page);

  await setEditorContent(page, '.code\nTHIS_IS_NOT_AN_INSTRUCTION r1\n');

  const marker = await page.waitForFunction(
    () => {
      const model = window.editor && window.editor.getModel();
      if (!window.monaco || !model) return null;
      return window.monaco.editor
        .getModelMarkers({ resource: model.uri })
        .find(
          (m) =>
            m.source === 'EduMIPS64' &&
            m.severity === window.monaco.MarkerSeverity.Error,
        );
    },
    null,
    { timeout: 10000 },
  );

  expect(await marker.jsonValue()).toMatchObject({
    startLineNumber: 2,
    source: 'EduMIPS64',
  });
});

test('Vim mode can be enabled and disabled after the editor mounts', async ({
  page,
}) => {
  await waitForPageReady(page);
  await setEditorContent(page, '.code\nSYSCALL 0\n');

  // Enable Vi Mode (dialog closes afterwards, so the editor is reachable).
  await setViMode(page, true);

  await page.evaluate(() => {
    window.editor.setPosition({ lineNumber: 1, column: 1 });
    window.editor.focus();
  });
  await page.keyboard.press('j');

  await expect
    .poll(() => page.evaluate(() => window.editor.getPosition().lineNumber))
    .toBe(2);
  await expect
    .poll(() => page.evaluate(() => window.editor.getValue()))
    .toBe('.code\nSYSCALL 0\n');

  // Disable Vi Mode again.
  await setViMode(page, false);

  await page.evaluate(() => {
    window.editor.setPosition({ lineNumber: 1, column: 1 });
    window.editor.focus();
  });
  await page.keyboard.press('j');

  await expect
    .poll(() => page.evaluate(() => window.editor.getValue()))
    .toBe('j.code\nSYSCALL 0\n');
});

test('cache config changes synchronize before the next run', async ({
  page,
}) => {
  await waitForPageReady(page);

  const program = `.data
arr: .space 64

.code
DADDI r10, r0, arr
DADDI r1, r0, 1
SD r1, 0(r10)
SD r1, 8(r10)
SD r1, 16(r10)
SD r1, 24(r10)
SD r1, 32(r10)
SD r1, 40(r10)
SD r1, 48(r10)
SD r1, 56(r10)
SYSCALL 0
`;

  await setCacheConfig(page, 'L1 Instruction Cache', {
    size: 1024,
    blockSize: 8,
    associativity: 1,
  });
  await setCacheConfig(page, 'L1 Data Cache', {
    size: 1024,
    blockSize: 8,
    associativity: 1,
  });
  await loadProgram(page, program);
  await runToCompletion(page);
  const smallBlockStats = await getCacheStats(page);

  await setCacheConfig(page, 'L1 Instruction Cache', {
    size: 1024,
    blockSize: 64,
    associativity: 1,
  });
  await setCacheConfig(page, 'L1 Data Cache', {
    size: 1024,
    blockSize: 64,
    associativity: 1,
  });
  await loadProgram(page, program);
  await runToCompletion(page);
  const largeBlockStats = await getCacheStats(page);

  expect(largeBlockStats.l1iMisses).toBeLessThanOrEqual(
    smallBlockStats.l1iMisses,
  );
  expect(largeBlockStats.l1dWriteMisses).toBeLessThan(
    smallBlockStats.l1dWriteMisses,
  );
});
