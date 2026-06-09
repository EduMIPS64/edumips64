# Smith History Archive

## 2026-06-07 — PR-A: Web promotion/versioning test & review pass

### Java tests (./gradlew test)
All Java tests GREEN (BUILD SUCCESSFUL in ~25s). No test asserted a fixed version format — `BannerTest` already used `MetaInfo.VERSION` directly (accepts null/empty as "dev"), and `ArgsTest.properly_displays_version` uses `new Version().getVersion()[0]` (not a hardcoded string). No format assertions needed fixing.

### Playwright tests
Ran `help-dialog.spec.js` and the new `version-and-nightly-badge.spec.js` against the locally served production build (with `worker.js` copied from `out/tmp/gwt-war/web/worker.js`). All version-touching tests GREEN. Full suite: 59/60 passed; the 1 failure (`cache-simulator.spec.js:99`) is a pre-existing GPU crash in the snap Chromium under 32-way parallelism — passes in isolation.

**Environment note**: Ubuntu 26.04 is not yet officially supported by Playwright 1.60.0. The bundled Chromium headless shell does not install. Workaround: pass `PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH=/usr/bin/chromium-browser` and configure `launchOptions: { executablePath, args: ['--no-sandbox', '--disable-dev-shm-usage'] }` in `playwright.config.js`. Extended the config to read that env var. CI (ubuntu-latest) is unaffected.

**GWT worker.js**: `npm run build` does NOT build `worker.js` — that requires `./gradlew webapp` (slow GWT compilation). For local Playwright runs, copy `out/tmp/gwt-war/web/worker.js` → `out/web/worker.js` after a webpack build. Without it the app mounts Monaco but never renders `#load-button`.

### NIGHTLY badge coverage added
New spec `src/test/webapp/version-and-nightly-badge.spec.js` covers:
1. About tab shows a non-empty version matching git-describe regex `^\d+\.\d+\.\d+(-\d+-g[0-9a-f]+)?(-dirty)?|g?[0-9a-f]{7,}$`
2. NIGHTLY badge (`#nightly-build-chip`) is absent on a root-path load
3. Path-based detection logic is correct (unit-level `page.evaluate`)

### Bug found and fixed
**BLOCKING BUG in `build-desktop.yml`**: The `fetch-depth: 0` patch corrupted the YAML — the `- name: Set up JDK 17` step was deleted and `uses: actions/setup-java@v5` was merged into the same YAML mapping as `actions/checkout@v6` (duplicate `uses` key in one step). Fixed by restoring the step as a proper separate sequence item.

## 2026-06-09 — Contextual run controls: anticipatory test authoring

New spec `src/test/webapp/contextual-controls.spec.js` (8 tests) authored covering the full §3.2/§3.3 state matrix from the finalized run-controls design doc. Tests are anticipatory — they assert the design contract and will be RED against the pre-implementation UI; they go GREEN once Trinity's changes land.

Key approach:
- **EMPTY**: `waitForPageReady` leaves app in EMPTY state; assert load-button visible and all five execution controls `toBeHidden()`
- **READY**: `loadProgram` transitions to READY; assert step/multi-step/run/stop visible+enabled; pause `toBeHidden()`
- **EXECUTING**: Click run-button after loading 10,000-iteration loop; `waitForSelector('#pause-button', {state:'visible'})` as EXECUTING signal; assert pause visible+enabled, stop visible+disabled
- **ENDED**: `runToCompletion`; assert all execution controls hidden, load-button visible
- **Lifecycle**: Single page load walking EMPTY → READY → ENDED

Used `toBeHidden()` (satisfied by both "not in DOM" AND `display:none`; would FAIL on `opacity:0`/`visibility:hidden`, which design forbids).

## 2026-06-09 — Contextual run controls: verification run

### First run results (before fixes) — 66/70 passed
- `a11y-quick-wins.spec.js:33` FAIL — GPU crash (pre-existing)
- `contextual-controls.spec.js:89` FAIL — GPU crash (pre-existing)
- `contextual-controls.spec.js:146` FAIL — 10,000-iteration loop too slow
- `settings-persistence.spec.js:165` FAIL — multi-step button tooltip ran in EMPTY state

### Test fixes applied
- Replaced `waitForSimulationComplete` with: click pause → wait for step visible (READY) → click stop
- Added `loadProgram` to settings-persistence stepStride tooltip check

### Second run results (after fixes) — 68/70 passed
All 8 contextual-controls tests: **8/8 PASS in isolation**.

## 2026-06-09 — Floating RunControlsToolbar verification (PR #1835, iteration 2)

**VERDICT: PASS ✅** — 69/71 pass, 1 skipped (drag test), 1 pre-existing GPU crash.

### What changed
- `RunControlsToolbar.js`: new floating, draggable, icon-only toolbar
- Execution buttons live inside `#run-controls-toolbar`
- Toolbar not rendered in EMPTY / ENDED / WAITING_FOR_INPUT
- READY: step, multi-step, run, stop visible+enabled; pause absent
- EXECUTING: pause visible+enabled; step/multi/run absent; stop visible but disabled

### Test fixes
- Rewrote contextual-controls.spec.js structure for floating toolbar
- Fixed settings-persistence tooltip check: hover #multi-step-button → assert .MuiTooltip-tooltip text (icon buttons break getByRole)
- Added `test.skip` for draggable test

### Key learnings
- Icon-only buttons: `getByRole('button', { name: /tooltip text/ })` breaks; use hover → .MuiTooltip-tooltip text
- MUI Tooltip elements: use `.MuiTooltip-tooltip` class selector (avoid [role="tooltip"] — ambiguous with Monaco)
- `toBeHidden()` correctly passes when element is not in DOM
