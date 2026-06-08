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
