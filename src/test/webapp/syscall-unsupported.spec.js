const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
} = require('./test-utils');

/**
 * Tests for how the Web UI reports SYSCALLs that cannot be served by the
 * browser environment: invalid syscall numbers, and file I/O syscalls issued
 * against file descriptors other than stdin/stdout (the web UI has no
 * filesystem, so only stdin/stdout are available).
 *
 * The worker raises an UnsupportedSyscallException which is routed through
 * ResultFactory.AddRuntimeErrors, so the alert must match the structured
 * "Synchronous exception" format produced by Simulator.js.
 */

// Program that issues an unknown SYSCALL number.
// The parser enforces a 5-bit unsigned immediate (0..31) for the SYSCALL
// argument, but only 0..5 are recognised at runtime. SYSCALL 6 therefore
// parses fine but is rejected in the ID stage with UNSUPPORTED_SYSCALL.
const invalidSyscallProgram = `.code
              syscall 6
              syscall 0
`;

// Program that issues SYSCALL 4 (write) to a non-stdout file descriptor.
// fd is 7 (not STDOUT_FD == 1), which should be refused under the web UI's
// NullFileUtils environment.
const writeToBadFdProgram = `.data
params_fd:    .word64 7
params_buf:   .space  8
params_count: .word64 5
buffer:       .space  8

.code
              daddi $t0, $zero, buffer
              sd    $t0, params_buf($zero)
              daddi $t6, $zero, params_fd
              syscall 4
              syscall 0
`;

// Program that issues SYSCALL 3 (read) from a non-stdin file descriptor.
// fd is 9 (not STDIN_FD == 0), which should be refused.
const readFromBadFdProgram = `.data
params_fd:    .word64 9
params_buf:   .space  8
params_count: .word64 5
buffer:       .space  8

.code
              daddi $t0, $zero, buffer
              sd    $t0, params_buf($zero)
              daddi $t6, $zero, params_fd
              syscall 3
              syscall 0
`;

// Program that issues SYSCALL 1 (open), which has no filesystem in the web UI
// and must be refused regardless of arguments.
const openProgram = `.data
filename:    .asciiz "nope.txt"
             .align  3
flags:       .word64 1

.code
             daddi $t6, $zero, filename
             syscall 1
             syscall 0
`;

/**
 * Helper that loads a program, runs it and captures all native alert() dialogs.
 * Returns the array of alert messages (typically length 1 for our tests).
 */
async function runAndCollectAlerts(page, program) {
  await page.goto(targetUri);
  await waitForPageReady(page);

  const alertMessages = [];
  page.on('dialog', async (dialog) => {
    if (dialog.type() === 'alert') {
      alertMessages.push(dialog.message());
    }
    await dialog.dismiss();
  });

  await loadProgram(page, program);
  await removeOverlay(page);

  await page.click('#run-button');

  // Wait until the alert handler has been invoked.
  await expect.poll(() => alertMessages.length, { timeout: 15000 }).toBeGreaterThan(0);

  return alertMessages;
}

/**
 * Assertions common to every unsupported-syscall alert: structured prefix,
 * UNSUPPORTED_SYSCALL code, and no raw Java exception toString leak.
 */
function assertUnsupportedSyscallAlert(alertMessage, expectedInstruction, expectedStage) {
  expect(alertMessage).toContain('Synchronous exception');
  expect(alertMessage).toContain('Unsupported system call');
  expect(alertMessage).toContain('UNSUPPORTED_SYSCALL');
  expect(alertMessage).toContain(`Instruction: ${expectedInstruction}`);
  expect(alertMessage).toContain(`Pipeline stage: ${expectedStage}`);
  // Must not be the raw Java exception toString.
  expect(alertMessage).not.toContain('org.edumips64.core');
}

test('invalid SYSCALL number produces a user-friendly unsupported-syscall alert', async ({
  page,
}) => {
  const alertMessages = await runAndCollectAlerts(page, invalidSyscallProgram);

  const alertMessage = alertMessages[0];
  assertUnsupportedSyscallAlert(alertMessage, 'SYSCALL 6', 'ID');
  expect(alertMessage).toContain('SYSCALL 6');

  // No spurious follow-up alert (e.g. "Cannot run in state READY").
  await page.waitForTimeout(1000);
  expect(alertMessages).toHaveLength(1);
  for (const msg of alertMessages) {
    expect(msg).not.toContain('Cannot run in state');
  }
});

test('SYSCALL 4 (write) on a non-stdout fd produces a user-friendly alert', async ({
  page,
}) => {
  const alertMessages = await runAndCollectAlerts(page, writeToBadFdProgram);

  const alertMessage = alertMessages[0];
  assertUnsupportedSyscallAlert(alertMessage, 'SYSCALL 4', 'MEM');
  // The underlying message must identify SYSCALL 4 and the offending fd.
  expect(alertMessage).toContain('SYSCALL 4');
  expect(alertMessage).toContain('file descriptor 7');

  await page.waitForTimeout(1000);
  expect(alertMessages).toHaveLength(1);
  for (const msg of alertMessages) {
    expect(msg).not.toContain('Cannot run in state');
  }
});

test('SYSCALL 3 (read) on a non-stdin fd produces a user-friendly alert', async ({
  page,
}) => {
  const alertMessages = await runAndCollectAlerts(page, readFromBadFdProgram);

  const alertMessage = alertMessages[0];
  assertUnsupportedSyscallAlert(alertMessage, 'SYSCALL 3', 'MEM');
  expect(alertMessage).toContain('SYSCALL 3');
  expect(alertMessage).toContain('file descriptor 9');

  await page.waitForTimeout(1000);
  expect(alertMessages).toHaveLength(1);
});

test('SYSCALL 1 (open) is refused in the web UI environment', async ({ page }) => {
  const alertMessages = await runAndCollectAlerts(page, openProgram);

  const alertMessage = alertMessages[0];
  assertUnsupportedSyscallAlert(alertMessage, 'SYSCALL 1', 'MEM');
  expect(alertMessage).toContain('SYSCALL 1');
  expect(alertMessage).toContain('no filesystem');

  await page.waitForTimeout(1000);
  expect(alertMessages).toHaveLength(1);
});
