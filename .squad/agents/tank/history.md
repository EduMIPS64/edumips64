# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

## Learnings

### 2026-06-05 — Structural Stall total fix (#1818)

**ResultFactory → Statistics.js stat mapping (structural stalls):**
- `ResultFactory.java` (~line 298-301) exports per-counter structural stall fields: `dividerStalls`, `memoryStalls`, `exStalls`, `funcUnitStalls` (the latter two were added in this fix).
- `Statistics.js` (~line 34 / 73) destructures these props and sums all four for the `stat-structural-stalls` display row.

**Key file paths:**
- `src/main/java/org/edumips64/client/ResultFactory.java` — Java → JSON bridge; add new stat fields here.
- `src/webapp/components/Statistics.js` — React component; destructure and render stats.
- `src/test/webapp/pipeline-stalls.spec.js` — Playwright tests for pipeline hazards; `readStat(page, id)` helper reads numeric stat values from the UI.

**Pattern:** When a new CPU getter needs to appear in the web UI, add a `.put("key", cpu.getX())` in `ResultFactory.java` and destructure + render it in `Statistics.js`.

### 2026-06-07 — git-describe build identity (PR-A slice 1, branch `squad/web-promotion-system`)

**Goal:** Replace static `Signature-Version` + `Build-Qualifier` hack with a git-describe-based build identity.

**Observed git describe string:** `v1.4.0-74-ge1b45a15-dirty` → stripped to `1.4.0-74-ge1b45a15-dirty`

**Files changed:**
- `build.gradle.kts`: Removed `buildQualifier` provider. Added `gitDescribe` Provider using `providers.exec { commandLine("git", "describe", "--tags", "--match", "v*", "--always", "--dirty") }.standardOutput.asText.map { it.trim().removePrefix("v") }.orElse(version as String)`. Updated `sharedManifest` to set `Signature-Version = gitDescribe.get()` and removed `Build-Qualifier` attribute.
- `src/main/java/org/edumips64/utils/MetaInfo.java`: Removed the Build-Qualifier augmentation block (and its latent NPE on absent attribute).

**Key pattern:** `orElse(version as String)` provides a safe fallback for non-git builds. The `version` cast is needed because Kotlin sees it as `Any` in the Gradle KTS context.

**Build result:** `./gradlew compileJava` and `./gradlew compileTestJava` both succeeded. No test class specifically for MetaInfo versioning found (BannerTest and ArgsTest reference MetaInfo/Version but don't test the git-describe path directly).

**Branch:** `squad/web-promotion-system` — commit `eec17684`. Not pushed.

**Note for CI:** CI workflows must use `fetch-depth: 0` in `actions/checkout` or `git describe` will fail on shallow clones (falls back to `version` safely, but loses describe fidelity).

---

## Learnings — 2026-06-07: Workflows slice (PR-A)

### What was built
- **Phase 0:** `release.yml` `deploy-prod` job disabled with `if: false` (body preserved for emergency re-enable). Production now gated exclusively via `promote-web.yml`.
- **`fetch-depth: 0`:** Added to `actions/checkout` in `build-web.yml`, `build-desktop.yml`, and `ci.yml` test-web-coverage job so `git describe --tags` sees the full tag history.
- **`.github/scripts/deploy-web-pages.sh`:** `set -euo pipefail` bash script implementing three subcommands:
  - `promote <artifact_dir> <build> <sha> <targetRelease> <actor>` — reads `manifest.json` current counter N, writes root prod files, archives to `v/<N+1>/`, copies current root to `prev/`, prunes `/v/` to 50 dirs max, writes updated `manifest.json`, ensures `CNAME`/`.nojekyll`.
  - `rollback <actor>` — swaps `prev/` to root, updates `manifest.json` with `rolledBackFrom` + `note: rollback`.
  - `nightly <artifact_dir>` — replaces only `nightly/`, never touches root/prev/v/manifest.json.
  - **First-run safety:** if `manifest.json` absent, seeds `prev/` from current root before the first promote so rollback is immediately available.
  - **Reserved names guard:** `v`, `prev`, `nightly`, `manifest.json`, `CNAME`, `.nojekyll`, `.git` are never deleted from root when clearing old prod files (uses `extglob` pattern).
- **`promote-web.yml`:** `workflow_dispatch`, actor guard `if: github.actor == 'lupino3'`, validates source run (repo/workflow/conclusion/branch/artifact), computes `git describe` build identity + `targetRelease` from `gradle.properties`, downloads `web` artifact via `gh run download`, clones Pages repo via `PAT_WEBUI`, runs script, commits `Promote web to v<N> @ <sha>`.
- **`rollback-web.yml`:** `workflow_dispatch`, actor guard, clones Pages repo, runs `rollback`, commits `Rollback web to last known good`.
- **`nightly-web.yml`:** `workflow_run` on CI completed, guards `conclusion==success && head_branch==master && event==push`, downloads `web` artifact, clones Pages repo, runs `nightly`, commits `Nightly web @ <sha>`.
- **Shared concurrency:** all three workflows use `concurrency: { group: web-pages-deploy, cancel-in-progress: false }` so no two can write the Pages repo simultaneously.

### Validation results
- `bash -n deploy-web-pages.sh` → passed.
- `shellcheck` not installed on host; apt-get blocked (no root). `bash -n` sufficient.
- All 7 YAML files validated with `python3 + pyyaml` → all OK.

### Key design notes
- `promote` never rebuilds — it promotes the exact immutable `web` artifact from the validated CI run (no rebuild = true immutability).
- First promotion creates the Pages repo directory layout automatically (no manual setup needed in Pages repo).
- `prev/` on first run is seeded from whatever is currently at the root of Pages repo → rollback is safe even on the very first promotion.
- `v/` pruning keeps the highest-numbered 50 dirs; older builds are deleted on each promotion.
- `CNAME` (`web.edumips.org`) and `.nojekyll` are always re-ensured present, so a bug in the script can't accidentally break GitHub Pages hosting.

### Branch / commit
`squad/web-promotion-system` — commit `4b5f67d3`. **Not pushed.**

### Files added/changed
- `.github/scripts/deploy-web-pages.sh` (new, chmod +x)
- `.github/workflows/promote-web.yml` (new)
- `.github/workflows/rollback-web.yml` (new)
- `.github/workflows/nightly-web.yml` (new)
- `.github/workflows/release.yml` (deploy-prod disabled)
- `.github/workflows/build-web.yml` (fetch-depth: 0)
- `.github/workflows/build-desktop.yml` (fetch-depth: 0)
- `.github/workflows/ci.yml` (fetch-depth: 0 on test-web-coverage checkout)
