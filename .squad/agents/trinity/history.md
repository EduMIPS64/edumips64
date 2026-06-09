# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

### Classic JSX Runtime Requirement (2026-06-05)

EduMIPS64 uses the **classic JSX runtime** (@babel/preset-react WITHOUT runtime:automatic). Every `src/webapp` React component MUST keep `import React` in scope, even if no JSX appears to reference React directly. Removing it compiles and builds successfully but crashes at runtime with "React is not defined", causing the entire Playwright test suite to fail. ESLint rules suggesting removal of unused imports must be reviewed manually before committing React component files. The JSX transformer needs React in scope regardless of visibility in the source code.

### Nightly detection & unused DefinePlugin globals (2026-06-07)

`window.location.pathname.includes('/nightly/')` misses `/nightly` without a trailing slash and false-matches paths like `/nightlybuild`. The correct SSR-safe pattern is: compute `path` via a ternary guard (`typeof window !== 'undefined' ? window.location.pathname : ''`), then check `path === '/nightly' || path.startsWith('/nightly/')`. Always access `window.location.pathname` only inside the defined-window branch.

`DefinePlugin` injected globals (`COMMITHASH`, `BRANCH`) that were never consumed in `src/webapp/`. Unused injected globals bloat the bundle and create a false impression that those values are available to the app. Only inject what is actually used; verify with a codebase grep before adding new globals.

### Web UI Statistics now sums all structural-stall counters (2026-06-05)

