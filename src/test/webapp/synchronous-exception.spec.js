const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
} = require('./test-utils');

/**
 * Tests for how the Web UI reports synchronous exceptions raised by the CPU.
 *
 * When a program triggers a synchronous exception (e.g. integer overflow), the
 * simulator must surface a user-friendly dialog that includes the faulting
 * instruction and the pipeline stage, rather than a raw
 * `org.edumips64.core.is.IntegerOverflowException: INTOVERFLOW` string.
 */

// Program that deliberately overflows a 32-bit addition and causes an
// IntegerOverflowException (INTOVERFLOW). 0x7FFFFFFF + 1 triggers the trap on
// ADD (the 3-op signed add).
const overflowProgram = `.data
big:  .word 0x7FFFFFFF
one:  .word 0x00000001

.code
      lw  $t0, big($zero)
      lw  $t1, one($zero)
      add $t2, $t0, $t1
      syscall 0
`;

test('integer overflow produces a user-friendly synchronous-exception alert', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, overflowProgram);
  await removeOverlay(page);

  // Run the program; once the overflow fires, the worker will post a failure
  // Result and the UI will show the runtime-error dialog.
  await page.click('#run-button');

  // Wait until the runtime-error dialog appears.
  const dialog = page.locator('#runtime-error-dialog');
  await expect(dialog).toBeVisible({ timeout: 15000 });

  const dialogText = await dialog.innerText();

  // The dialog must describe the exception in plain English, and include the
  // faulting instruction and pipeline stage.
  expect(dialogText).toContain('Synchronous exception');
  expect(dialogText).toContain('Integer overflow');
  expect(dialogText).toContain('INTOVERFLOW');
  expect(dialogText).toMatch(/Instruction:\s*ADD\b/i);
  expect(dialogText).toContain('stage: EX');

  // It must NOT be the raw Java exception toString (which used to be the case).
  expect(dialogText).not.toContain('org.edumips64.core');

  // Dismiss the dialog.
  await page.click('#runtime-error-ok');
  await expect(dialog).not.toBeVisible();

  // Give the UI a moment to settle and confirm no further runtime-error
  // dialogs appear (no spurious "Cannot run in state READY" etc.).
  await page.waitForTimeout(1000);
  await expect(dialog).not.toBeVisible();
});
