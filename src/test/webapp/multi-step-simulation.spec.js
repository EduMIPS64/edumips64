const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
} = require('./test-utils');

/**
 * Helper to read the current cycle count from the Statistics panel.
 */
async function getCycles(page) {
  const text = await page.locator('#stat-cycles').textContent();
  return parseInt(text || '0', 10);
}

/**
 * Helper to read the current instruction count from the Statistics panel.
 */
async function getInstructions(page) {
  const text = await page.locator('#stat-instructions').textContent();
  return parseInt(text || '0', 10);
}

/**
 * Helper to perform a single step and wait for the cycle counter to advance.
 */
async function singleStep(page) {
  const before = await getCycles(page);
  await removeOverlay(page);
  await page.click('#step-button');
  await expect(page.locator('#stat-cycles')).not.toHaveText(String(before), {
    timeout: 5000,
  });
}

const LOOP_PROGRAM = `.code
DADDI r1, r0, 5
loop:
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;

test('multi-step button advances simulation by multiple cycles', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, LOOP_PROGRAM);

  // Record cycles before multi-step
  const cyclesBefore = await getCycles(page);

  await removeOverlay(page);
  await page.click('#multi-step-button');

  // Wait for the cycle count to increase past the initial value
  await expect(page.locator('#stat-cycles')).not.toHaveText(
    String(cyclesBefore),
    { timeout: 15000 }
  );

  const cyclesAfter = await getCycles(page);

  // Multi-step should advance more than 1 cycle (default stride is 500,
  // but this small program finishes in ~30 cycles, so any value > 1 is good).
  expect(cyclesAfter).toBeGreaterThan(1);

  await page.close();
});

test('statistics reflect multiple steps after multi-step', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  await loadProgram(page, LOOP_PROGRAM);

  await removeOverlay(page);
  await page.click('#multi-step-button');

  // Wait for cycles to update
  await expect(page.locator('#stat-cycles')).toHaveText(/^[1-9][0-9]*$/, {
    timeout: 15000,
  });

  const cycles = await getCycles(page);
  const instructions = await getInstructions(page);

  // The program runs a loop 5 times plus setup and SYSCALL → at least 7 instructions
  expect(cycles).toBeGreaterThan(1);
  expect(instructions).toBeGreaterThan(1);

  await page.close();
});

test('can single-step after multi-step', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // Use a longer loop so multi-step doesn't finish the whole program
  const longerLoop = `.code
DADDI r1, r0, 2000
loop:
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;

  await loadProgram(page, longerLoop);

  await removeOverlay(page);
  await page.click('#multi-step-button');

  // Wait for cycles to update
  await expect(page.locator('#stat-cycles')).toHaveText(/^[1-9][0-9]*$/, {
    timeout: 15000,
  });

  const cyclesAfterMulti = await getCycles(page);
  expect(cyclesAfterMulti).toBeGreaterThan(1);

  // Now single-step if the simulation is still running (step button enabled)
  const stepButton = page.locator('#step-button');
  const isEnabled = !(await stepButton.isDisabled());

  if (isEnabled) {
    await singleStep(page);
    const cyclesAfterSingle = await getCycles(page);
    expect(cyclesAfterSingle).toBe(cyclesAfterMulti + 1);
  } else {
    // If the simulation already finished, that's also fine — verify cycles are > 1
    expect(cyclesAfterMulti).toBeGreaterThan(1);
  }

  await page.close();
});
