# Tank — Core/Backend Developer (Summarized)

**Created:** 2026-06-05T07:18:06+02:00  
**Last activity:** 2026-06-09  
**History size:** Summarized 2026-06-09 (was 17628 bytes, reduced to fit <15360 threshold)

## Six Sessions: Web Promotion System + Infrastructure

### 2026-06-05: Structural Stalls Stats Integration
Fixed web UI to sum all four structural-stall CPU counters. Pattern: `ResultFactory.java` exports stats via `.put()`, `Statistics.js` destructures + renders.

### 2026-06-07–06-08: Web Promotion System (PR #1826)

**Deploy script:** Ported 342-line bash `deploy-web-pages.sh` → Python (`deploy-web-pages.py`). Stdlib-only, Bandit-clean. Key features:
- Reversible SWAP rollback (collect root → staging, move prev to root, move staging to prev)
- Prune snapshots to 50 (monotonic numbering prevents rollback collisions)
- manifest.json history array (hard contract: `n`, `build`, `sha`, `targetRelease`, `promotedAt`, `promotedBy`)
- Tested: first-run, second-run, rollback, double-rollback (reversible), nightly, error guards, prune-to-50

**JAR versioning:** Filenames now carry `git-describe` (e.g., `edumips64-1.4.1-5-gabc1234.jar`). Key insight: build happens before tag creation in release flow → JAR carries prev-tag+distance; must glob + rename, not reconstruct.

**Workflow updates:**
- `nightly-web.yml`: Changed from `workflow_run` to `schedule` cron (01:00 UTC daily). Uses `gh run list` to find latest green master build.
- `promote-web.yml`: Unified with optional `run_id` (fresh build or external run validation). Separate `comment-preview` job (minimal privileges).
- `ci.yml`: Added `workflow_dispatch` for on-demand master builds.

**GitHub Actions patterns:**
- Permissions block must explicitly include `actions: read` for `gh api`/`gh run download` calls (defaults to `none` if not listed)
- `workflow_run` workflows only execute from DEFAULT BRANCH; feature-branch versions never run. Validate with `actionlint` before merge.
- Gating: both `promote` and `comment-preview` respect environment approval gates (belt-and-suspenders with `actor==lupino3 && ref==master`)

### 2026-06-09: Deploy Script Tests + Verification
- Added pytest job to `ci.yml` (`test-deploy-script`): 5 tests, <1s, always-green contract
- Verified `actions/checkout@v6` bump already in PR #1828 (no additional work needed)

## Durable Patterns (For Future Tasks)

1. **Stats integration:** Java `ResultFactory.java` → JSON → React `Statistics.js`
2. **Reversible state:** True SWAP (not one-way), collect before destructing
3. **Provider laziness:** `set(gitDescribe)` safe; `.get()` in `doFirst {}` blocks only
4. **Monotonic numbering:** Prevents collision on rollback (use max ever used, not current+1)
5. **Manifest contract:** History array is hard contract with frontend; do not rename keys
6. **Least privilege:** Separate jobs for different permission scopes (deploy vs. comment)
7. **Pre-merge validation:** actionlint + node --check for workflow_run files before merge

## Archive

Detailed session learnings from 2026-06-05 through 2026-06-09 are available in git history of `.squad/agents/tank/history.md` (pre-2026-06-09 versions).
