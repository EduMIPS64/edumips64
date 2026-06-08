# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

## Learnings

### 2026-06-07 — Nightly web deploy changed to cron schedule (#1826)

**Trigger change:** `nightly-web.yml` was rewritten from `on: workflow_run` (fired on every green master CI push) to `on: schedule` (cron `0 1 * * *`, 01:00 UTC daily) plus `workflow_dispatch` for manual testing.

**Artifact discovery pattern:** Because there is no longer a triggering run context, the job now uses `gh run list --workflow ci.yml --branch master --status success --limit 1 --json databaseId,headSha --jq '.[0]'` to find the latest green master build. `databaseId` and `headSha` are extracted via `python3` JSON parsing and exposed as `steps.find_run.outputs.*` for subsequent steps. This mirrors the `gh api` + python3 pattern used in `promote-web.yml`.

**Job-level `if:` removed:** Schedule and `workflow_dispatch` triggers always run on the default branch; the old branch/event guard was only meaningful for `workflow_run`.

**Doc update:** Both nightly references in `docs/developer-guide.md` (workflow list ~line 69 and nightly-channel section ~line 479) updated to state "daily schedule at 01:00 UTC, deploying the latest green master build".

### 2026-06-05 — Structural Stall total fix (#1818)

**ResultFactory → Statistics.js stat mapping (structural stalls):**
- `ResultFactory.java` (~line 298-301) exports per-counter structural stall fields: `dividerStalls`, `memoryStalls`, `exStalls`, `funcUnitStalls` (the latter two were added in this fix).
- `Statistics.js` (~line 34 / 73) destructures these props and sums all four for the `stat-structural-stalls` display row.

**Key file paths:**
- `src/main/java/org/edumips64/client/ResultFactory.java` — Java → JSON bridge; add new stat fields here.
- `src/webapp/components/Statistics.js` — React component; destructure and render stats.
- `src/test/webapp/pipeline-stalls.spec.js` — Playwright tests for pipeline hazards; `readStat(page, id)` helper reads numeric stat values from the UI.

**Pattern:** When a new CPU getter needs to appear in the web UI, add a `.put("key", cpu.getX())` in `ResultFactory.java` and destructure + render it in `Statistics.js`.

## Learnings

### 2026-06-07 — PR #1826 review fixes: permissions, unused input, rollback SWAP

**GitHub Actions `permissions` and the Actions API:** When a job has an explicit `permissions:` block, GitHub sets every unmentioned scope to `none`. Both `promote-web.yml` and `nightly-web.yml` call `gh api` / `gh run download` / `gh run list` against the Actions artifacts API, so they need `actions: read` alongside `contents: read`. Without it, these calls 403 silently. Pattern from `pr-reports.yml` (the `metadata` and `deploy-staging` jobs) — look there first whenever a workflow downloads artifacts from a separate run.

**Remove unused `workflow_dispatch` inputs:** The `sha` input in `promote-web.yml` was declared but never wired — always grep `inputs.<name>` before removal to confirm it's truly dead. Clean removal is preferable to leaving confusing dead params.

**Reserved-name guard placement:** The guard scanning artifact top-level entries for reserved names belongs _before_ any destructive operation in `cmd_promote`, immediately after the `artifact_dir` existence check. Use the existing `RESERVED_NAMES` array and `die` helper; keep `dotglob`+`nullglob` consistent with the rest of the script.

