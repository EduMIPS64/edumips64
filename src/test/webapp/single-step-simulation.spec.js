const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
} = require('./test-utils');

/**
 * Helper to read a specific GPR value from the Registers table.
 * The table renders one row per GPR (R0–R31), each containing:
 *   <td class="registerName">R0</td>
 *   <td class="registerName">(zero)</td>
 *   <td>hexValue</td>
 * followed by the FPR columns in the same row.
 */
async function getRegisterValue(page, registerName) {
  const rows = page.locator('#registers tbody tr');
  const count = await rows.count();
  for (let i = 0; i < count; i++) {
    const nameCell = rows.nth(i).locator('td.registerName').first();
    const name = await nameCell.textContent();
    if (name && name.trim() === registerName) {
      // The hex value is in the 3rd <td> (index 2) in the GPR section
      const valueCell = rows.nth(i).locator('td').nth(2);
      const text = await valueCell.textContent();
      return text ? text.trim() : '';
    }
  }
  return null;
}

/**
 * Helper to read the current cycle count from the Statistics panel.
 */
async function getCycles(page) {
  const text = await page.locator('#stat-cycles').textContent();
  return parseInt(text || '0', 10);
}

/**
 * Helper to perform a single step and wait for the cycle counter to advance.
 */
async function singleStep(page) {
  const before = await getCycles(page);
  await removeOverlay(page);
  await page.click('#step-button');
  // Wait for the cycle count to increase (confirms the step completed)
  await expect(page.locator('#stat-cycles')).not.toHaveText(String(before), {
    timeout: 5000,
  });
}

const PROGRAM = `.code
DADDI r1, r0, 10
DADDI r2, r0, 20
DADD r3, r1, r2
SYSCALL 0
`;

test('single-step produces expected register values after each instruction retires', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, PROGRAM);

  // The 5-stage pipeline means an instruction enters WB 5 cycles after it is
  // fetched (IF → ID → EX → MEM → WB). With forwarding disabled by default,
  // stalls may add extra cycles. We step enough times to guarantee each
  // instruction has completed WB, checking the register once we know it must
  // have been written.

  // Step through enough cycles for the first instruction (DADDI r1,r0,10) to
  // reach WB. In a classic 5-stage pipeline that is 5 cycles.
  for (let i = 0; i < 5; i++) {
    await singleStep(page);
  }
  // r1 should now hold 10 (0x000000000000000A)
  const r1 = await getRegisterValue(page, 'R1');
  expect(r1).toBe('000000000000000A');

  // Continue stepping until the second instruction has also retired.
  // Depending on stalls, a few more steps are sufficient.
  // We step until r2 is no longer zero, with a reasonable upper bound.
  let r2 = await getRegisterValue(page, 'R2');
  let safety = 0;
  while (r2 === '0000000000000000' && safety < 10) {
    await singleStep(page);
    r2 = await getRegisterValue(page, 'R2');
    safety++;
  }
  expect(r2).toBe('0000000000000014');

  // Continue stepping until the third instruction (DADD r3,r1,r2) retires.
  let r3 = await getRegisterValue(page, 'R3');
  safety = 0;
  while (r3 === '0000000000000000' && safety < 10) {
    await singleStep(page);
    r3 = await getRegisterValue(page, 'R3');
    safety++;
  }
  expect(r3).toBe('000000000000001E');

  await page.close();
});

test('pipeline stages are populated during single-stepping', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, PROGRAM);

  // After a few steps the pipeline should be full — every stage has an
  // instruction (or at least most stages have content).
  for (let i = 0; i < 4; i++) {
    await singleStep(page);
  }

  // The pipeline table lives inside #pipeline. Each row has two cells:
  //   <td class="instructionName">IF</td>  <td>instruction code</td>
  // We verify that at least some of the five main stages (IF, ID, EX, MEM, WB)
  // contain non-empty instruction text.
  const stages = ['IF', 'ID', 'EX', 'MEM', 'WB'];
  let populatedStages = 0;

  for (const stage of stages) {
    const row = page.locator('#pipeline table tbody tr', {
      has: page.locator(`td.instructionName:text-is("${stage}")`),
    });
    const codeCell = row.locator('td').nth(1);
    const text = (await codeCell.textContent()) || '';
    if (text.trim().length > 0) {
      populatedStages++;
    }
  }

  // After 4 steps we expect at least 4 stages to have instructions
  expect(populatedStages).toBeGreaterThanOrEqual(4);

  await page.close();
});

test('cycle count increases monotonically with each single step', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, PROGRAM);

  let previousCycles = 0;
  for (let i = 0; i < 6; i++) {
    await singleStep(page);
    const current = await getCycles(page);
    expect(current).toBeGreaterThan(previousCycles);
    previousCycles = current;
  }

  await page.close();
});
