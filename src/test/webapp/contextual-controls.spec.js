/**
 * Contextual run controls — anticipatory spec for the contextual-controls feature.
 *
 * Asserts the §3.2 / §3.3 state → controls matrix from:
 *   run-controls-design.md (authored by Morpheus, finalized 2026-06-09)
 *
 * Matrix summary (execution controls):
 *   EMPTY      : Load ✅  Step 🚫  MultiStep 🚫  Run 🚫  Pause 🚫  Stop 🚫
 *   READY      : Load ✅  Step ✅  MultiStep ✅  Run ✅  Pause 🚫  Stop ✅
 *   EXECUTING  : Load 🚫  Step 🚫  MultiStep 🚫  Run 🚫  Pause ✅  Stop 🔒 (disabled)
 *   ENDED      : Load ✅  Step 🚫  MultiStep 🚫  Run 🚫  Pause 🚫  Stop 🚫
 *
 * Editor controls (clear, restore-sample, open-code, save-code, help) are
 * always visible in every state (may be disabled during EXECUTING, but the
 * visibility contract is what we test here).
 *
 * NOTE: These tests are authored ANTICIPATORILY — they are written against the
 * design contract before Trinity's implementation is complete. They will fail
 * against the pre-implementation UI (buttons are currently always visible) and
 * are expected to go green once contextual rendering lands in Header.js.
 *
 * Implementation note (for Trinity):
 *   Hidden controls MUST use conditional render or `display:none` — NOT
 *   `opacity:0` or `visibility:hidden`, which Playwright still considers
 *   visible. `toBeHidden()` matches both "not in DOM" and `display:none`.
 */

'use strict';

const { test, expect } = require('./fixtures');
const {
  targetUri,
  waitForPageReady,
  loadProgram,
  runToCompletion,
  removeOverlay,
  waitForSimulationComplete,
} = require('./test-utils');

// Minimal valid program — completes quickly (SYSCALL 0).
const simpleProgram = `.code
DADDI r1, r0, 1
SYSCALL 0
`;

// Long-running program — 10 000 loop iterations — used to catch the
// transient EXECUTING state reliably before the simulation finishes.
const longProgram = `.code
DADDI r1, r0, 10000
loop:
DADDI r1, r1, -1
BNEZ r1, loop
SYSCALL 0
`;

// ─── EMPTY state ─────────────────────────────────────────────────────────────

test('EMPTY: load-button visible; step/multi-step/run/pause/stop hidden', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // Load button must be visible in EMPTY state (it's always visible).
  await expect(page.locator('#load-button')).toBeVisible();

  // All execution controls that don't apply to EMPTY must be hidden.
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#multi-step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#stop-button')).toBeHidden();
});

test('EMPTY: editor controls (clear, restore-sample, open-code, save-code, help) are visible', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // §3.3 — editor controls are always visible.
  await expect(page.locator('#clear-code-button')).toBeVisible();
  await expect(page.locator('#restore-sample-button')).toBeVisible();
  await expect(page.locator('#load-code-button')).toBeVisible();
  await expect(page.locator('#save-code-button')).toBeVisible();
  await expect(page.locator('#help-button')).toBeVisible();
});

// ─── READY state ─────────────────────────────────────────────────────────────

test('READY: step/multi-step/run/stop visible & enabled; pause hidden; load still visible', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);

  // Execution controls that apply to READY must be visible AND enabled.
  await expect(page.locator('#step-button')).toBeVisible();
  await expect(page.locator('#step-button')).toBeEnabled();

  await expect(page.locator('#multi-step-button')).toBeVisible();
  await expect(page.locator('#multi-step-button')).toBeEnabled();

  await expect(page.locator('#run-button')).toBeVisible();
  await expect(page.locator('#run-button')).toBeEnabled();

  await expect(page.locator('#stop-button')).toBeVisible();
  await expect(page.locator('#stop-button')).toBeEnabled();

  // Load button remains visible in READY (user can reload/re-parse).
  await expect(page.locator('#load-button')).toBeVisible();

  // Pause has no meaning in READY — must be hidden.
  await expect(page.locator('#pause-button')).toBeHidden();
});

test('READY: editor controls remain visible after loading a program', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);

  await expect(page.locator('#clear-code-button')).toBeVisible();
  await expect(page.locator('#restore-sample-button')).toBeVisible();
  await expect(page.locator('#load-code-button')).toBeVisible();
  await expect(page.locator('#save-code-button')).toBeVisible();
  await expect(page.locator('#help-button')).toBeVisible();
});

