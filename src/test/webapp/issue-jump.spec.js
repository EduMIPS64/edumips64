const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay } = require('./test-utils');

/**
 * Tests for the "click an Issues panel entry to jump the editor to the
 * offending line" feature.
 *
 * The default sample is clean, so the test types a program that triggers a
 * known parser error on a specific line. Once the issue surfaces in the
 * Issues panel, clicking it must:
 *   - reveal the offending line in the Monaco editor,
 *   - place the cursor at the reported (row, column),
 *   - move keyboard focus to the editor.
 */

// A minimal program with a deliberate syntax error several lines deep so we
// can verify the editor truly jumps to the reported line.
const BROKEN_PROGRAM = `; Program with a deliberate syntax error.
.data
.code
\tdaddi\tr1, r0, 1
\tdaddi\tr2, r0, 2
\tdaddi\tr3, r0, 3
\tnotARealInstruction r4, r0, 4
\tsyscall\t0
`;

async function typeProgram(page, program) {
  await removeOverlay(page);
  const inputArea = page.locator('.monaco-editor textarea.inputarea');
  await inputArea.click({ force: true });
  await page.keyboard.press('ControlOrMeta+a');
  await page.keyboard.press('Backspace');
  await page.keyboard.insertText(program);
}

test('clicking an Issues entry jumps the editor to that line', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await typeProgram(page, BROKEN_PROGRAM);

  // Wait for the parsing error to show up in the Issues panel.
  const issueButton = page.locator('.error-list-item-button').first();
  await expect(issueButton).toBeVisible({ timeout: 10000 });

  // Read the row reported in the issue label so the assertion is robust to
  // small changes in the parser's diagnostics.
  const label = await issueButton.innerText();
  const match = label.match(/Line\s+(\d+)\s+Position\s+(\d+)/i);
  expect(match, `Issue label "${label}" should expose Line/Position`).not.toBeNull();
  const expectedLine = Number(match[1]);
  const expectedColumn = Number(match[2]);

  // Move the cursor far away from the offending line so we can detect the
  // jump unambiguously.
  await page.evaluate(() => {
    window.editor.setPosition({ lineNumber: 1, column: 1 });
  });

  await issueButton.click();

  // After the click the editor's cursor must be at (expectedLine, expectedColumn).
  await expect.poll(async () => {
    return page.evaluate(() => {
      const pos = window.editor.getPosition();
      return pos ? { line: pos.lineNumber, column: pos.column } : null;
    });
  }, { timeout: 5000 }).toEqual({ line: expectedLine, column: expectedColumn });

  // The offending line must be within the editor's currently visible range,
  // i.e. revealLineInCenter actually scrolled the viewport.
  const visible = await page.evaluate(() => {
    const ranges = window.editor.getVisibleRanges();
    return ranges.map((r) => [r.startLineNumber, r.endLineNumber]);
  });
  const isVisible = visible.some(
    ([start, end]) => expectedLine >= start && expectedLine <= end,
  );
  expect(isVisible, `Line ${expectedLine} should be visible after click; visible ranges: ${JSON.stringify(visible)}`).toBe(true);
});
