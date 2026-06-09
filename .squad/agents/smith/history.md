# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

## 2026-06-09 — Program ▾ dropdown menu tests (squad/program-menu)

### MUI Menu portal gotcha
MUI `<Menu>` renders its items in a React portal **only while the menu is open**. When the menu is closed, `#clear-code-button`, `#load-code-button`, `#save-code-button`, and `#restore-sample-button` are **not in the DOM**. Any test that clicks or asserts these must first open the menu. `toHaveCount(0)` is the correct assertion for "item absent" when the menu is closed.

### New test helpers (test-utils.js)
- `openProgramMenu(page)` — clicks `#program-menu-button` and waits for `#program-menu` to be visible (portal renders).
- `clickProgramMenuItem(page, id)` — opens the menu then clicks the item (clicking an item auto-closes the menu).

### Readiness signal change (waitForSimulationComplete)
Changed from `#clear-code-button:not([disabled])` to `#program-menu-button:not([disabled])`. The Program menu button is always in the DOM and is disabled during EXECUTING/WAITING_FOR_INPUT, so it is the correct readiness discriminator now that the individual items live in a portal.

### Specs updated
- `test-utils.js` — two new helpers + waitForSimulationComplete fix
- `clear-button.spec.js` — all three `page.click('#clear-code-button')` calls wrapped with `clickProgramMenuItem`
- `editor-persistence.spec.js` — `#restore-sample-button` click wrapped with `clickProgramMenuItem`
- `syntax-highlighting-during-run.spec.js` — isEnabled() polling switched to `#program-menu-button`
- `contextual-controls.spec.js` — "editor controls" tests reworked; EXECUTING test asserts button disabled; lifecycle test updated
- `program-menu.spec.js` (new) — focused coverage: items absent when closed, button disabled in EXECUTING, re-enables after

### Local run result (commit f6e54235)
16 passed, 1 skipped, 1 pre-existing tooltip-intercept flake (`clear button removes accordion change markers` — Stats accordion click intercepted by MUI Tooltip, unrelated to Program menu changes; was failing on the branch before my edits too).



### What changed (Trinity's latest architecture)
- `RunControlsToolbar.js`: All five execution buttons (step, multi-step, run, pause, stop) are now **always rendered** when the toolbar is visible, greyed out (disabled) rather than absent from the DOM.
- Previous model: READY → pause absent; EXECUTING → step/multi-step/run absent.
- New model: READY → pause present but disabled; EXECUTING → step/multi-step/run/stop present but disabled.
- Toolbar visibility rule unchanged: not rendered in EMPTY / ENDED / WAITING_FOR_INPUT.

### Test fixes applied (contextual-controls.spec.js)

**READY test:**
- `#pause-button toBeHidden()` → `toBeVisible() + toBeDisabled()`

**EXECUTING test:**
- `#step-button toBeHidden()` → `toBeVisible() + toBeDisabled()`
- `#multi-step-button toBeHidden()` → `toBeVisible() + toBeDisabled()`
- `#run-button toBeHidden()` → `toBeVisible() + toBeDisabled()`
- EXECUTING entry signal: `waitForSelector('#pause-button', {state:'visible'})` →
  `waitForSelector('#pause-button:not([disabled])')` — pause is visible (just disabled) in READY too, so visibility alone was no longer a sufficient EXECUTING signal.
- EXECUTING teardown: `waitForSelector('#step-button', {state:'visible'})` →
  `waitForSelector('#step-button:not([disabled])')` — step is now always visible; enabled state is the READY discriminator.

**Lifecycle test:**
- READY section: `#pause-button toBeHidden()` → `toBeVisible() + toBeDisabled()`

**test-utils.js:**
- Updated `waitForRunningState()` JSDoc: selector `#step-button:not([disabled])` is still correct; step is always present when toolbar visible but disabled in EXECUTING, so `:not([disabled])` correctly discriminates READY.

### Full suite results (commit 207827ba)
| # | Result | Root cause |
|---|--------|------------|
| 1 | `cache-simulator.spec.js:125` FAIL | GPU crash — pre-existing env issue (snap Chromium, Ubuntu 26.04) |

**69 passed, 1 skipped (drag test), 1 pre-existing GPU crash. VERDICT: PASS ✅**

### Key learnings
- When buttons are always-present (just disabled), `state:'visible'` is not a sufficient discriminator for state transitions. Must use `:not([disabled])` selectors.
- `toBeDisabled()` in Playwright correctly targets the `disabled` attribute on `<button>` elements — MUI IconButton's `disabled={true}` prop sets the HTML attribute on the rendered `<button>`.
- `toBeHidden()` remains correct for toolbar-level assertions (toolbar not rendered → null → element absent from DOM).


## 2026-06-09: Program Menu Tests + Toolbar Verification — Completed

**Status:** Tests reworked, verdicts rendered

Program menu test rework (16 passed, APPROVE). Always-visible toolbar verification (69/71 pass, PASS verdict). PR #1836 ready.



## 2026-06-09: Program Menu Gating Contract Update — Completed

**Status:** Contract tightened by Andrea; tests updated and all green.

### New gating contract for #program-menu-button

| Logical state | CPU status | Button |
|---|---|---|
| EMPTY | READY (no program) | **ENABLED** |
| READY | RUNNING + !executing | **DISABLED** |
| EXECUTING | RUNNING + executing | **DISABLED** |
| WAITING_FOR_INPUT | inputRequest active | **DISABLED** |
| ENDED | STOPPED | **ENABLED** |

The button is enabled **only** in EMPTY and ENDED. It is disabled whenever
a program is loaded into the simulator — even when paused (READY) — not
just during active execution.

### Reset-via-#stop-button workflow

To reuse the Program menu after loading a program (READY state), the user
(or test) must click `#stop-button` in the floating run toolbar to reset
the simulator back to EMPTY. The button is then re-enabled.

**Test pattern:**
```js
await loadProgram(page, prog);
await expect(page.locator('#program-menu-button')).toBeDisabled(); // READY
// ... run / pause ...
await page.click('#stop-button');  // → EMPTY
await page.waitForSelector('#program-menu-button:not([disabled])');
await expect(page.locator('#program-menu-button')).toBeEnabled();
```

`resetSimulator(page)` helper added to test-utils.js encapsulates this
pattern.

### Suite results (commit cacbf90e)

73 passed, 1 skipped (known drag test). Zero failures. VERDICT: PASS ✅