// ─── EXECUTING state ──────────────────────────────────────────────────────────
//
// Catching EXECUTING reliably is challenging because a short program
// completes before Playwright can observe the transient state.  We use a
// 10 000-iteration loop to give ourselves a comfortable window, then wait
// for #pause-button to become visible as the definitive signal that the
// simulator is in EXECUTING.
//
// If the host machine is so fast that even 10 000 iterations complete before
// the assertion runs, this test will time out on `waitForSelector` (not
// produce a false positive).  In that case increase the loop counter or use
// the WAITING_FOR_INPUT trick with a syscall 3 program as an alternative
// stable "executing" signal — see note in decisions/inbox/.
//
// Stop is DISABLED (not hidden) in EXECUTING per §3.2 footnote ¹ —
// tooltip "Pause before stopping".  This avoids rewriting cancellation logic
// while still communicating intent.

test('EXECUTING: pause visible & enabled; stop visible but disabled; step/multi-step/run/load hidden', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, longProgram);
  await removeOverlay(page);

  // Start execution.
  await page.click('#run-button');

  // Wait until the UI enters EXECUTING: #pause-button is rendered and visible.
  await page.waitForSelector('#pause-button', { state: 'visible', timeout: 10000 });

  // ── assertions while EXECUTING ──
  await expect(page.locator('#pause-button')).toBeVisible();
  await expect(page.locator('#pause-button')).toBeEnabled();

  // Stop is visible but disabled ("Pause before stopping").
  await expect(page.locator('#stop-button')).toBeVisible();
  await expect(page.locator('#stop-button')).toBeDisabled();

  // Controls inapplicable during EXECUTING must be hidden.
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#multi-step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#load-button')).toBeHidden();

  // Pause the simulation — avoids waiting for natural completion, which would
  // time out for a 10 000-iteration loop on the test machine.  Clicking pause
  // transitions the simulator back to READY (pause/run buttons swap), making
  // the stop button enabled so we can reset cleanly.
  await page.click('#pause-button');
  // Wait for READY state: step button re-appears, pause button disappears.
  await page.waitForSelector('#step-button', { state: 'visible', timeout: 10000 });
  // Reset the simulator so subsequent tests start from a clean EMPTY state.
  await page.click('#stop-button');
});

// ─── ENDED state ─────────────────────────────────────────────────────────────

test('ENDED: load-button visible; step/multi-step/run/pause/stop hidden', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);
  await runToCompletion(page);

  // Execution controls must all be hidden after the simulation ends.
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#multi-step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#stop-button')).toBeHidden();

  // Load remains visible — user can re-parse and rerun.
  await expect(page.locator('#load-button')).toBeVisible();
});

test('ENDED: editor controls remain visible after simulation completes', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);
  await runToCompletion(page);

  await expect(page.locator('#clear-code-button')).toBeVisible();
  await expect(page.locator('#restore-sample-button')).toBeVisible();
  await expect(page.locator('#load-code-button')).toBeVisible();
  await expect(page.locator('#save-code-button')).toBeVisible();
  await expect(page.locator('#help-button')).toBeVisible();
});

// ─── Full lifecycle ───────────────────────────────────────────────────────────
//
// A single test that walks through EMPTY → READY → ENDED in one page load,
// asserting key control visibility at each transition.

test('lifecycle: EMPTY → READY → ENDED control transitions', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // ── EMPTY ──
  await expect(page.locator('#load-button')).toBeVisible();
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#stop-button')).toBeHidden();

  // ── READY (after loadProgram) ──
  await loadProgram(page, simpleProgram);
  await expect(page.locator('#step-button')).toBeVisible();
  await expect(page.locator('#step-button')).toBeEnabled();
  await expect(page.locator('#run-button')).toBeVisible();
  await expect(page.locator('#run-button')).toBeEnabled();
  await expect(page.locator('#stop-button')).toBeVisible();
  await expect(page.locator('#stop-button')).toBeEnabled();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#load-button')).toBeVisible();

  // ── ENDED (after runToCompletion) ──
  await runToCompletion(page);
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#stop-button')).toBeHidden();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#load-button')).toBeVisible();

  // Editor controls survive all transitions.
  await expect(page.locator('#clear-code-button')).toBeVisible();
  await expect(page.locator('#help-button')).toBeVisible();
});
