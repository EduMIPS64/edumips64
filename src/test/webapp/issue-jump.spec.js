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

  // Wait for the parsing error to show up in the Issues panel, and for the
  // debounced syntax check (500 ms) to settle so the list of issues is
  // stable before we read its data attributes.
  const issueButtons = page.locator('.error-list-item-button');
  await expect(issueButtons.first()).toBeVisible({ timeout: 10000 });
  await page.waitForTimeout(800);

  // Pick the first issue button and read the (row, column) directly from
  // the data-* attributes that the component sets from the same `value`
  // that its onClick handler closes over. This guarantees our expectation
  // matches the values the click handler will receive, regardless of how
  // the parser formats the description in the visible label.
  const buttonCount = await issueButtons.count();
  expect(buttonCount).toBeGreaterThan(0);
  const targetButton = issueButtons.first();
  const issueRow = Number(await targetButton.getAttribute('data-issue-row'));
  const issueColumn = Number(await targetButton.getAttribute('data-issue-column'));
  expect(Number.isFinite(issueRow)).toBe(true);
  expect(Number.isFinite(issueColumn)).toBe(true);

  // The handler clamps the column to 1 when the parser reports column 0
  // (which happens for diagnostics that are not tied to a specific
  // character within a line). Mirror that here.
  const expectedLine = issueRow;
  const expectedColumn = Math.max(1, issueColumn || 1);

  // Move the cursor far away from the offending line so we can detect the
  // jump unambiguously.
  await page.evaluate(() => {
    window.editor.setPosition({ lineNumber: 1, column: 1 });
  });

  await targetButton.click();

  // After the click the editor's cursor must be on the line reported by
  // the issue, at the expected (clamped) column. Poll because the click's
  // effect on the Monaco view propagates asynchronously.
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
