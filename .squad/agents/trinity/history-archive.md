# Trinity Agent — Session History Archive

**Archived:** 2026-06-13  
**Summary Period:** 2026-06-05 through 2026-06-09  

## Learnings Preserved

### Classic JSX Runtime Requirement (2026-06-05)
Every `src/webapp` React component MUST keep `import React` in scope, even if no JSX appears to reference React directly. Removing it compiles and builds successfully but crashes at runtime with "React is not defined", causing the entire Playwright test suite to fail. ESLint rules suggesting removal of unused imports must be reviewed manually before committing React component files.

### Nightly Detection & Unused DefinePlugin Globals (2026-06-07)
- `window.location.pathname.includes('/nightly/')` misses `/nightly` without trailing slash and false-matches paths like `/nightlybuild`. Use: `path === '/nightly' || path.startsWith('/nightly/')` with SSR-safe ternary guard.
- `DefinePlugin` injected globals that were never consumed bloat the bundle. Only inject what is actually used; verify with grep before adding new globals.

### Web UI Statistics Structural Stall Counters (2026-06-05)
`Statistics.js` now sums all four structural-stall CPU counters (`dividerStalls`, `memoryStalls`, `exStalls`, `funcUnitStalls`). When adding new structural-stall counters to `CPU.java`, ensure they are exported in `ResultFactory.java` and included in the sum in `Statistics.js`.

### Version History Navigator Manifest Contract (2026-06-08)
- `/manifest.json` contract: `current`, `prev`, `sha`, `build`, `targetRelease`, `promotedAt`, `promotedBy`, `history[]` (newest-first, re-sorted descending by `n`)
- Gating rule: render `PreviousVersions` only when manifest is valid AND `getBuildInfo().kind !== 'pr'`
- Absolute hrefs: use `'/v/' + n + '/'` so links work when app is served from `/v/<k>/`
- New files: `src/webapp/versionHistory.js`, `src/test/webapp/version-history.spec.js`; updated `HelpDialog.js`

### Contextual Run Controls State Model (2026-06-09)
- `deriveLogicalState(status, executing, inputRequest)` maps three observable values to: `EMPTY`, `READY`, `EXECUTING`, `ENDED`, `WAITING_FOR_INPUT`
- State derivation is a plain function (no hooks), defined at top of `Header.js` or `simulatorState.js` for reuse
- Execution controls use `{showX && <Button/>}` pattern or always-rendered with `disabled` prop
- Stop button special case: shown in both READY and EXECUTING; rendered disabled in EXECUTING with tooltip "Pause before stopping"
- Stop wrapped in `<span>` for MUI Tooltip compat when disabled

### Floating Draggable Debug Toolbar (2026-06-09)
- MUI `Paper`, `position: fixed`, `elevation: 8`, pill shape (`borderRadius: 24px`), `zIndex: 1200`
- Drag handle uses `setPointerCapture` on handle element for smooth drag outside bounds
- Position stored in `useState`, constrained to stay on-screen
- Returns `null` in EMPTY/ENDED/WAITING_FOR_INPUT states; mounted unconditionally so position persists

### Program Menu Gating Contract (2026-06-09)
- **DISABLED** when program loaded: READY, EXECUTING, WAITING_FOR_INPUT
- **ENABLED** in EMPTY and ENDED (ENDED must be enabled — it's the only way to start a fresh program)

### Candidate Build UI & Badge Migration (2026-06-13)
- Candidate detection regex: matches paths like `/<date>/<n>-<sha>/`
- `buildInfo.js` returns `kind: 'candidate'` with `candidateDate`, `candidateN`, `candidateSha`
- Migrated from nightly lane to candidate badge (purple→blue visual treatment)
- DOM hooks: `id="candidate-build-chip"`, `class="candidate-chip"`, `aria-label="Candidate build"`

### Data-Theme Bridge Pattern (2026-06-13)
When React app uses user-selectable MUI theme AND has static CSS rules for raw HTML elements:
1. Publish resolved `paletteMode` to DOM: `React.useLayoutEffect(() => { document.documentElement.setAttribute('data-theme', paletteMode); }, [paletteMode])`
2. Gate static CSS rules on `html[data-theme='dark'] selector { ... }` instead of `@media prefers-color-scheme`
3. Inline styles in component as defense-in-depth

## Session Summaries

### 2026-06-05: Classic JSX Runtime
Identified that React must stay in scope for JSX compilation even when seemingly unused. Discovered `DefinePlugin` globals were unused. Fixed `Statistics.js` structural-stall summation logic.

### 2026-06-07-06-08: Version History Navigator
Implemented manifest contract, version selection, `PreviousVersions` component, and absolute href linking. Tests via Playwright `page.route`. Gating ensures feature shows only in production and mocked dev scenarios.

### 2026-06-09: Contextual Controls & Floating Toolbar
Three iterations: (1) Inline execution controls with logical-state derivation, (2) Floating VSCode-style toolbar with drag persistence, (3) Always-visible buttons (disabled instead of hidden). Implemented program menu (Alternative A: dropdown). Coordinated with Smith (test rewrites), Link (trilingual docs).

### 2026-06-13: Web Stdout Theme Fix
Investigated user report of missing stdout. Root cause: CSS desync (OS media query vs. user theme). Fixed via data-theme bridge: published MUI `paletteMode` to DOM, re-gated static rules. Added regression test (two directions). PR #1846 merged and promoted.

## No Further Sessions
All Trinity work completed and merged.
