const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  runToCompletion,
} = require('./test-utils');

/**
 * Helper to read a specific GPR value from the Registers table.
 */
async function getRegisterValue(page, registerName) {
  const rows = page.locator('#registers tbody tr');
  const count = await rows.count();
  for (let i = 0; i < count; i++) {
    const nameCell = rows.nth(i).locator('td.registerName').first();
    const name = await nameCell.textContent();
    if (name && name.trim() === registerName) {
      const valueCell = rows.nth(i).locator('td').nth(2);
      const text = await valueCell.textContent();
      return text ? text.trim() : '';
    }
  }
  return null;
}

test('arithmetic operations produce correct register values', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // DADDI r1, r0, 50  → r1 = 50
  // DADDI r2, r0, 30  → r2 = 30
  // DADD  r3, r1, r2  → r3 = 80
  // DSUB  r4, r1, r2  → r4 = 20
  const program = `.code
DADDI r1, r0, 50
DADDI r2, r0, 30
DADD r3, r1, r2
DSUB r4, r1, r2
SYSCALL 0
`;

  await loadProgram(page, program);
  await runToCompletion(page);

  const r1 = await getRegisterValue(page, 'R1');
  expect(r1).toBe('0000000000000032'); // 50

  const r2 = await getRegisterValue(page, 'R2');
  expect(r2).toBe('000000000000001E'); // 30

  const r3 = await getRegisterValue(page, 'R3');
  expect(r3).toBe('0000000000000050'); // 80

  const r4 = await getRegisterValue(page, 'R4');
  expect(r4).toBe('0000000000000014'); // 20

  await page.close();
});

test('load/store operations produce correct register values', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // Store 42 into memory, then load it back into a different register.
  const program = `.data
value: .word 42

.code
LD r1, value(r0)
DADDI r2, r1, 8
SD r2, value(r0)
LD r3, value(r0)
SYSCALL 0
`;

  await loadProgram(page, program);
  await runToCompletion(page);

  // r1 = 42 (loaded from memory)
  const r1 = await getRegisterValue(page, 'R1');
  expect(r1).toBe('000000000000002A');

  // r2 = 42 + 8 = 50
  const r2 = await getRegisterValue(page, 'R2');
  expect(r2).toBe('0000000000000032');

  // r3 = 50 (re-loaded from memory after SD)
  const r3 = await getRegisterValue(page, 'R3');
  expect(r3).toBe('0000000000000032');

  await page.close();
});

test('branch loop produces correct final register value', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // Counted loop: r1 counts from 10 down to 0, r2 accumulates +3 each
  // iteration → r2 = 10 * 3 = 30 when the loop ends.
  const program = `.code
DADDI r1, r0, 10
DADDI r2, r0, 0
loop:
DADDI r2, r2, 3
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;

  await loadProgram(page, program);
  await runToCompletion(page);

  // r1 should be 0 after the loop
  const r1 = await getRegisterValue(page, 'R1');
  expect(r1).toBe('0000000000000000');

  // r2 should be 30 (0x1E) — 10 iterations × 3
  const r2 = await getRegisterValue(page, 'R2');
  expect(r2).toBe('000000000000001E');

  await page.close();
});
