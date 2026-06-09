/**
 * Contextual run controls — spec for the floating RunControlsToolbar feature.
 *
 * Asserts the §3.2 / §3.3 state → controls matrix.
 *
 * Architecture (as implemented by Trinity in RunControlsToolbar.js):
 *   - #load-button lives in the Header and is ALWAYS visible in the DOM.
 *   - Execution controls (step, multi-step, run, pause, stop) live inside the
 *     floating #run-controls-toolbar overlay mounted from Simulator.js.
 *   - The toolbar itself is NOT rendered (returns null) in EMPTY, ENDED, and
 *     WAITING_FOR_INPUT states.
 *
 * Matrix summary (execution controls):
 *   EMPTY      : Load ✅  #run-controls-toolbar 🚫  Step 🚫  MultiStep 🚫  Run 🚫  Pause 🚫  Stop 🚫
 *   READY      : Load ✅  #run-controls-toolbar ✅  Step ✅  MultiStep ✅  Run ✅  Pause 🔒(disabled)  Stop ✅
 *   EXECUTING  : Load ✅  #run-controls-toolbar ✅  Step 🔒  MultiStep 🔒  Run 🔒  Pause ✅  Stop 🔒 (disabled)
 *   ENDED      : Load ✅  #run-controls-toolbar 🚫  Step 🚫  MultiStep 🚫  Run 🚫  Pause 🚫  Stop 🚫
 *
 * When the toolbar IS visible (READY or EXECUTING), ALL FIVE buttons are
 * always present in the DOM — they are greyed-out (disabled) rather than
 * removed when not actionable.  Use toBeDisabled() / toBeEnabled() rather
 * than toBeHidden() / toBeVisible() to check actionability within a
 * visible toolbar.
 *
 * Editor controls (clear, restore-sample, open-code, save-code, help) are
 * always visible in every state (may be disabled during EXECUTING, but the
 * visibility contract is what we test here).
 *
 * Hidden controls MUST use conditional render or `display:none` — NOT
 * `opacity:0` or `visibility:hidden`, which Playwright still considers
 * visible. `toBeHidden()` matches both "not in DOM" and `display:none`.
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
  openProgramMenu,
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

test('EMPTY: load-button visible; run-controls-toolbar absent; step/multi-step/run/pause/stop hidden', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // Load button lives in the header and is always visible.
  await expect(page.locator('#load-button')).toBeVisible();

  // The floating toolbar is not rendered in EMPTY state.
  await expect(page.locator('#run-controls-toolbar')).toBeHidden();

  // All execution controls (inside the toolbar) must also be hidden.
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#multi-step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#stop-button')).toBeHidden();
});

test('EMPTY: program-menu-button visible and enabled; help-button visible', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);

  // §3.3 — Program menu button is always visible; items live in a portal and
  // are only present in the DOM while the menu is open.
  await expect(page.locator('#program-menu-button')).toBeVisible();
  await expect(page.locator('#program-menu-button')).toBeEnabled();

  // Open the menu and verify all four items are present, then close.
  await openProgramMenu(page);
  await expect(page.locator('#clear-code-button')).toBeVisible();
  await expect(page.locator('#load-code-button')).toBeVisible();
  await expect(page.locator('#save-code-button')).toBeVisible();
  await expect(page.locator('#restore-sample-button')).toBeVisible();
  await page.keyboard.press('Escape');

  await expect(page.locator('#help-button')).toBeVisible();
});

// ─── READY state ─────────────────────────────────────────────────────────────

test('READY: run-controls-toolbar visible; step/multi-step/run/stop visible & enabled; pause visible but disabled; load still visible', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);

  // The floating toolbar must appear in READY state.
  await page.waitForSelector('#run-controls-toolbar');
  await expect(page.locator('#run-controls-toolbar')).toBeVisible();

  // Execution controls that apply to READY must be visible AND enabled.
  await expect(page.locator('#step-button')).toBeVisible();
  await expect(page.locator('#step-button')).toBeEnabled();

  await expect(page.locator('#multi-step-button')).toBeVisible();
  await expect(page.locator('#multi-step-button')).toBeEnabled();

  await expect(page.locator('#run-button')).toBeVisible();
  await expect(page.locator('#run-button')).toBeEnabled();

  await expect(page.locator('#stop-button')).toBeVisible();
  await expect(page.locator('#stop-button')).toBeEnabled();

  // Load button remains visible in READY (it's always in the header).
  await expect(page.locator('#load-button')).toBeVisible();

  // Pause has no meaning in READY — present in the toolbar but disabled.
  await expect(page.locator('#pause-button')).toBeVisible();
  await expect(page.locator('#pause-button')).toBeDisabled();
});

test('READY: program-menu-button visible and enabled; menu items visible when open', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);

  // Program menu button must be visible and enabled in READY.
  await expect(page.locator('#program-menu-button')).toBeVisible();
  await expect(page.locator('#program-menu-button')).toBeEnabled();

  // Open the menu and verify all four items are present, then close.
  await openProgramMenu(page);
  await expect(page.locator('#clear-code-button')).toBeVisible();
  await expect(page.locator('#load-code-button')).toBeVisible();
  await expect(page.locator('#save-code-button')).toBeVisible();
  await expect(page.locator('#restore-sample-button')).toBeVisible();
  await page.keyboard.press('Escape');

  await expect(page.locator('#help-button')).toBeVisible();
});

// ─── EXECUTING state ──────────────────────────────────────────────────────────
//
// Catching EXECUTING reliably is challenging because a short program
// completes before Playwright can observe the transient state.  We use a
// 10 000-iteration loop to give ourselves a comfortable window, then wait
// for #pause-button to become ENABLED as the definitive signal that the
// simulator is in EXECUTING.  (In the always-present model, pause is
// visible in READY too — just disabled — so `state:'visible'` is not a
// sufficient signal; we must check `:not([disabled])` instead.)
//
// If the host machine is so fast that even 10 000 iterations complete before
// the assertion runs, this test will time out on `waitForSelector` (not
// produce a false positive).  In that case increase the loop counter or use
// the WAITING_FOR_INPUT trick with a syscall 3 program as an alternative
// stable "executing" signal — see note in decisions/inbox/.
//
// Stop is DISABLED (not hidden) in EXECUTING per §3.2 footnote ¹ —
// tooltip "Pause before stopping".  Pause is enabled.  All other buttons
// (step, multi-step, run) remain in the DOM but are disabled — the toolbar
// always renders all five when visible.

test('EXECUTING: pause visible & enabled; stop visible but disabled; step/multi-step/run visible but disabled; load-button still visible in header', async ({
  page,
}) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, longProgram);
  await removeOverlay(page);

  // Toolbar is present in READY — wait for it before clicking run.
  await page.waitForSelector('#run-controls-toolbar');

  // Start execution.
  await page.click('#run-button');

  // Wait until the UI enters EXECUTING: #pause-button becomes enabled.
  // (In the always-present model, pause is visible even in READY but disabled;
  // we must wait for it to be enabled as the definitive EXECUTING signal.)
  await page.waitForSelector('#pause-button:not([disabled])', { timeout: 10000 });

  // ── assertions while EXECUTING ──
  await expect(page.locator('#pause-button')).toBeVisible();
  await expect(page.locator('#pause-button')).toBeEnabled();

  // Stop is visible but disabled ("Pause before stopping").
  await expect(page.locator('#stop-button')).toBeVisible();
  await expect(page.locator('#stop-button')).toBeDisabled();

  // Controls inapplicable during EXECUTING are present in the toolbar but disabled.
  await expect(page.locator('#step-button')).toBeVisible();
  await expect(page.locator('#step-button')).toBeDisabled();
  await expect(page.locator('#multi-step-button')).toBeVisible();
  await expect(page.locator('#multi-step-button')).toBeDisabled();
  await expect(page.locator('#run-button')).toBeVisible();
  await expect(page.locator('#run-button')).toBeDisabled();

  // Load button lives in the header — always visible regardless of state.
  await expect(page.locator('#load-button')).toBeVisible();

  // Program menu button is visible but DISABLED during EXECUTING — the user
  // cannot modify the program while the CPU is running.
  await expect(page.locator('#program-menu-button')).toBeVisible();
  await expect(page.locator('#program-menu-button')).toBeDisabled();

  // Pause the simulation — avoids waiting for natural completion, which would
  // time out for a 10 000-iteration loop on the test machine.  Clicking pause
  // transitions the simulator back to READY (pause/run buttons swap), making
  // the stop button enabled so we can reset cleanly.
  await page.click('#pause-button');
  // Wait for READY state: step button becomes enabled (it's always present, just
  // disabled during EXECUTING), then reset cleanly.
  await page.waitForSelector('#step-button:not([disabled])', { timeout: 10000 });
  // Reset the simulator so subsequent tests start from a clean EMPTY state.
  await page.click('#stop-button');
});

// ─── ENDED state ─────────────────────────────────────────────────────────────

test('ENDED: load-button visible; run-controls-toolbar absent; step/multi-step/run/pause/stop hidden', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);
  await runToCompletion(page);

  // The floating toolbar is not rendered in ENDED state.
  await expect(page.locator('#run-controls-toolbar')).toBeHidden();

  // Execution controls must all be hidden after the simulation ends.
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#multi-step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#stop-button')).toBeHidden();

  // Load remains visible — user can re-parse and rerun.
  await expect(page.locator('#load-button')).toBeVisible();
});

test('ENDED: program-menu-button visible and enabled; help-button visible', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);
  await runToCompletion(page);

  // Program menu button must be visible and enabled after simulation ends.
  await expect(page.locator('#program-menu-button')).toBeVisible();
  await expect(page.locator('#program-menu-button')).toBeEnabled();

  // Open the menu and verify all four items, then close.
  await openProgramMenu(page);
  await expect(page.locator('#clear-code-button')).toBeVisible();
  await expect(page.locator('#load-code-button')).toBeVisible();
  await expect(page.locator('#save-code-button')).toBeVisible();
  await expect(page.locator('#restore-sample-button')).toBeVisible();
  await page.keyboard.press('Escape');

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
  await expect(page.locator('#run-controls-toolbar')).toBeHidden();
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#stop-button')).toBeHidden();

  // ── READY (after loadProgram) ──
  await loadProgram(page, simpleProgram);
  await page.waitForSelector('#run-controls-toolbar');
  await expect(page.locator('#run-controls-toolbar')).toBeVisible();
  await expect(page.locator('#step-button')).toBeVisible();
  await expect(page.locator('#step-button')).toBeEnabled();
  await expect(page.locator('#run-button')).toBeVisible();
  await expect(page.locator('#run-button')).toBeEnabled();
  await expect(page.locator('#stop-button')).toBeVisible();
  await expect(page.locator('#stop-button')).toBeEnabled();
  await expect(page.locator('#pause-button')).toBeVisible();
  await expect(page.locator('#pause-button')).toBeDisabled();
  await expect(page.locator('#load-button')).toBeVisible();

  // ── ENDED (after runToCompletion) ──
  await runToCompletion(page);
  await expect(page.locator('#run-controls-toolbar')).toBeHidden();
  await expect(page.locator('#step-button')).toBeHidden();
  await expect(page.locator('#run-button')).toBeHidden();
  await expect(page.locator('#stop-button')).toBeHidden();
  await expect(page.locator('#pause-button')).toBeHidden();
  await expect(page.locator('#load-button')).toBeVisible();

  // Program menu button and help survive all transitions; items only visible when menu is open.
  await expect(page.locator('#program-menu-button')).toBeVisible();
  await expect(page.locator('#help-button')).toBeVisible();
});

// ─── Draggable toolbar ────────────────────────────────────────────────────────
//
// NOTE: Skipped — dragging via pointer events in headless Chromium is brittle.
// The pointer-capture approach in RunControlsToolbar.js works reliably in
// manual testing but the synthetic PointerEvent sequence in Playwright
// (pointerdown → pointermove → pointerup) does not reliably trigger
// `setPointerCapture` in the snap Chromium build on this host.
// A robust drag test would require a dedicated Playwright fixture that
// bypasses the pointer-capture limitation. Deferred.
test.skip('toolbar is draggable: position changes after drag gesture', async ({ page }) => {
  await page.goto(targetUri);
  await waitForPageReady(page);
  await loadProgram(page, simpleProgram);

  await page.waitForSelector('#run-controls-toolbar');
  const toolbar = page.locator('#run-controls-toolbar');
  const handle = toolbar.locator('[aria-label="Drag toolbar"]');

  const before = await toolbar.boundingBox();
  await handle.dispatchEvent('pointerdown', { clientX: before.x + 10, clientY: before.y + 10 });
  await handle.dispatchEvent('pointermove', { clientX: before.x + 60, clientY: before.y + 60 });
  await handle.dispatchEvent('pointerup', {});
  const after = await toolbar.boundingBox();

  // Position should have changed.
  expect(after.x).not.toBeCloseTo(before.x, -1);
});
