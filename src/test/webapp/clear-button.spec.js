const { test, expect } = require('@playwright/test');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  runToCompletion
} = require('./test-utils');

/**
 * Test: Verify Clear button behavior
 * Load a test program, run it to completion, then click clear and verify:
 * 1) Statistics are at 0
 * 2) Code is the trivial "syscall 0" program
 * 3) Error accordion is not visible
 * 4) Change markers are not visible
 */
test('clear button resets simulator state and UI', async ({ page }) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Load a test program that generates statistics
  const testProgram = `.data
value: .word 42

.code
LD r1, value(r0)
DADDI r2, r1, 1
SD r2, value(r0)
SYSCALL 0
`;

  await loadProgram(page, testProgram);
  await runToCompletion(page);

  // Verify that statistics are non-zero after running
  const cyclesBeforeClear = await page.locator('#stat-cycles').textContent();
  expect(parseInt(cyclesBeforeClear || '0', 10)).toBeGreaterThan(0);

  // Verify that instructions executed is non-zero
  const instructionsBeforeClear = await page.locator('#stat-instructions').textContent();
  expect(parseInt(instructionsBeforeClear || '0', 10)).toBeGreaterThan(0);

  // Remove overlay before clicking clear button
  await removeOverlay(page);

  // Click the Clear button
  await page.click('#clear-code-button');

  // Wait a bit for the state to update
  await page.waitForTimeout(500);

  // 1) Verify that statistics are at 0
  const cyclesAfterClear = await page.locator('#stat-cycles').textContent();
  expect(cyclesAfterClear).toBe('0');

  const instructionsAfterClear = await page.locator('#stat-instructions').textContent();
  expect(instructionsAfterClear).toBe('0');

  const rawStallsAfterClear = await page.locator('#stat-raw-stalls').textContent();
  expect(rawStallsAfterClear).toBe('0');
  const wawStallsAfterClear = await page.locator('#stat-waw-stalls').textContent();
  expect(wawStallsAfterClear).toBe('0');
  const structuralStallsAfterClear = await page.locator('#stat-structural-stalls').textContent();
  expect(structuralStallsAfterClear).toBe('0');

  // 2) Verify that the code is the trivial "syscall 0" program
  // Get the editor content by reading from Monaco's model
  const editorContent = await page.evaluate(() => {
    const editor = window.monaco.editor.getModels()[0];
    return editor ? editor.getValue() : '';
  });

  // The clearCode function sets code to ".data\n\n.code\n  SYSCALL 0\n"
  expect(editorContent.trim()).toBe('.data\n\n.code\n  SYSCALL 0'.trim());

  // 3) Verify that the error accordion is not visible
  // The error accordion only renders when parsingErrors is defined
  const errorAccordion = page.locator('.error-accordion');
  await expect(errorAccordion).toHaveCount(0);

  // 4) Verify that change markers are not visible
  // Change markers have class "accordion-change-indicator"
  const changeMarkers = page.locator('.accordion-change-indicator');
  await expect(changeMarkers).toHaveCount(0);

  await page.close();
});

/**
 * Test: Verify Clear button works after a program with errors
 * Load a program with syntax errors, then clear and verify error accordion disappears
 */
test('clear button removes error accordion after syntax errors', async ({ page }) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Load a program with syntax errors (invalid instruction)
  const invalidProgram = `.code
INVALID_INSTRUCTION r1, r2
SYSCALL 0
`;

  // Remove overlay before interacting
  await removeOverlay(page);

  const inputArea = page.locator('.monaco-editor textarea.inputarea');
  await inputArea.click({ force: true });

  // Clear existing text
  await page.keyboard.press('ControlOrMeta+a');
  await page.keyboard.press('Backspace');

  // Insert invalid program
  await page.keyboard.insertText(invalidProgram);

  // Remove overlay again
  await removeOverlay(page);

  // Wait a bit for syntax checking to complete
  await page.waitForTimeout(1000);

  // Verify that the error accordion is visible
  const errorAccordionBefore = page.locator('.error-accordion');
  await expect(errorAccordionBefore).toBeVisible();

  // Click the Clear button
  await page.click('#clear-code-button');

  // Wait for state to update
  await page.waitForTimeout(500);

  // Verify that the error accordion is not visible after clear
  const errorAccordionAfter = page.locator('.error-accordion');
  await expect(errorAccordionAfter).toHaveCount(0);

  await page.close();
});

/**
 * Test: Verify change markers appear during execution and are cleared
 * This test verifies the full lifecycle of accordion change markers
 */
test('clear button removes accordion change markers', async ({ page }) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Load a simple program
  const testProgram = `.code
DADDI r1, r0, 100
SYSCALL 0
`;

  await loadProgram(page, testProgram);

  // Collapse an accordion (e.g., stats) to trigger change markers when it updates
  const statsAccordion = page.locator('text=Stats').locator('..');
  await statsAccordion.click();

  // Wait for accordion to collapse
  await page.waitForTimeout(300);

  // Run the program to completion
  await runToCompletion(page);

  // Wait a bit for change markers to appear
  await page.waitForTimeout(500);

  // Verify that at least one change marker is visible
  // (Stats should have changed while collapsed)
  const changeMarkersBefore = page.locator('.accordion-change-indicator');
  const countBefore = await changeMarkersBefore.count();
  expect(countBefore).toBeGreaterThan(0);

  // Remove overlay before clicking clear button
  await removeOverlay(page);

  // Click the Clear button
  await page.click('#clear-code-button');

  // Wait for state to update
  await page.waitForTimeout(500);

  // Verify that all change markers are removed
  const changeMarkersAfter = page.locator('.accordion-change-indicator');
  await expect(changeMarkersAfter).toHaveCount(0);

  await page.close();
});