The web UI `Statistics.js` component now sums all four structural-stall CPU counters (`dividerStalls`, `memoryStalls`, `exStalls`, `funcUnitStalls`) for display in the "Structural Stall" row (issue #1818 fix, PR #1819). When adding new structural-stall counters to `CPU.java`, ensure they are exported in `ResultFactory.java` and included in the sum in `Statistics.js`.

### Version History Navigator — manifest contract, gating, and new files (2026-06-08)

**Manifest contract** (`/manifest.json` at the root, fetched with `cache: 'no-cache'`):
```json
{
  "current": <int>, "prev": <int>, "sha": <str>, "build": <str>,
  "targetRelease": <str>, "promotedAt": <iso>, "promotedBy": <str>,
  "history": [ { "n": <int>, "build": <str>, "sha": <str>, "targetRelease": <str>,
                  "promotedAt": <iso>, "promotedBy": <str> }, ... ]
}
```
`history` is newest-first in the manifest but `buildVersionList` re-sorts descending by `n`. Each `n` has a live snapshot at `/v/<n>/`.

**Gating rule:** render `PreviousVersions` only when (a) `fetchManifest()` returns a valid, non-null manifest AND (b) `getBuildInfo().kind !== 'pr'`. Production always shows it; PR previews never do; local dev shows it only when the manifest is mocked (useful for Playwright tests).

**Absolute hrefs:** `href` for each version must be `'/v/' + n + '/'` (absolute, not relative) so the link works correctly when the app is itself served from `/v/<k>/`.

**No JS unit runner:** the project has no jest/vitest; unit-level coverage of `getViewedVersion`, `isValidManifest`, and `buildVersionList` relies on Playwright + `page.evaluate` or is deferred to CI.

**New files:**
- `src/webapp/versionHistory.js` — pure helpers (`getViewedVersion`, `buildVersionList`, `isValidManifest`) + async `fetchManifest`.
- `src/test/webapp/version-history.spec.js` — Playwright specs: Test A mocks manifest via `page.route`, Test B asserts graceful absence with no mock.

**Changed files:**
- `src/webapp/components/HelpDialog.js` — added `PreviousVersions` component rendered below `<BuildInfoLine />` in the About TabPanel; added `Chip` import and `versionHistory` imports.

### Contextual Run Controls — state model and component wiring (2026-06-09)

**State derivation:** `deriveLogicalState(status, executing, inputRequest)` is a plain function defined at the top of `Header.js` (not in Simulator.js). It maps the three observable values to one of: `'EMPTY'`, `'READY'`, `'EXECUTING'`, `'ENDED'`, `'WAITING_FOR_INPUT'`.

**Simulator state location:** `status` (READY/RUNNING/STOPPED), `executing` (boolean), and `inputRequest` (object|null) all live in `Simulator.js` state. `status` was already passed to `<Header>`; `executing` and `inputRequest` were added as new props in this change.

**Contextual rendering wiring:** Execution controls (`showLoad`, `showStep`, `showMultiStep`, `showRun`, `showPause`, `showStop`) are computed as booleans from `logicalState` and used to conditionally mount buttons via JSX short-circuit (`{showX && <Button .../>}`). All execution controls are wrapped in `<Box sx={{ display:'flex', gap:1, minWidth:320 }}>` to prevent layout jank.

**Stop special case:** Stop is shown in both READY and EXECUTING; rendered disabled (`stopDisabled = logicalState === 'EXECUTING'`) with tooltip `'Pause before stopping'` when disabled. The MUI Tooltip requires a non-disabled DOM child to show, so the disabled Button is wrapped in a `<span>`.

**Editor controls:** Clear, Restore, and Open Code are disabled when `logicalState` is `'EXECUTING'` or `'WAITING_FOR_INPUT'` (replacing the old `status === 'RUNNING'` check, which incorrectly disabled them in READY/paused state). Save Code is now always enabled (design matrix: ✅ in all states).

**Props removed from Simulator.js → Header.js:** `runEnabled`, `stepEnabled`, `pauseEnabled`, `stopEnabled` (all derivable from logical state now). `loadEnabled` kept because it reflects parsing-error validity.

**Build verified:** `npm run build-dbg` compiled successfully. ESLint (post-prettier) shows only the 3 pre-existing unused-var errors (`fileContent`, `setFileContent`, `handleFileLoad`) that existed before this change.

## 2026-06-09 — Contextual Run Controls Implementation Complete (PR #1835)

Successfully implemented contextual run controls in `Header.js` and `Simulator.js`. Design locked with 8 constraints:
1. Contextual hiding (conditional render, not display:none/visibility:hidden)
2. Stop disabled (not hidden) during EXECUTING
3. No PAUSED state (collapsed to READY)
4. No STOPPING React state
5. Conditional render for Playwright compat
6. Fixed-min-width container prevents toolbar jank
7. Keyboard shortcuts deferred
8. WAITING_FOR_INPUT hides all execution controls

**Implementation verified:**
- `deriveLogicalState()` correctly maps 3 observable props to 5 logical states
- All execution controls use `{showX && <Button>}` pattern (not in DOM when hidden)
- Stop button wrapped in `<span>` for MUI Tooltip support when disabled
- Editor controls properly re-enabled when paused (READY state, not EXECUTING)
- Props cleanup: removed 4 enable-state bools, added `executing` + `inputRequest`
- Full test suite: 68/70 PASS (2 pre-existing GPU crashes); contextual-controls spec: 8/8 PASS
- No implementation bugs found; design fully realized

## 2026-06-09 — Floating Draggable Debug Toolbar (PR #1835, second iteration)

Andrea requested replacement of the inline execution controls with a VSCode-style floating debug toolbar.

### Floating Toolbar Component (RunControlsToolbar.js)

**New file:** `src/webapp/components/RunControlsToolbar.js`
- MUI `Paper` with `position: fixed`, `elevation: 8`, rounded pill shape (`borderRadius: '24px'`), `zIndex: 1200`
- Icon-only `IconButton` buttons (no text labels) with MUI `Tooltip`s for all action names
- Drag handle (`DragIndicatorIcon`) at the left; uses `setPointerCapture` on the handle element so drag remains smooth even when pointer moves outside the handle
- Position stored in `useState` (lazy initial value: center of viewport, y=80); constrained to stay on-screen via viewport bounds check in `handlePointerMove`
- Refs renamed to `isDraggingRef` / `dragOffsetRef` to satisfy `@eslint-react/naming-convention-ref-name`
- Returns `null` (not rendered) in EMPTY / ENDED / WAITING_FOR_INPUT states; same visibility logic as the previous inline controls
- Mounted unconditionally from `Simulator.js` so position state persists across logical-state changes

### Shared State Helper (simulatorState.js)

**New file:** `src/webapp/simulatorState.js`
Exports `deriveLogicalState(status, executing, inputRequest)` as a named export, consumed by both `Header.js` and `RunControlsToolbar.js`. Previously this function was inlined in `Header.js`.

### Header.js Changes

- Removed execution controls `Box` (contained Step / Multi Step / Run / Pause / Stop)
- `Load` button is now **always visible** (no `showLoad` condition) — it is always present in the header AppBar regardless of logical state
- Removed now-unused imports: `PlayArrowIcon`, `FastForwardIcon`, `PlayCircleIcon`, `PauseCircleIcon`, `StopCircleIcon`
- Removed now-unused variables: `multiStepCount`, `showLoad`, `showStep`, `showMultiStep`, `showRun`, `showPause`, `showStop`, `stopDisabled`
- Props removed from Header interface: `onRunClick`, `onStepClick`, `onPauseClick`, `onStopClick`, `multiStepCount`
- `deriveLogicalState` imported from `../simulatorState` (no longer defined inline)

### Simulator.js Mounting

`RunControlsToolbar` is mounted immediately after `<Header>` in the ThemeProvider tree, receiving: `onStepClick`, `onRunClick`, `onPauseClick`, `onStopClick`, `status`, `executing`, `inputRequest`, `multiStepCount`.
