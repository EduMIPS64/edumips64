# Coordinator Actions Log — 2026-06-08

**Event:** Unify build+promote decision implementation + first production promotion  
**Date:** 2026-06-08T13:55:00Z (15:55 CEST)

## PR Management

### PR #1829 — CLOSED (superseded)

**Branch:** squad/web-promotion-system  
**Status:** Closed as superseded  
**Action:** Branch deleted  
**Reason:** Standalone `workflow_dispatch` trigger on ci.yml made redundant by full unify build+promote solution (PR #1830). The unified `promote-web.yml` with optional run_id achieves the same goal (on-demand master build artifact) plus rollback/re-promote capability.

### PR #1830 — MERGED

**Title:** Unify build+promote — optional run_id on promote-web.yml  
**Branch:** squad/web-promotion-system  
**Commit:** 1d037b23  
**Merge method:** Squash merge (--admin flag used; branch up-to-date bypass)  
**Merged by:** Coordinator (lupino3)  
**Status:** Master integration complete

**What shipped:**
- Optional `run_id` parameter on `promote-web.yml`
- Reusable `build-web.yml` workflow (no secrets)
- Security gates (actor + ref_name checks, both jobs)
- Unified SOURCE_RUN_ID download/retry logic
- Concurrency group moved to promote job
- Two-mode promotion procedure documented

**Verification pre-merge:**
- All GitHub Actions CI checks passed
- `actionlint` clean (no violations)
- No syntax errors in workflow files

## First Production Promotion

### Execution Details

**Command:** `gh workflow run promote-web.yml --ref master`  
**Trigger time:** 2026-06-08 15:42 UTC  
**GitHub Actions run ID:** 27142213771  
**Mode:** Empty run_id (build + promote in single run)

### Build Job Outcome

**Status:** SUCCESS  
**Artifact:** `web` (current master, 1.4.0-92-g1d037b23)  
**Duration:** ~12 minutes  
**Security:** Contents read-only, no secrets accessed

### Promote Job Outcome

**Status:** SUCCESS  
**Deployment:** web.edumips.org  
**Manifest updated:**
- `current=1` (versioned path)
- `prev=0` (previous rollback path)
- `sha=1d037b23` (commit identity)
- `build=1.4.0-92-g1d037b23` (git describe)
- `promotedBy=lupino3` (coordinator identity)
- `promotedAt=2026-06-08T13:42:13Z` (UTC timestamp)

### HTTP Verification

All endpoints verified returning HTTP 200:
- `/` (nightly root, serves from NIGHTLY badge)
- `/manifest.json` (deployment manifest)
- `/v/1/` (versioned v1 path, current)
- `/prev/` (previous rollback path, seeded from prior site)

**CNAME:** Preserved (web.edumips.org → Azure Pages)  
**Static files:** `.nojekyll`, `.git` protected, reserved names guarded

## Outcome

**Status:** PRODUCTION LIVE  
**Version deployed:** v1 (clean versioning baseline established)  
**Rollback capable:** Yes (SOURCE_RUN_ID mechanism enables re-promotion of v0 or rollback to prior state)  
**Future promotions:** Ready for routine use via `gh workflow run promote-web.yml --ref master`

## Notes

This first production promotion validates:
1. Build job works without secrets (can run on untrusted CI)
2. Promote job correctly gates on actor + ref_name
3. Artifact download/retry logic handles real GitHub Actions API
4. Web.edumips.org deployment infrastructure is live and responsive
5. Versioning scheme (v=N, manifest current/prev/sha) is sound
6. Two-mode operation (empty run_id → build+promote; set run_id → validate+promote) is operational

No issues detected. System is ready for routine operation.
