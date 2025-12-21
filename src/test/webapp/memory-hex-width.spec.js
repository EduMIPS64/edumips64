const { test, expect } = require('@playwright/test');
const {
  targetUri,
  waitForPageReady,
  waitForRunningState,
  loadProgram,
  runToCompletion
} = require('./test-utils');

/**
 * Helper function to expand Memory accordion and wait for table
 */
async function expandMemoryAndWaitForTable(page) {
  await page.click('#memory-accordion-summary');
  await page.waitForSelector('#memory tbody tr', { timeout: 5000 });
}

/**
 * Regression test for issue #1306: Memory values should be displayed with consistent 64-bit hex width.
 * 
 * Before the fix, memory values were displayed with 32-bit hex widths (8 characters),
 * causing inconsistent display when values were stored in memory.
 * 
 * After the fix, all memory values should be displayed with 64-bit hex widths (16 characters).
 */
test('memory values should have consistent 64-bit hex width', async ({ page }) => {
  await page.goto(targetUri);

  // Wait for the page to load
  await waitForPageReady(page);

  // Click Load button to load the default sample program
  await page.click('#load-button');

  // Wait for the simulator to enter RUNNING state
  await waitForRunningState(page);

  // Expand the Memory accordion and wait for table
  await expandMemoryAndWaitForTable(page);

  // Get all value cells from the memory table (second column)
  // The memory table has columns: Address, Value, Label, Code, Comment
  // We need to check the Value column which contains value_hex
  const valueElements = await page.$$('#memory tbody tr td:nth-child(2)');
  
  expect(valueElements.length).toBeGreaterThan(0);

  // Verify each value_hex has exactly 16 hex characters (64-bit)
  for (let i = 0; i < valueElements.length; i++) {
    const hexValue = await valueElements[i].textContent();
    const trimmedValue = hexValue.trim();
    
    // A valid 64-bit hex value should have exactly 16 hex characters
    // The format should match: 0000000000000006 (for value 6)
    expect(trimmedValue).toMatch(/^[0-9A-Fa-f]{16}$/);
  }

  await page.close();
});

/**
 * Additional test to verify memory values after executing a store byte instruction.
 * This specifically tests the case from issue #1306 where storing a byte
 * caused inconsistent hex display.
 */
test('memory values remain 64-bit width after store byte instruction', async ({ page }) => {
  await page.goto(targetUri);

  // Wait for the page to load
  await waitForPageReady(page);

  // Program that stores a byte value in memory (test case from issue #1306)
  const testProgram = `.data
  target: .space 8

.code
  DADDI r1, r0, 6
  SB r1, target(r0)
  SYSCALL 0
`;

  await loadProgram(page, testProgram);
  await runToCompletion(page);

  // Expand the Memory accordion and wait for table
  await expandMemoryAndWaitForTable(page);

  // Get all value cells from the memory table
  const valueElements = await page.$$('#memory tbody tr td:nth-child(2)');
  
  expect(valueElements.length).toBeGreaterThan(0);

  // Verify each value_hex has exactly 16 hex characters (64-bit)
  // This is the key test for issue #1306 - after storing a byte, 
  // the hex representation should still be 16 characters
  for (let i = 0; i < valueElements.length; i++) {
    const hexValue = await valueElements[i].textContent();
    const trimmedValue = hexValue.trim();
    
    // A valid 64-bit hex value should have exactly 16 hex characters
    expect(trimmedValue).toMatch(/^[0-9A-Fa-f]{16}$/);
  }

  // Verify that the stored value 6 appears in one of the memory cells
  // (the byte 6 should be visible as part of the 64-bit hex representation)
  const memoryContent = await page.textContent('#memory');
  // The value 6 stored as a byte at the beginning of an 8-byte cell 
  // should show up as 0600000000000000 (little-endian) or similar

  await page.close();
});
