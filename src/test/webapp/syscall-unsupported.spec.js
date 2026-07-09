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
 * ResultFactory.AddRuntimeErrors, so the dialog must match the structured
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
             .word64 1

.code
             daddi $t6, $zero, filename
             syscall 1
             syscall 0
`;

/**
 * Helper that loads a program, runs it and waits for the runtime-error MUI
 * dialog to appear. Returns the text content of the dialog.
 */
async function runAndGetDialogText(page, program) {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, program);
  await removeOverlay(page);

  await page.click('#run-button');

  // Wait until the runtime-error dialog appears.
  const dialog = page.locator('#runtime-error-dialog');
  await expect(dialog).toBeVisible({ timeout: 15000 });

  const text = await dialog.innerText();

  // Dismiss the dialog.
  await page.click('#runtime-error-ok');
  await expect(dialog).not.toBeVisible();

  return text;
}

/**
 * Assertions common to every unsupported-syscall dialog: structured prefix,
 * correct faulting instruction and stage, and no raw Java exception toString
 * leak.
 *
 * The Web UI composes the message as:
 *   "Synchronous exception: <message>\n\nInstruction: <instr>\nPipeline stage: <stage>"
 */
function assertUnsupportedSyscallAlert(
  dialogText,
  expectedInstruction,
  expectedStage,
) {
  expect(dialogText).toContain('Synchronous exception');
  expect(dialogText).toContain(`Instruction: ${expectedInstruction}`);
  expect(dialogText).toContain(`Pipeline stage: ${expectedStage}`);
  // Must not be the raw Java exception toString.
  expect(dialogText).not.toContain('org.edumips64.core');
}

test('invalid SYSCALL number produces a user-friendly unsupported-syscall alert', async ({
  page,
}) => {
  const dialogText = await runAndGetDialogText(page, invalidSyscallProgram);

  assertUnsupportedSyscallAlert(dialogText, 'SYSCALL 6', 'ID');
  expect(dialogText).toContain(
    'SYSCALL 6 is not a supported system call number',
  );

  // Give UI a moment to settle; confirm no further dialogs appear.
  await page.waitForTimeout(1000);
  await expect(page.locator('#runtime-error-dialog')).not.toBeVisible();
});

test('SYSCALL 4 (write) on a non-stdout fd produces a user-friendly alert', async ({
  page,
}) => {
  const dialogText = await runAndGetDialogText(page, writeToBadFdProgram);

  assertUnsupportedSyscallAlert(dialogText, 'SYSCALL 4', 'MEM');
  // The underlying message must identify SYSCALL 4 (write) and the offending fd.
  expect(dialogText).toContain('SYSCALL 4 (write) on file descriptor 7');
  expect(dialogText).toContain('stdout');

  await page.waitForTimeout(1000);
  await expect(page.locator('#runtime-error-dialog')).not.toBeVisible();
});

test('SYSCALL 3 (read) on a non-stdin fd produces a user-friendly alert', async ({
  page,
}) => {
  const dialogText = await runAndGetDialogText(page, readFromBadFdProgram);

  assertUnsupportedSyscallAlert(dialogText, 'SYSCALL 3', 'MEM');
  expect(dialogText).toContain('SYSCALL 3 (read) on file descriptor 9');
  expect(dialogText).toContain('stdin');

  await page.waitForTimeout(1000);
  await expect(page.locator('#runtime-error-dialog')).not.toBeVisible();
});

test('SYSCALL 1 (open) is refused in the web UI environment', async ({
  page,
}) => {
  const dialogText = await runAndGetDialogText(page, openProgram);

  assertUnsupportedSyscallAlert(dialogText, 'SYSCALL 1', 'MEM');
  expect(dialogText).toContain('SYSCALL 1 (open)');
  expect(dialogText).toContain('no filesystem');

  await page.waitForTimeout(1000);
  await expect(page.locator('#runtime-error-dialog')).not.toBeVisible();
});
