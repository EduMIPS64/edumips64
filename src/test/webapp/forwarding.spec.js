const { test, expect } = require('./fixtures');
const {
  targetUri,
  removeOverlay,
  waitForPageReady,
  loadProgram,
  runToCompletion,
} = require('./test-utils');

const STORAGE_PREFIX = 'edumips64:v1:';

/**
 * The "forwarding.s" program from src/test/resources/. Kept inline so the
 * web-UI tests do not depend on the on-disk layout used by the Java
 * end-to-end tests. The expected cycle / instruction / stall counts below are
 * the same ones asserted by `EndToEndTests.testForwarding()` in
 * `src/test/java/org/edumips64/EndToEndTests.java`:
 *   - with forwarding    : 15 cycles, 10 instructions
 *   - without forwarding : 18 cycles, 10 instructions
 */
const FORWARDING_PROGRAM = `    .code
    dadd    r1, r2, r3
    dsub    r4, r1, r5
    nop
    nop
    nop
    nop
    nop
    lw      r1, 0(r0)
    dsub    r2, r1, r5
    syscall 0
`;

const EXPECTED_WITH_FORWARDING = { cycles: 15, instructions: 10 };
const EXPECTED_WITHOUT_FORWARDING = { cycles: 18, instructions: 10 };

/**
 * Equivalent to the "issue51-halt.s" resource; the expectations below match
 * `runForwardingTest("issue51-halt.s", 13, 17, 6)` in EndToEndTests.
 */
const ISSUE51_HALT_PROGRAM = `.code
          daddi   r2, r0, 2
start:    daddi   r2, r2, -1
          bne     r2, r0, start
          halt
`;

const EXPECTED_ISSUE51_WITH_FORWARDING = { cycles: 13, instructions: 6 };
const EXPECTED_ISSUE51_WITHOUT_FORWARDING = { cycles: 17, instructions: 6 };

/**
 * Clear all edumips64 localStorage keys before each test to ensure hermeticity.
 */
async function clearSettings(page) {
  await page.evaluate((prefix) => {
    const keysToRemove = [];
    for (let i = 0; i < window.localStorage.length; i++) {
      const k = window.localStorage.key(i);
      if (k && k.startsWith(prefix)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach((k) => window.localStorage.removeItem(k));
  }, STORAGE_PREFIX);
}

test.beforeEach(async ({ page }) => {
  await page.goto(targetUri);
  await clearSettings(page);
});

test.afterEach(async ({ page }) => {
  await clearSettings(page);
});

/**
 * Ensure the General Settings accordion is expanded. Mirrors the helper used
 * in `settings-persistence.spec.js`.
 */
async function openSettingsAccordion(page) {
  const summary = page.getByRole('button', { name: /General Settings/ });
  await summary.waitFor({ state: 'visible' });
  if ((await summary.getAttribute('aria-expanded')) !== 'true') {
    await summary.click();
  }
  await expect(summary).toHaveAttribute('aria-expanded', 'true');
}

/**
 * Toggle the "CPU Forwarding" switch to the desired state. Uses the
 * accessible label so the test exercises the same DOM the user sees.
 */
async function setForwarding(page, enabled) {
  await openSettingsAccordion(page);
  const forwardingSwitch = page.getByLabel('CPU Forwarding');
  await forwardingSwitch.waitFor({ state: 'visible' });
  const isChecked = await forwardingSwitch.isChecked();
  if (isChecked !== enabled) {
    await forwardingSwitch.click();
  }
  await expect(forwardingSwitch).toBeChecked({ checked: enabled });
}

/**
 * Read the execution stats rendered in the Statistics accordion.
 */
async function getExecutionStats(page) {
  const cycles = parseInt(
    (await page.locator('#stat-cycles').textContent()) || '0',
    10
  );
  const instructions = parseInt(
    (await page.locator('#stat-instructions').textContent()) || '0',
    10
  );
  const rawStalls = parseInt(
    (await page.locator('#stat-raw-stalls').textContent()) || '0',
    10
  );
  const wawStalls = parseInt(
    (await page.locator('#stat-waw-stalls').textContent()) || '0',
    10
  );
  const structuralStalls = parseInt(
    (await page.locator('#stat-structural-stalls').textContent()) || '0',
    10
  );
  return { cycles, instructions, rawStalls, wawStalls, structuralStalls };
}

/**
 * Test: Running `forwarding.s` without forwarding (the default) produces the
 * same cycle / instruction counts as the Java EndToEndTests.
 */
test('forwarding.s runs with 18 cycles when forwarding is disabled', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await setForwarding(page, false);

  await loadProgram(page, FORWARDING_PROGRAM);
  await runToCompletion(page);

  const stats = await getExecutionStats(page);
  expect(stats.cycles).toBe(EXPECTED_WITHOUT_FORWARDING.cycles);
  expect(stats.instructions).toBe(EXPECTED_WITHOUT_FORWARDING.instructions);
});

/**
 * Test: Running `forwarding.s` with forwarding enabled produces the same 15
 * cycles / 10 instructions asserted by EndToEndTests.
 */
test('forwarding.s runs with 15 cycles when forwarding is enabled', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await setForwarding(page, true);

  await loadProgram(page, FORWARDING_PROGRAM);
  await runToCompletion(page);

  const stats = await getExecutionStats(page);
  expect(stats.cycles).toBe(EXPECTED_WITH_FORWARDING.cycles);
  expect(stats.instructions).toBe(EXPECTED_WITH_FORWARDING.instructions);

  // And, like the Java test, enabling forwarding must reduce the cycle count
  // compared to the forwarding-disabled run.
  expect(stats.cycles).toBeLessThan(EXPECTED_WITHOUT_FORWARDING.cycles);
});

