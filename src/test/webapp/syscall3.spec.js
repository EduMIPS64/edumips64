const { test, expect } = require('@playwright/test');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  waitForSimulationComplete,
  loadProgram,
} = require('./test-utils');

const syscall3Program = `.data
params_read:  .word64 0
              .word64 buffer
              .word64 5
params_write: .word64 1
              .word64 buffer
              .word64 5
buffer:       .space 8

.code
              daddi $t6, $zero, params_read
              syscall 3
              daddi $t6, $zero, params_write
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

test('syscall 3 enforces the same maximum input length as the desktop app', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, syscall3Program);

  await removeOverlay(page);
  await page.click('#run-button');

  const inputDialog = page.getByRole('dialog');
  await expect(inputDialog).toBeVisible();

  await inputDialog.getByRole('textbox').fill('abcdef');
  await inputDialog.getByRole('button', { name: 'OK' }).click();
  await expect(inputDialog).toContainText('Input must not exceed 5 characters');

  await inputDialog.getByRole('textbox').fill('vwxyz');
  await inputDialog.getByRole('button', { name: 'OK' }).click();

  await waitForSimulationComplete(page);
  await openStdoutAccordion(page);
  await expect(page.locator('#stdout-view')).toHaveValue('vwxyz');
});
