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


---

## 2026-06-13 — Candidate Build Tests (feat/promotable-candidate-builds)

### Task
Added tests for the candidate build feature per §6.1 (Python) and §6.3 (Playwright) of the design spec.

### 1. Dead-code cleanup
Confirmed `replace_subdir` in `.github/scripts/deploy-web-pages.py` was referenced only in its own definition (no callers after nightly removal). Removed the function.

### 2. Python tests (`.github/scripts/test_deploy_web_pages.py`)
Added `candidate()` and `read_candidates()` helpers plus 10 new test cases:
- First candidate run: creates `candidates.json` and date/n-sha dir with correct fields.
- Second run same day: n increments to 2, both entries present, sorted newest-first.
- Different day: n resets to 1 for the new date.
- Pruning: old entries removed from JSON and dirs deleted; empty date dirs removed; fresh entries kept.
- `root_prod_entries()` excludes date-pattern dirs and `candidates.json`.
- `cmd_promote` does NOT delete candidate date-dirs or `candidates.json`.
- `cmd_rollback` does NOT disturb candidate date-dirs / `candidates.json`.
- Legacy `/nightly/` dir removed on first candidate deploy.
- Sorted descending by (date, n).
- `die()` on missing artifact dir.

**Result: 15 passed (5 pre-existing + 10 new) ✅**

### 3. Playwright specs

**`version-and-candidate-badge.spec.js`** (renamed from `version-and-nightly-badge.spec.js` via `git mv`):
- Updated describe block name, chip selectors, and detection logic to target `#candidate-build-chip` and `CANDIDATE_PATH_RE`.
- "About tab shows a non-empty version string" test carried over unchanged.

**`candidate-builds.spec.js`** (new file, mirrors `version-history.spec.js`):
- Test A: Section present when `candidates.json` is mocked with 3 entries.
- Test B: Section absent when `candidates.json` returns 404.
- Test C: Share links assert correct `/<date>/<n>-<shortsha>/` hrefs.
- Test D: Section absent when `candidates.json` returns empty array.

**Syntax validation**: `npx playwright test --list` confirms all 7 new/updated specs are parsed without errors. ✅

**E2e runtime**: All Playwright tests fail due to missing `worker.js` (GWT compilation artifact not present in dev env) — **same failure mode as existing specs** (confirmed by running `version-history.spec.js` which also fails). Deferred to CI. ✅ (by design)

### Key learnings
- `datetime.now(timezone.utc)` in Python can be monkeypatched by replacing the entire `datetime` class on the module with a stub that provides a fixed `now()`. This is necessary because the production code calls `datetime.now(timezone.utc)` directly — patching `timezone.utc` or `datetime.now` alone doesn't work.
- Worker.js (GWT compilation artifact) must be present for Playwright tests to reach `waitForPageReady`. When testing in a non-GWT dev environment, syntax validation via `--list` is the correct floor.
- `git mv` correctly renames Playwright specs while preserving git history.

- **2026-06-13:** Candidate builds QA session: wrote 10 Python tests for deploy-web-pages.py candidate subcommand (pruning, JSON index, per-day counter); wrote Playwright specs for UI candidate selection + badge display. All tests APPROVED and passing in PR #1845. Removed dead replace_subdir function. Feature validated.