/**
 * Test: issue51-halt.s produces the expected cycle / instruction counts under
 * both forwarding settings, matching `runForwardingTest("issue51-halt.s",
 * 13, 17, 6)` in EndToEndTests.
 */
test('issue51-halt.s cycle counts match EndToEndTests under both forwarding settings', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  // First run: forwarding disabled.
  await setForwarding(page, false);
  await loadProgram(page, ISSUE51_HALT_PROGRAM);
  await runToCompletion(page);
  const statsNoFwd = await getExecutionStats(page);
  expect(statsNoFwd.cycles).toBe(EXPECTED_ISSUE51_WITHOUT_FORWARDING.cycles);
  expect(statsNoFwd.instructions).toBe(
    EXPECTED_ISSUE51_WITHOUT_FORWARDING.instructions
  );

  // Reload so the second run starts from a clean CPU state (the
  // forwarding setting is persisted, so we re-enable it explicitly
  // afterwards).
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  // Second run: forwarding enabled.
  await setForwarding(page, true);
  await loadProgram(page, ISSUE51_HALT_PROGRAM);
  await runToCompletion(page);
  const statsFwd = await getExecutionStats(page);
  expect(statsFwd.cycles).toBe(EXPECTED_ISSUE51_WITH_FORWARDING.cycles);
  expect(statsFwd.instructions).toBe(
    EXPECTED_ISSUE51_WITH_FORWARDING.instructions
  );

  // Sanity check: the same program halts in fewer cycles with forwarding on.
  expect(statsFwd.cycles).toBeLessThan(statsNoFwd.cycles);
});

/**
 * Test: The forwarding switch persists across page reloads and is reflected
 * in localStorage.
 */
test('forwarding switch persists across page reloads', async ({ page }) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  await setForwarding(page, true);

  const stored = await page.evaluate(
    (key) => window.localStorage.getItem(key),
    `${STORAGE_PREFIX}forwarding`
  );
  expect(stored).toBe('true');

  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  await openSettingsAccordion(page);
  const forwardingSwitch = page.getByLabel('CPU Forwarding');
  await expect(forwardingSwitch).toBeChecked();
});

