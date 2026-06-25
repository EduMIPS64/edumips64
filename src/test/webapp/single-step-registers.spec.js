const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  runToCompletion,
} = require('./test-utils');

/**
 * "Involved" web-UI test that exercises single-stepping and asserts that the
 * simulator output (cycle counter and architectural register values) is
 * exactly what we expect — directly addressing the "running some simulation
 * steps and ensuring the output is as expected" item of issue #588.
 *
 * Unlike forwarding.spec.js / sample-program-stdout.spec.js (which only use
 * the Run-All button), this test drives the simulator one cycle at a time via
 * the Single Step button and checks the visible state after each click.
 *
 * The program is deterministic and short so it runs identically regardless of
 * the pipeline timing:
 *   r1 <- 5
 *   r2 <- r1 + 10  = 15 (0xF)
 *   r3 <- r1 + r2  = 20 (0x14)
 */
const STEP_PROGRAM = `.code
  daddi r1, r0, 5
  daddi r2, r1, 10
  dadd  r3, r1, r2
  syscall 0
`;

/**
 * Read the cycle counter rendered in the Statistics accordion as an integer.
 */
async function getCycles(page) {
  const text = (await page.locator('#stat-cycles').textContent()) || '0';
  return parseInt(text, 10);
}

/**
 * Read a register's value from the Registers table by its name (e.g. "R1").
 *
 * The Registers table (id="registers") renders each register as three
 * consecutive cells: name, alias, hex value. GPR and FPU registers share a
 * row (two triples), while special registers occupy a row on their own, but
 * the name → alias → value ordering is identical in every case, so walking
 * the sibling cells works uniformly.
 *
 * The hex string has no "0x" prefix (e.g. "0000000000000005"), so we parse it
 * as a base-16 integer and return a Number for easy comparison.
 */
async function readRegister(page, name) {
  const hex = await page.evaluate((regName) => {
    const cells = Array.from(
      document.querySelectorAll('#registers td.registerName'),
    );
    for (const cell of cells) {
      if (cell.textContent.trim() === regName) {
        const aliasCell = cell.nextElementSibling;
        const valueCell = aliasCell ? aliasCell.nextElementSibling : null;
        return valueCell ? valueCell.textContent.trim() : null;
      }
    }
    return null;
  }, name);
  expect(hex, `register ${name} should be present in the table`).not.toBeNull();
  return parseInt(hex, 16);
}

test('single stepping advances the cycle counter one cycle at a time', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await loadProgram(page, STEP_PROGRAM);

  // Right after loading, the destination registers have not been written back
  // yet, so they must still be zero.
  expect(await readRegister(page, 'R1')).toBe(0);
  expect(await readRegister(page, 'R2')).toBe(0);
  expect(await readRegister(page, 'R3')).toBe(0);

  // Each click of the Single Step button must advance the cycle counter by
  // exactly one, relative to whatever the counter reads immediately after the
  // program is loaded.
  const startCycles = await getCycles(page);
  for (let i = 1; i <= 3; i++) {
    await page.click('#step-button');
    await expect(page.locator('#stat-cycles')).toHaveText(
      String(startCycles + i),
    );
  }
});

test('register values after stepping a program to completion match expected output', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await removeOverlay(page);

  await loadProgram(page, STEP_PROGRAM);

  // Drive the program to completion using Run-All (built on top of single
  // cycles) and then assert the final architectural state.
  await runToCompletion(page);

  expect(await readRegister(page, 'R1')).toBe(5);
  expect(await readRegister(page, 'R2')).toBe(15);
  expect(await readRegister(page, 'R3')).toBe(20);
});
