const { test, expect } = require('@playwright/test');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  waitForSimulationComplete,
  loadProgram,
} = require('./test-utils');

// The parser's .word64 directive only accepts integer/hex literals, not
// label names, so the buffer address is populated at runtime via sd.
const syscall3Program = `.data
params_read_fd:     .word64 0
params_read_buf:    .space  8
params_read_count:  .word64 5
params_write_fd:    .word64 1
params_write_buf:   .space  8
params_write_count: .word64 5
buffer:             .space  8

.code
              daddi $t0, $zero, buffer
              sd    $t0, params_read_buf($zero)
              sd    $t0, params_write_buf($zero)
              daddi $t6, $zero, params_read_fd
              syscall 3
              daddi $t6, $zero, params_write_fd
              syscall 4
              syscall 0
`;

async function openStdoutAccordion(page) {
  await page.locator('text=Standard Output').click();
}

test('syscall 3 opens an input dialog and resumes execution', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, syscall3Program);

  await removeOverlay(page);
  await page.click('#run-button');

  const inputDialog = page.getByRole('dialog');
  await expect(inputDialog).toBeVisible();
  await expect(inputDialog).toContainText('Please input your text');

  await inputDialog.getByRole('textbox').fill('abcde');
  await inputDialog.getByRole('button', { name: 'OK' }).click();

  await waitForSimulationComplete(page);
  await openStdoutAccordion(page);
  await expect(page.locator('#stdout-view')).toHaveValue('abcde');
});

test('syscall 3 caps the input at the maximum allowed length', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, syscall3Program);

  await removeOverlay(page);
  await page.click('#run-button');

  const inputDialog = page.getByRole('dialog');
  await expect(inputDialog).toBeVisible();
  await expect(inputDialog).toContainText('max 5 characters');

  // Counter starts at 0 / 5
  const counter = inputDialog.getByTestId('input-counter');
  await expect(counter).toHaveText('0 / 5');

  // Attempting to enter more than 5 characters is silently truncated to 5.
  const textbox = inputDialog.getByRole('textbox');
  await textbox.fill('abcdefgh');
  await expect(textbox).toHaveValue('abcde');
  await expect(counter).toHaveText('5 / 5');

  // Clear and re-enter a valid value to complete the syscall.
  await textbox.fill('');
  await expect(counter).toHaveText('0 / 5');
  await textbox.fill('vwxyz');
  await expect(counter).toHaveText('5 / 5');
  await inputDialog.getByRole('button', { name: 'OK' }).click();

  await waitForSimulationComplete(page);
  await openStdoutAccordion(page);
  await expect(page.locator('#stdout-view')).toHaveValue('vwxyz');
});
