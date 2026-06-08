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
