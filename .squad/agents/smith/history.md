# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

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
Deferred: badge PRESENCE requires serving under `/nightly/` which the local harness doesn't do.

### Bug found and fixed
**BLOCKING BUG in `build-desktop.yml`**: The `fetch-depth: 0` patch that was applied to add deep clone support corrupted the YAML — the `- name: Set up JDK 17` step was deleted and `uses: actions/setup-java@v5` was merged into the same YAML mapping as `actions/checkout@v6` (duplicate `uses` key in one step). This would have made the desktop build fail at workflow parse time. Fixed by restoring the step as a proper separate sequence item.

### Reviewer verdict
**APPROVE WITH FIX** — the build-desktop.yml YAML corruption (fixed in this commit) was the only blocking issue. All other changes are correct: git-describe versioning flows cleanly from build.gradle.kts through MetaInfo.java; webpack VERSION is properly derived; NIGHTLY badge detection is path-based (correct approach); workflow gating/promote/rollback/nightly logic is sound; `promote-web.yml` uses `actions/checkout@v4` (vs `@v6` elsewhere) — minor inconsistency, not blocking.

