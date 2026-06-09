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


### 2026-06-08 — PR preview sticky comment via separate minimal-privilege job (#1826)

**Sticky comment pattern:** To post a single updating comment on a PR from a `workflow_run` workflow:
1. Use a hidden HTML marker constant (`<!-- web-preview-link -->`) embedded in the comment body.
2. Paginate all issue comments with `github.paginate(github.rest.issues.listComments, { owner, repo, issue_number, per_page: 100 })`.
3. Find the first comment whose `body.includes(MARKER)` AND `user.type === 'Bot'` (identifies comments by github-actions[bot]).
4. If found → `updateComment({ comment_id: existing.id, body })`; else → `createComment({ issue_number, body })`.
5. Wrap in try/catch with `core.warning` on error — never hard-fail the pipeline over a comment.

**Least-privilege separate-job approach:** `pull-requests: write` must NOT be added to the deploy job (which already has `id-token: write` + Azure secrets). Instead, add a dedicated `comment-preview` job with `permissions: pull-requests: write` only. Dependency on `deploy-staging` (via `needs: [metadata, deploy-staging]`) ensures it only runs after a successful deploy and inherits the environment-based approval gate for untrusted actors.

**`workflow_run` / base-branch reason this cannot be tested pre-merge:** `workflow_run` workflows only fire from the workflow definition on the repository's **default branch** (master). A version of `pr-reports.yml` on a feature branch is never executed by GitHub Actions. Correctness must be verified by construction (actionlint + node --check) before merging; the first real end-to-end test only happens after the PR is merged.

**GHA expression injection guard:** Only pass `${{ needs.metadata.outputs.pr }}` (already validated numeric upstream) into the script via a `const pr = '...'` assignment. Read `head_sha` and `html_url` through `context.payload.workflow_run.*` inside JS — never via `${{ }}` — to avoid injection and quoting issues with arbitrary SHA strings.

## Learnings

### 2026-06-08 — Unified promote-web: optional run_id, build job, master-only gate

