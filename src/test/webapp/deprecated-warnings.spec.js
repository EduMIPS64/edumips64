const { test, expect } = require('@playwright/test');
const { targetUri, waitForPageReady, removeOverlay, waitForRunningState } = require('./test-utils');

/**
 * Tests for the behavior of deprecated-instruction warnings.
 *
 * Regression tests for https://github.com/EduMIPS64/edumips64/issues related
 * to "Deprecated Instructions weird behaviours":
 *   1. The deprecated instruction in the sample code must be flagged as a
 *      warning as soon as the page loads, without requiring the user to
 *      edit the code first.
 *   2. After loading code that contains only warnings (no errors), the
 *      hover provider in the editor must still show the per-instruction
 *      metadata tooltip (Address/OpCode/Binary/Hex/CPU Stage).
 */

test('deprecated instruction in sample is flagged on initial load', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // The Issues accordion should show the deprecated-instruction warning for
  // the sample program, without the user having had to edit anything.
  const warningItem = page.locator('.error-list-item').first();
  await expect(warningItem).toBeVisible({ timeout: 10000 });

  // And the warning chip in the Issues header should show "1".
  await expect(page.getByRole('heading', { name: /Issues\s*1/ })).toBeVisible();
});

test('hover tooltip works after loading code with a warning', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  // Wait for the initial syntax check to populate the Issues panel.
  await expect(page.locator('.error-list-item').first()).toBeVisible({ timeout: 10000 });

  // Load the (sample) program, which contains a deprecated-instruction warning.
  await page.waitForSelector('#load-button:not([disabled])', { timeout: 10000 });
  await page.click('#load-button');
  await waitForRunningState(page);

  // Trigger the Monaco hover on an instruction line (line 17, `daddi r10, r0, 1000`).
  // If `parsedInstructions` was wrongly cleared because the program has
  // warnings, the hover provider would return nothing and no tooltip would
  // be rendered.
  const hoverContents = await page.evaluate(async () => {
    const { editor } = window;
    editor.setPosition({ lineNumber: 17, column: 5 });
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
