const { test, expect } = require('./fixtures');
const { targetUri, waitForPageReady, removeOverlay, loadProgram } = require('./test-utils');

/**
 * Tests for the behavior of deprecated-instruction warnings.
 *
 * Regression tests for the original "Deprecated Instructions weird behaviours"
 * report:
 *   1. A program containing a deprecated WinMIPS64 instruction (e.g. `bnez`)
 *      must be flagged as a warning by the syntax checker.
 *   2. After loading code that contains only warnings (no errors), the
 *      hover provider in the editor must still show the per-instruction
 *      metadata tooltip (Address/OpCode/Binary/Hex/CPU Stage).
 *
 * Note: the default sample program intentionally uses the canonical
 * `bne … r0` form (instead of `bnez`) so that first-time visitors land
 * on a clean Issues panel. These tests therefore inject their own
 * deprecated snippet rather than relying on the default sample.
 */

const DEPRECATED_PROGRAM = `; Minimal program exercising a deprecated WinMIPS64 instruction.
.data
.code
\tdaddi\tr10, r0, 3
loop:
\tdaddi\tr10, r10, -1
\tbnez\tr10, loop
\tsyscall\t0
`;

test('deprecated instruction is flagged by the syntax checker', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // Replace the default sample with a program that uses `bnez` (a deprecated
  // WinMIPS64 alias). The syntax checker should raise a warning, but the
  // program must still be loadable (no errors).
  await loadProgram(page, DEPRECATED_PROGRAM);

  // The Issues accordion should show the deprecated-instruction warning.
  const warningItem = page.locator('.error-list-item').first();
  await expect(warningItem).toBeVisible({ timeout: 10000 });

  // And the warning chip in the Issues header should show "1".
  await expect(page.getByRole('heading', { name: /Issues\s*1/ })).toBeVisible();
});

test('hover tooltip works after loading code with a warning', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // Load a program that contains a deprecated-instruction warning. If
  // `parsedInstructions` was wrongly cleared because the program has
  // warnings, the hover provider would return nothing and no tooltip would
  // be rendered.
  await loadProgram(page, DEPRECATED_PROGRAM);

  // Trigger the Monaco hover on the first instruction line of the program
  // (`daddi r10, r0, 3`).
  const hoverContents = await page.evaluate(async () => {
    const { editor } = window;
    const model = editor.getModel();
    // Find the first non-blank line in `.code` (the daddi above).
    let target = 1;
    for (let i = 1; i <= model.getLineCount(); i++) {
      const text = model.getLineContent(i).trim();
      if (text.startsWith('daddi')) { target = i; break; }
    }
    editor.setPosition({ lineNumber: target, column: 5 });
    editor.focus();
    editor.trigger('test', 'editor.action.showHover', {});
    await new Promise((r) => setTimeout(r, 1000));
    const nodes = document.querySelectorAll('.monaco-hover .hover-contents');
    return Array.from(nodes).map((e) => e.innerText).join('\n');
  });

  expect(hoverContents).toContain('Address');
  expect(hoverContents).toContain('OpCode');
  expect(hoverContents).toContain('Binary');
});