/**
 * Test: The forwarding setting persists across browser sessions, and running
 * the same program in a "new session" (page reload) yields the same cycle
 * and stall counts as the previous session. This is a stronger guarantee
 * than the pure persistence test above: it proves that the persisted value
 * is actually applied to the simulator on startup.
 */
test('forwarding is applied on a fresh session and yields identical cycle/stall counts', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  // Session 1: enable forwarding and run the program.
  await setForwarding(page, true);
  await loadProgram(page, FORWARDING_PROGRAM);
  await runToCompletion(page);
  const session1Stats = await getExecutionStats(page);
  expect(session1Stats.cycles).toBe(EXPECTED_WITH_FORWARDING.cycles);

  // Simulate a new browser session by reloading the page. The forwarding
  // setting must be restored from localStorage before the worker runs the
  // program, otherwise the cycle count will jump to the no-forwarding value.
  await page.reload();
  await waitForPageReady(page);
  await removeOverlay(page);

  // Do NOT toggle the switch — the test is that the restored value is what
  // drives the CPU. Just confirm it is still "on".
  await openSettingsAccordion(page);
  await expect(page.getByLabel('CPU Forwarding')).toBeChecked();

  await loadProgram(page, FORWARDING_PROGRAM);
  await runToCompletion(page);
  const session2Stats = await getExecutionStats(page);

  // Identical cycles, instructions and stalls across sessions.
  expect(session2Stats.cycles).toBe(session1Stats.cycles);
  expect(session2Stats.instructions).toBe(session1Stats.instructions);
  expect(session2Stats.rawStalls).toBe(session1Stats.rawStalls);
  expect(session2Stats.wawStalls).toBe(session1Stats.wawStalls);
  expect(session2Stats.structuralStalls).toBe(session1Stats.structuralStalls);

  // And specifically the forwarding-enabled value, not the default one.
  expect(session2Stats.cycles).toBe(EXPECTED_WITH_FORWARDING.cycles);
  expect(session2Stats.cycles).toBeLessThan(EXPECTED_WITHOUT_FORWARDING.cycles);
});

/**
 * Test: The forwarding switch cannot be toggled while a program is running.
 * The switch's disabled state is the only UI affordance that prevents the
 * user from changing forwarding mid-execution (which would otherwise reset
 * the CPU and discard their simulation state).
 */
test('forwarding switch is disabled while a program is running', async ({
  page,
}) => {
  await waitForPageReady(page);
  await removeOverlay(page);

  // Load a program that will not finish on its own in the short window we
  // need to observe the RUNNING state. The loop counts r1 down from 100 to
  // 0 and then exits through SYSCALL 0; between `load` and the first step,
  // the simulator is in RUNNING (program loaded, not yet completed), which
  // is exactly what we want to assert against.
  const longProgram = `.code
DADDI r1, r0, 100
loop:
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;

  await setForwarding(page, false);
  await loadProgram(page, longProgram);
  // At this point the simulator is in the RUNNING state (program loaded, but
  // not yet completed). `loadProgram` waits for `#step-button:not([disabled])`
  // which is exactly the RUNNING condition.

  await openSettingsAccordion(page);
  const forwardingSwitch = page.getByLabel('CPU Forwarding');

  // The switch must be disabled while the simulator is RUNNING.
  await expect(forwardingSwitch).toBeDisabled();

  // Try to flip it anyway; the state must not change. Playwright's `.click()`
  // with `force: true` bypasses actionability checks, which lets us assert
  // that even a forced click is ignored by the disabled <input>.
  await forwardingSwitch.click({ force: true });
  await expect(forwardingSwitch).not.toBeChecked();

  // Stopping the simulation must re-enable the switch, so the user can
  // change forwarding for the next run.
  await page.click('#stop-button');
  await expect(forwardingSwitch).toBeEnabled();
});
