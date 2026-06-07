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
