# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

## 2026-06-09 — Always-visible toolbar buttons (PR #1835, iteration 3)

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

