# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

### Classic JSX Runtime Requirement (2026-06-05)

EduMIPS64 uses the **classic JSX runtime** (@babel/preset-react WITHOUT runtime:automatic). Every `src/webapp` React component MUST keep `import React` in scope, even if no JSX appears to reference React directly. Removing it compiles and builds successfully but crashes at runtime with "React is not defined", causing the entire Playwright test suite to fail. ESLint rules suggesting removal of unused imports must be reviewed manually before committing React component files. The JSX transformer needs React in scope regardless of visibility in the source code.

### Web UI Statistics now sums all structural-stall counters (2026-06-05)

The web UI `Statistics.js` component now sums all four structural-stall CPU counters (`dividerStalls`, `memoryStalls`, `exStalls`, `funcUnitStalls`) for display in the "Structural Stall" row (issue #1818 fix, PR #1819). When adding new structural-stall counters to `CPU.java`, ensure they are exported in `ResultFactory.java` and included in the sum in `Statistics.js`.

## Learnings — 2026-06-07: Web build identity + NIGHTLY badge

**What the UI shows now:**
- Previously: `${BRANCH}-${COMMITHASH.substring(0,7)}` (e.g. `master-eec1768`) — constructed in `index.js` using webpack BRANCH/COMMITHASH globals; the webpack `VERSION` global (git-describe) was unused in the display chain.
- Now: `VERSION` from webpack DefinePlugin, which is `git describe --tags --match v* --always --dirty` with leading `v` stripped (e.g. `1.4.0-75-geec17684-dirty`). Matches desktop identity exactly.

**Badge mechanism:**
- Runtime detection only: `window.location.pathname.includes('/nightly/')`.
- No compile-time flag — same immutable artifact works across all channel paths.
- Implemented as a MUI `Chip` with `id="nightly-build-chip"` and `className="nightly-chip"` inside a `Tooltip`.
- CSS: purple pill (`.nightly-chip { background-color: #7b1fa2; color: #fff; }`).
- Does NOT appear for production (`web.edumips.org/`), versioned (`/v/N/`), or PR preview paths.

**Files changed:**
- `webpack.config.js`: `GitRevisionPlugin({ versionCommand: 'describe --tags --match v* --always --dirty' })` + `.replace(/^v/, '')` in DefinePlugin.
- `src/webapp/index.js`: `const version = VERSION;` (was `${BRANCH}-${COMMITHASH.substring(0,7)}`).
- `src/webapp/components/Header.js`: `isNightly` runtime detection + NIGHTLY Chip/Tooltip in JSX.
- `src/webapp/css/main.css`: `.nightly-chip` purple pill styling.

**Build:** `npm run build-dbg` compiled successfully (webpack 5, 6185 ms, no errors).
**Committed (not pushed):** `4149d54e` on `squad/web-promotion-system`.
