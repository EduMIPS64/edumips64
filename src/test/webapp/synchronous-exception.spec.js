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
 * simulator must surface a user-friendly alert that includes the faulting
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

  // Capture all native alert() dialogs shown by the simulator. We collect
  // every alert (not just the first) so we can also assert that the Run All
  // flow does not surface a spurious follow-up "Cannot run in state READY"
  // dialog after the real synchronous-exception alert.
  const alertMessages = [];
  page.on('dialog', async (dialog) => {
    if (dialog.type() === 'alert') {
      alertMessages.push(dialog.message());
    }
    await dialog.dismiss();
  });

  await loadProgram(page, overflowProgram);
  await removeOverlay(page);

  // Run the program; once the overflow fires, the worker will post a failure
  // Result and the UI will show the alert.
  await page.click('#run-button');

  // Wait until the alert handler has been invoked.
  await expect.poll(() => alertMessages.length, { timeout: 15000 }).toBeGreaterThan(0);

  const alertMessage = alertMessages[0];

  // The alert must describe the exception in plain English, and include the
  // faulting instruction and pipeline stage.
  expect(alertMessage).toContain('Synchronous exception');
  expect(alertMessage).toContain('Integer overflow');
  expect(alertMessage).toContain('INTOVERFLOW');
  expect(alertMessage).toMatch(/Instruction:\s*ADD\b/i);
  expect(alertMessage).toContain('Pipeline stage: EX');

  // It must NOT be the raw Java exception toString (which used to be the case).
  expect(alertMessage).not.toContain('org.edumips64.core');

  // Give the UI a moment to (not) fire any follow-up alerts, then assert that
  // no spurious second dialog (e.g. "Cannot run in state READY") was shown.
  await page.waitForTimeout(1000);
  expect(alertMessages).toHaveLength(1);
  for (const msg of alertMessages) {
    expect(msg).not.toContain('Cannot run in state');
  }
});