**Design:** `promote-web.yml` now supports two modes via an optional `run_id` input:
- **Fresh mode** (`run_id` empty): a new `build` job runs `.github/workflows/build-web.yml`
  (the shared reusable CI build) against `github.sha`, then the `promote` job downloads
  the artifact via `github.run_id` (the current workflow run's own ID).
- **External mode** (`run_id` given): `build` job is skipped; `promote` validates the
  external ci.yml run and promotes that artifact (rollback/re-promote/arbitrary-run preserved).

**Key structural decisions (from rubber-duck review):**
1. `concurrency: group: web-pages-deploy` moved from workflow-level to the `promote` job only —
   so a fresh-mode build is not serialised by the Pages-deploy lock.
2. `promote` job `if:` uses `always()` + mode-specific `needs.build.result` check:
   `'success'` in fresh mode, `'skipped'` in external mode. This gives better failure locality.
3. Both jobs gated on `github.actor == 'lupino3' && github.ref_name == 'master'`.

**Security rationale for master-only gate:** `workflow_dispatch` runs use the workflow file
at the branch the dispatch targets. Without `github.ref_name == 'master'`, a maintainer could
dispatch from a feature branch carrying a tampered workflow file, reaching the PAT_WEBUI secrets.
The `ref_name == 'master'` check ensures only the audited master copy can execute the PAT steps.
The `build` job has only `contents: read` and never sees PAT_WEBUI regardless.

**SOURCE_RUN_ID unifies artifact download:** A three-step "determine source" sequence:
(1) `validate` (external mode only) validates the external run, emits `head_sha` + `source_run_id`;
(2) `fresh_source` (fresh mode only) emits `head_sha = github.sha` + `source_run_id = github.run_id`;
(3) `source` (always) concatenates the two pairs — exactly one is non-empty — and re-emits them.
All downstream steps reference `steps.source.outputs.{head_sha,source_run_id}` uniformly.
The download step retries up to 3×(sleep 5) to tolerate same-run artifact API propagation lag.

### 2026-06-08 — ci.yml now supports workflow_dispatch for on-demand master builds

**Added `workflow_dispatch:` trigger** to `ci.yml` so a maintainer can run the full validating CI on master on demand without waiting for the nightly cron.

**Key insight — why a dispatch run qualifies for web promotion:** `promote-web.yml` validates that the triggering `ci.yml` run (a) targeted the `master` branch, (b) completed with `success`, and (c) produced a `web` artifact. A `workflow_dispatch` run on master satisfies all three conditions unchanged — no edits to `promote-web.yml` were needed.

**Force-all-builds on dispatch:** The `detect-changes` outputs used `github.event_name == 'schedule' && 'true'` to force snap/electron builds. Extended to `(github.event_name == 'schedule' || github.event_name == 'workflow_dispatch') && 'true'` so manual runs also build every artifact, making the dispatch button a true "build current master" action.

**PR-only steps remain untouched and correctly skip on dispatch:** `save-pr-metadata` is gated by `if: github.event_name == 'pull_request'`; the `paths-filter` checkout step is also guarded; `ref:` expressions use `github.event.pull_request.head.sha || github.sha` (falls back to master HEAD); `build-and-test-snap`'s bot-login guards evaluate true on dispatch (null login is not 'dependabot[bot]'). No changes required for any of these.

### 2026-06-08 — manifest.json `history` array, monotonic numbering, prune lockstep

**manifest history schema** — `manifest.json` now carries a `"history"` key (newest-first array). Each entry has exactly: `n` (int), `build`, `sha`, `targetRelease`, `promotedAt`, `promotedBy` (all strings). This is the hard contract with the frontend; do not rename or add keys without a coordinated frontend change.

**Backfill logic** — when promoting against an old history-less manifest with `current >= 1`, a single entry is synthesized from the manifest's top-level fields. When the manifest is absent (first-ever promote) or `current == 0`, history starts empty.

**Monotonic version numbering** — `new_n = max(current, prev, max(n in history), max(existing v/ numeric subdirs)) + 1`. This fixes the rollback collision bug where the old `new_n = current + 1` would reuse an existing `/v/<n>/` after a rollback. A defensive assertion (`die()`) fires if `v/<new_n>/` somehow already exists.

**Prune lockstep** — `prune_versions()` is called BEFORE writing `manifest.json`. After pruning, `history` is filtered to only entries whose `v/<n>/` dir still exists (`filter_history_to_existing()`). Manifest is always written last.

**Rollback** — `cmd_rollback()` swaps current ↔ prev and does not modify `history`. No history entry is added on rollback (rollback is not a promotion).

**pytest location** — `.github/scripts/test_deploy_web_pages.py` (5 tests). Run: `cd .github/scripts && python3 -m pytest test_deploy_web_pages.py -q`. No existing Python/pytest CI job found in `.github/workflows/`; recommend adding a `scripts-lint` step to `ci.yml` as a follow-up.
### 2026-06-09 — pytest CI job for deploy-web-pages.py (#1831)

**New `test-deploy-script` job in `ci.yml`:** Added a lightweight pytest job (job id `test-deploy-script`, name "Deploy script tests (pytest)") that runs the 5-test suite at `.github/scripts/test_deploy_web_pages.py` on every PR and nightly schedule. No paths-filter gate — the suite finishes in <1 s and must always be green.

**`working-directory` is mandatory:** `test_deploy_web_pages.py` loads the hyphenated module `deploy-web-pages.py` via `importlib.util.spec_from_file_location` using `Path(__file__).parent`. The test file's `__file__` must resolve inside `.github/scripts/`, so the step sets `working-directory: .github/scripts`. Without it, `importlib` fails to find the script.

**Python setup:** Uses `actions/setup-python@v5` pinned to `python-version: '3.12'` (matches the repo's documented Python 3.12+ requirement), then `pip install pytest`. No `requirements.txt` exists for the scripts directory — only stdlib + pytest are needed.
## Learnings

### 2026-06-09 — checkout@v6 bump in deploy workflows already covered by #1828

**Task:** Bump `actions/checkout@v4` → `@v6` in `promote-web.yml`, `nightly-web.yml`, `rollback-web.yml` (three deploy workflows that were noted as missed by Dependabot PR #1828).

**Finding:** When the task was actioned, all three files already contained `actions/checkout@v6`. Commit `617fc68c` (PR #1828, merged 2026-06-09 by Renovate + lupino3) **did** include these three files — despite the task brief stating they were missed. No additional PR was opened; the task was a no-op.

**Lesson:** Always grep/view the files on the latest master before creating a branch — the situation may have changed between when a task is written and when it is executed. A clean `git fetch origin` + grep for the target pattern is the definitive check before doing any work.
