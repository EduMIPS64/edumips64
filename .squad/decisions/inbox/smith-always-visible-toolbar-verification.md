# Smith QA Verdict: Always-Present Toolbar Buttons (PR #1835)

**Date:** 2026-06-09T15:41:52+02:00  
**Branch:** squad/streamline-run-controls  
**Commit:** 207827ba  
**Spec:** src/test/webapp/contextual-controls.spec.js  

---

## VERDICT: PASS ✅

Trinity's new `RunControlsToolbar.js` architecture — all five execution buttons
always present in the DOM when the toolbar is visible, enabled/disabled rather
than rendered/absent — is correctly implemented and verified by the test suite.

---

## Implementation correctness

| Check | Result |
|-------|--------|
| Toolbar absent in EMPTY | ✅ |
| Toolbar absent in ENDED | ✅ |
| READY: step/multi-step/run/stop **enabled** | ✅ |
| READY: pause **visible + disabled** | ✅ |
| EXECUTING: pause **enabled** | ✅ |
| EXECUTING: step/multi-step/run **visible + disabled** | ✅ |
| EXECUTING: stop **visible + disabled** ("Pause before stopping") | ✅ |
| Load button always in header | ✅ |

---

## Test changes required (test bugs, not impl bugs)

All fixes were in the test code. No implementation bugs found.

### 1. READY pause: `toBeHidden()` → `toBeVisible() + toBeDisabled()`
Old model had pause absent in READY; new model has it present but disabled.

### 2. EXECUTING step/multi-step/run: `toBeHidden()` → `toBeVisible() + toBeDisabled()`
Old model had these absent in EXECUTING; new model has them present but disabled.

### 3. EXECUTING entry signal
`waitForSelector('#pause-button', {state:'visible'})` was insufficient — pause
is now visible even in READY (just disabled). Fixed to:
`waitForSelector('#pause-button:not([disabled])')`.

### 4. EXECUTING teardown
`waitForSelector('#step-button', {state:'visible'})` was insufficient — step is
now always visible. Fixed to: `waitForSelector('#step-button:not([disabled])')`.

### 5. test-utils.js JSDoc
Updated `waitForRunningState()` comment to explain the always-present model.
The selector `#step-button:not([disabled])` remains valid — it correctly
discriminates READY (enabled) from EXECUTING (disabled).

---

## Full suite results

| Spec | Result |
|------|--------|
| contextual-controls.spec.js (8 tests) | ✅ All pass |
| settings-persistence.spec.js (6 tests) | ✅ All pass (no changes needed) |
| All other specs (55 tests) | ✅ Pass |
| cache-simulator.spec.js:125 | ❌ GPU crash (pre-existing env flake) |
| contextual-controls.spec.js drag test | ⏭ Skipped (intentional, deferred) |

**Total: 69/71 pass, 1 skipped, 1 pre-existing GPU crash.**

---

## No implementation bugs to report

Trinity's implementation in `RunControlsToolbar.js` is correct:
- `deriveLogicalState()` mapping correct
- `stepDisabled = logicalState !== 'READY'` — correct
- `pauseDisabled = logicalState !== 'EXECUTING'` — correct
- `stopDisabled = logicalState === 'EXECUTING'` — correct ("Pause before stopping" tooltip)
- Early return `null` for EMPTY/ENDED/WAITING_FOR_INPUT — correct