**Reversible rollback via SWAP:** The old rollback was one-way (set `manifest.prev = 0`, losing the current root). The correct design is a true in-place SWAP: (1) collect root prod files into a `.rollback-swap` staging dir _before_ touching prev/, (2) move prev/* to root, (3) move staging dir contents to prev/, (4) in manifest.json swap `current` ↔ `prev`. Key: collect root_files into an array _before_ creating the staging dir so the dir itself is never included in the move set. The `.rollback-swap` name is safe because it's not a valid web-artifact filename, but must be created after the array is collected.

### 2026-06-08 — deploy-web-pages.sh → .py port

**Python port of the 342-line bash deploy script:**
- Standard-library only (os, sys, json, shutil, pathlib, datetime, argparse) — no subprocess; Bandit/Codacy clean.
- `dotglob` in bash → `Path(".").iterdir()` in Python (both include hidden files); filter with `RESERVED_NAMES` list.
- `cp -a "$src/." "$dest/"` (contents, not the dir) → custom `copy_contents_into()` using `shutil.copytree(dirs_exist_ok=True)` + `shutil.copy2` + symlink handling; preserves metadata like `cp -a`.
- `shutil.move` for rollback staging: collect root file list BEFORE creating `.rollback-swap` dir so the staging dir never appears in the move set.
- Prune logic: collect numeric dir names, sort descending, delete anything beyond index `MAX_VERSIONS-1`.
- All 3 workflows updated to `cp deploy-web-pages.py`, `python3 deploy-web-pages.py`, `rm -f deploy-web-pages.py`.
- `_ArgumentParser` subclass overrides `error()` → `sys.exit(1)` (argparse defaults to 2) to match bash `die` exit-code contract.
- Tested: first-run promote, second promote, rollback, double-rollback (restores), nightly, error guards, prune-versions (50-cap verified). Cross-validated byte-identical against the old bash through a full operation sequence.


### 2026-06-08 — JAR filename carries git-describe build version (#1826)

**Key file paths:**
- `build.gradle.kts` ~line 269: `tasks.jar` — add `archiveVersion.set(gitDescribe)` here.
- `build.gradle.kts` ~line 287: `tasks.register<Jar>("noHelpJar")` — same pattern.
- `build.gradle.kts` ~line 356: `tasks.register<Exec>("msi")` — find JAR by glob, not by reconstructed `${version}` filename.
- `.github/workflows/build-desktop.yml` ~line 71: JAR upload — discover actual filename with shell glob before uploading.
- `.github/workflows/release.yml` ~line 276: JAR staging — glob and rename to canonical release name.
- `docs/developer-guide.md` ~line 96/101/111: Updated to use `<build-version>` terminology.

**git-describe-in-filename pattern:**
- `val gitDescribe: Provider<String>` is already defined in build.gradle.kts (~line 254) using `providers.exec { commandLine("git", "describe", ...) }`.
- Wire it lazily: `archiveVersion.set(gitDescribe)`. Do NOT call `.get()` at configuration time.
- Produced filename: `edumips64-<git-describe>.jar` (e.g. `edumips64-1.4.1-5-gabc1234.jar` for dev, `edumips64-1.4.1.jar` at a clean tag).

**Release-at-tag collapses-to-tag insight:**
- The release workflow VALIDATES that the tag does NOT exist before building, then creates the tag at the very end. So during `./gradlew jar` in `build-desktop`, the new tag is absent → git-describe returns `<prev-tag>-<N>-g<sha>`, NOT the new version string.
- Therefore the `create-release` staging step cannot `cp edumips64-${VERSION}.jar`; it must glob for the actual file and rename it. Pattern: `JAR_SRC=$(ls release-artifacts/JAR/edumips64-*.jar | grep -v '\-nohelp\.jar' | head -1); cp "$JAR_SRC" "staged/edumips64-${VERSION}.jar"`.

**Provider-laziness gotcha:**
- `providers.exec { ... }` is lazy by nature. `archiveVersion.set(gitDescribe)` wires the provider without forcing evaluation. Safe.
- Calls to `.get()` inside a `doFirst { }` block are fine (execution time). Calls to `.get()` at the top level of `build.gradle.kts` or inside `val x = ...` ARE configuration-time and will run git on every `./gradlew tasks`. The existing `sharedManifest` block already does `.get()` inside a Manifest Action lambda, which runs at configuration time — this is the existing pattern and is acceptable.

**MSI JAR-finding pattern:**
- The MSI task downloads a pre-built JAR (from CI artifact), then needs to find it. Since git-describe on the MSI Windows runner may differ from the Linux build runner (different fetch-depth/tags), never recompute the archive name from gitDescribe in the MSI task. Instead, glob the build directory: `layout.buildDirectory.get().asFile.listFiles()?.filter { it.name.startsWith("edumips64-") && it.name.endsWith(".jar") && !it.name.contains("nohelp") }?.firstOrNull()`.
