const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  runToCompletion,
  clickProgramMenuItem,
} = require('./test-utils');

/**
 * Test: Verify Clear button behavior
 * Load a test program, run it to completion, then click clear and verify:
 * 1) Statistics are at 0
 * 2) Code is the trivial "syscall 0" program
 * 3) Issues card is not visible
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
  const instructionsBeforeClear = await page
    .locator('#stat-instructions')
    .textContent();
  expect(parseInt(instructionsBeforeClear || '0', 10)).toBeGreaterThan(0);

  // Remove overlay before clicking clear button
  await removeOverlay(page);

  // Open the Program menu and click "New" (Clear)
  await clickProgramMenuItem(page, '#clear-code-button');

  // Wait a bit for the state to update
  await page.waitForTimeout(500);

  // 1) Verify that statistics are at 0
  const cyclesAfterClear = await page.locator('#stat-cycles').textContent();
  expect(cyclesAfterClear).toBe('0');

  const instructionsAfterClear = await page
    .locator('#stat-instructions')
    .textContent();
  expect(instructionsAfterClear).toBe('0');

  const rawStallsAfterClear = await page
    .locator('#stat-raw-stalls')
    .textContent();
  expect(rawStallsAfterClear).toBe('0');
  const wawStallsAfterClear = await page
    .locator('#stat-waw-stalls')
    .textContent();
  expect(wawStallsAfterClear).toBe('0');
  const structuralStallsAfterClear = await page
    .locator('#stat-structural-stalls')
    .textContent();
  expect(structuralStallsAfterClear).toBe('0');

  // 2) Verify that the code is the trivial "syscall 0" program
  // Get the editor content by reading from Monaco's model
  const editorContent = await page.evaluate(() => {
    const editor = window.monaco.editor.getModels()[0];
    return editor ? editor.getValue() : '';
  });

  // The clearCode function sets code to ".data\n\n.code\n  SYSCALL 0\n"
  expect(editorContent.trim()).toBe('.data\n\n.code\n  SYSCALL 0'.trim());

  // 3) Verify that the issues card is not visible (no parsing errors after clear)
  const issuesCard = page.locator('#issues-card');
  await expect(issuesCard).toHaveCount(0);

  await page.close();
});

/**
 * Test: Verify Clear button works after a program with errors
 * Load a program with syntax errors, then clear and verify error accordion disappears
 */
test('clear button removes issues card after syntax errors', async ({
  page,
}) => {
  await page.goto(targetUri);

  await waitForPageReady(page);

  // Load a program with syntax errors (invalid instruction)
  const invalidProgram = `.code
INVALID_INSTRUCTION r1, r2
SYSCALL 0
`;

  // Remove overlay before interacting
  await removeOverlay(page);

  // Insert invalid program directly using Monaco API
  await page.evaluate((prog) => {
    window.monaco.editor.getModels()[0].setValue(prog);
  }, invalidProgram);

  // Remove overlay again
  await removeOverlay(page);

  // Wait a bit for syntax checking to complete
  await page.waitForTimeout(1000);

  // Verify that the issues card is visible
  const issuesCardBefore = page.locator('#issues-card');
  await expect(issuesCardBefore).toBeVisible();

  // Open the Program menu and click "New" (Clear)
  await clickProgramMenuItem(page, '#clear-code-button');

  // Wait for state to update
  await page.waitForTimeout(500);

  // Verify that the issues card is not visible after clear
  const issuesCardAfter = page.locator('#issues-card');
  await expect(issuesCardAfter).toHaveCount(0);

  await page.close();
});
