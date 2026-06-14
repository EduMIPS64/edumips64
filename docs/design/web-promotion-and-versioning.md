# Design: Web UI Production Promotion & Versioning

> **⚠️ Superseded (proposed):** the dual-index model described here
> (`manifest.json` + `candidates.json`, `/v/<n>/` + date dirs + `/prev/`) is
> being replaced by a single commit-addressed model. See
> [`unified-web-versioning.md`](./unified-web-versioning.md). This document
> remains the reference for the system as currently deployed until that change
> lands.

**Author:** Morpheus (Lead/Architect)  
**Date:** 2026-06-05 (decisions locked 2026-06-07)  
**Status:** IMPLEMENTED (PR #1826)  
**Scope:** Gated production deploys to web.edumips.org, rollback via /prev, versioning split, per-commit candidate channel

---

## Problem Statement

Andrea wants to let AI agents merge to `master` freely for development agility, but production deploys to `web.edumips.org` must be **gated by an explicit human action**. Previously, every push to master auto-deployed to production via `release.yml`'s `deploy-prod` job — incompatible with "agents loose on master."

Three sub-problems solved:
1. **Manual promotion** — decouple merge-to-master from deploy-to-prod.
2. **Fast rollback** — `https://web.edumips.org/prev/` serves the previous promoted release.
3. **Versioning** — unified build identity via `git describe` while web ships continuously with a promotion counter.

---

## Current State (pre-implementation baseline)

| Aspect | Details |
|--------|---------|
| **Prod hosting** | GitHub Pages from `EduMIPS64/web.edumips.org` repo (branch `master`). DNS: `web.edumips.org`. |
| **Prod deploy trigger** | `release.yml` → `deploy-prod` job runs on every push to master + workflow_dispatch. Auth: `PAT_WEBUI`. |
| **PR previews** | Azure Blob Storage (`edumips64ci`). URL: `https://edumips64ci.z16.web.core.windows.net/<PR>/`. |
| **Versioning** | `gradle.properties`: `version=1.4.1`, `codename="WalkOfLife"`. Single version covers all artifacts. Git tags: `v1.4.1`. |
| **Build output** | GWT worker.js + React/webpack bundle → `out/web/`. Core + web UI ship together. |
| **Merge settings** | Squash + rebase only (no merge commits). Protected `master`. |
| **Artifact retention** | GitHub Actions: 90 days default. |

---

## Chosen Implementation

This section describes the system as built. All workflows live in `.github/workflows/`; the shared deploy script is `.github/scripts/deploy-web-pages.py`.

### Phase 0 — Disable Auto-Deploy

`release.yml`'s `deploy-prod` job is disabled (`if: false`). The job is kept for emergency use but does not fire on master pushes. Production stays on its last deployed version until an explicit promotion.

### Manual Gated Promotion

**Workflow:** `.github/workflows/promote-web.yml` (`workflow_dispatch`)

**Inputs:** `run_id` (optional) — the CI run whose `web` artifact to promote. Behavior depends on whether it is provided:
- **Empty `run_id`** → **Build+promote current master:** a `build` job (using reusable `build-web.yml`) builds and tests the current master, then the `promote` job deploys that fresh artifact. One-click "ship current master" mode. Security preserved: `build-web.yml` has `contents: read` only and never sees `PAT_WEBUI`; the credential lives only in the `promote` job.
- **`run_id` provided** → **Promote a specific validated run:** skip the build; validate that the given run is from this repo, ran `ci.yml`, concluded `success`, on `master` branch, and has a `web` artifact. Deploy that immutable artifact. This preserves rollback (re-promote a previously-validated run by id), re-promote (run the same id again), and audit (promote an arbitrary run by explicit id).

**Dispatch requirement:** Must be dispatched from the `master` branch in both modes.

**Access control:** `if: github.actor == 'lupino3'` — only Andrea can trigger.

**Steps (build+promote mode, no `run_id`):**
1. **Build** (via reusable `build-web.yml` on current `master`): GWT worker + React/webpack bundle to `out/web/`, no secrets required.
2. **Promote** (same flow as below, using current run id as `SOURCE_RUN_ID`).

**Steps (promote-specific-run mode, `run_id` provided):**
1. **Validate** the source CI run: confirms it belongs to this repo, ran `ci.yml`, concluded `success`, on `master` branch, and has a `web` artifact. Use the validated SHA and `run_id` as `SOURCE_RUN_ID`.
2. **Checkout** this repo at the validated SHA with `fetch-depth: 0`.
3. **Compute build identity:** `git describe --tags --match 'v*' --always <SHA> | sed 's/^v//'` → e.g. `1.4.0-2-gabc1234`. Also reads `targetRelease` from `gradle.properties`.
4. **Download** the immutable `web` artifact from the source run (current run if build mode, or validated external run if promote-specific mode).
5. **Clone** the Pages repo (`EduMIPS64/web.edumips.org`).
6. **Run** `deploy-web-pages.py promote <artifact_dir> <build> <sha> <targetRelease> <actor>`:
   - Archives current root files → `/prev/` (on first run, seeds `/prev/` from existing root).
   - Copies artifact → `/v/<N+1>/` (immutable snapshot) and to repo root (current).
   - Writes `manifest.json` with promotion number, SHA, build string, timestamp, actor.
   - Prunes `/v/` entries beyond the 50 most recent.
   - Ensures `CNAME` and `.nojekyll` are preserved.
7. **Commit and push** to Pages repo.

**Concurrency:** The `promote` job uses `concurrency: group: web-pages-deploy` with `cancel-in-progress: false` to serialise Pages writes safely across promote, rollback, and candidate operations. The `build` job (in fresh mode) is not serialised — parallel builds of different master snapshots do not conflict.

### Rollback

**Workflow:** `.github/workflows/rollback-web.yml` (`workflow_dispatch`, no inputs)

**Access control:** `if: github.actor == 'lupino3'`

**Mechanism:** `deploy-web-pages.py rollback <actor>` swaps `/prev/` contents into the repo root and updates `manifest.json` (sets `current` to what was `prev`, marks `rolledBackFrom`). No artifact download needed — instant emergency recovery.

**Normal rollback** (non-emergency): re-run `promote-web.yml` with the `run_id` of the previous good build; the system treats it as a new promotion.

### Candidate Channel

**Workflow:** `.github/workflows/candidate-web.yml`

**Trigger:** 
- `workflow_run` event after a successful **CI Build** (`ci.yml`) on `master` branch (per-commit, replacing the old daily nightly cron).
- `workflow_dispatch` for on-demand manual deployment.

**Mechanism:**
1. Finds the successful `ci.yml` run that triggered the workflow_run event (or the latest green run if manual dispatch).
2. Downloads its `web` artifact.
3. Computes the date-based candidate directory path: `<YYYY-MM-DD>/<N>-<shortsha>/` where:
   - `<YYYY-MM-DD>` is the current date.
   - `<N>` is an incrementing counter (1-based) for that day (resets daily).
   - `<shortsha>` is the abbreviated commit SHA.
4. Runs `deploy-web-pages.py candidate <artifact_dir> <date> <N> <sha> <shortsha> <build> <targetRelease>` to:
   - Deploy the artifact to `/<YYYY-MM-DD>/<N>-<shortsha>/`.
   - Update `/candidates.json` (root index file) with metadata for the new candidate.
   - Prune candidates older than 14 days, sorted by date.
   - Ensure `candidates.json` is in `RESERVED_NAMES` (protected from promotion/rollback).
5. Commits and pushes to Pages repo.

**Ungated** — no actor restriction. Candidate builds provide a preview channel for contributors and agents.

**UI indication:** The web UI's `Header.js` component detects the candidate path prefix (`/<YYYY-MM-DD>/<N>-<shortsha>/`) at runtime and displays a **"CANDIDATE"** badge (purple) so users clearly see they are not on the stable production channel.

**Build identity & metadata:** Each candidate build includes git-describe string, SHA, and the candidate date/N identifier (no promotion number — those are reserved for gated production promotions).

**Root index file (`/candidates.json`):**
```json
{
  "candidates": [
    { "date": "2026-06-13", "n": 3, "sha": "abc1234567890123", "shortsha": "abc1234", "path": "2026-06-13/3-abc1234", "build": "1.4.0-7-gabc1234", "targetRelease": "1.4.1", "deployedAt": "2026-06-13T15:30:45Z" },
    { "date": "2026-06-13", "n": 2, "sha": "def5678901234567", "shortsha": "def5678", "path": "2026-06-13/2-def5678", "build": "1.4.0-6-gdef5678", "targetRelease": "1.4.1", "deployedAt": "2026-06-13T14:22:10Z" }
  ],
  "retentionDays": 14
}
```
Candidates are sorted newest-first for easy discovery of the latest build.

### Versioning Model (Unified Build Identity, Split Release Label)

The system uses three complementary identifiers:

| Layer | Identifier | Source | Purpose | Advances |
|-------|-----------|--------|---------|----------|
| **Release label** | `1.4.1` | `gradle.properties version=` | Git tags, JAR/MSI naming | On tagged desktop releases (manual) |
| **Build identity** | `1.4.0-2-gabc1234` | `git describe --tags --match 'v*' --always --dirty` (leading `v` stripped) | Displayed everywhere; unique per commit | Every commit (auto-derived, monotonic) |
| **Promotion number** | `44` | `manifest.json` in Pages repo | Web deploy counter, rollback UX | On each production promotion |

**How build identity is wired:**

| Surface | Mechanism |
|---------|-----------|
| **Desktop (Swing) + CLI** | `build.gradle.kts` `sharedManifest` writes git-describe to `Signature-Version` → `MetaInfo.java` reads it at class-load. Flows automatically to window title, status bar, crash dialog, CLI `--version`. |
| **Web UI** | `webpack.config.js` uses `GitRevisionPlugin` → injected via `DefinePlugin` into `index.js`. |
| **User manual (Sphinx docs)** | `docs/user/common_conf.py` reads git-describe for the `version`/`release` config. |

**CI requirement:** All workflows that need build identity use `fetch-depth: 0` in `actions/checkout` so `git describe` has full tag history.

At a tagged commit (e.g. `v1.4.1`), `git describe` yields exactly `1.4.1` — no special-case release logic needed.

### Pages Layout

All content lives in the separate Pages repo `EduMIPS64/web.edumips.org`:

```
EduMIPS64/web.edumips.org repo:
├── index.html              ← current prod (physical copy)
├── [all current files]     ← current prod assets
├── prev/                   ← previous prod version (rollback target)
├── v/
│   ├── 42/                 ← immutable snapshot (prod promotion 42)
│   ├── 43/                 ← immutable snapshot (= prev)
│   └── 44/                 ← immutable snapshot (= current)
├── 2026-06-13/             ← candidate builds for date (per-commit)
│   ├── 1-abc1234/
│   ├── 2-def5678/
│   └── 3-ghi9012/
├── 2026-06-12/
│   ├── 1-jkl3456/
│   └── 2-mno7890/
├── candidates.json         ← candidate metadata (sorted newest-first, 14-day retention)
├── manifest.json           ← prod metadata (see below)
├── CNAME                   ← preserved (web.edumips.org)
└── .nojekyll               ← preserved
```

**manifest.json example:**
```json
{
  "current": 44,
  "prev": 43,
  "sha": "abc1234",
  "build": "1.4.0-2-gabc1234",
  "targetRelease": "1.4.1",
  "promotedAt": "2026-06-07T18:49:37Z",
  "promotedBy": "lupino3"
}
```

The structure is bootstrapped automatically on the first `promote-web.yml` run — no manual setup of the Pages repo is required.

### URL Scheme

| URL | Content |
|-----|---------|
| `https://web.edumips.org/` | Current promoted version (stable) |
| `https://web.edumips.org/prev/` | Previous promoted version (rollback target) |
| `https://web.edumips.org/v/44/` | Immutable snapshot by promotion number |
| `https://web.edumips.org/<YYYY-MM-DD>/<N>-<shortsha>/` | Candidate build (per-commit, ungated) |
| `https://web.edumips.org/candidates.json` | Machine-readable candidate metadata |
| `https://web.edumips.org/manifest.json` | Machine-readable promotion metadata |

### What Stays the Same

- PR preview system (Azure Blob) — unchanged.
- Desktop release process — unchanged.
- `gradle.properties version=` — unchanged (remains the release/target label).
- CI checks on PRs — unchanged.

---

## Alternatives Considered

Organized by decision point. Each lists the option(s) we evaluated and rejected, with rationale.

### Promotion Trigger

| Rejected option | Rationale |
|-----------------|-----------|
| **(A) Environment approval gate** — add a `Production` GitHub Environment with required reviewers to the existing `deploy-prod` job; every master push creates a pending deployment awaiting approval. | Creates notification noise on every merge. No way to skip/batch. Artifact is rebuilt per push (not immutable promotion of an already-tested build). |
| **(C) Git tag triggers deploy** — pushing a tag like `web-v<N>` triggers prod deploy by rebuilding from tag. | Rebuilding from tag means what you tested ≠ what you deploy (non-immutable). Agents could accidentally push tags. Extra manual step. |
| **(D) Hard build+promote, dropping run_id** — a single workflow that ALWAYS rebuilds master and promotes, with no `run_id` input. | Removes the ability to re-promote / roll back by promoting a previously-validated run by id. Wastes CI resources by always rebuilding when an identical green artifact already exists. Loses audit trail of which runs have been shipped. |
| **(E) Separate build→promote orchestrator** — keep two workflows but add a third that chains "trigger build, wait for green, then promote". | Cross-workflow orchestration is awkward in Actions (dispatch-then-wait has no native support). No capability gain over making `run_id` optional within a single workflow. Adds UI complexity (three workflows instead of one). |
| **Auto-promote on green CI + time delay** — automatically promote the latest green build after N hours if no issues are raised. | Defeats the purpose of human gating. Andrea specifically wants an explicit manual action for production. |

### Rollback Hosting

| Rejected option | Rationale |
|-----------------|-----------|
| **Git-history rollback (A')** — `git revert` a commit in the Pages repo to roll back, no `/prev/` URL. | Slow (requires git operations), no instant one-click recovery, no `/prev/` preview URL for verification. |
| **Azure Blob / external CDN** — host versioned history externally with pointer-based routing. | Requires DNS migration, introduces cost and infra complexity for marginal benefit at current scale. The chosen `/prev/` directory on GitHub Pages is simpler and free. |

### Versioning Scheme

| Rejected option | Rationale |
|-----------------|-----------|
| **(i) Single unified version across all components** — `1.4.1` for everything, web promotions use `1.4.1+build.44`. | Web ships continuously; version is stale between desktop releases. Many builds share `1.4.1` — not point-in-time. |
| **(iii) Fully independent semver per component** — `core-2.0.0`, `swing-1.4.1`, `web-1.3.0`. | Over-engineered. Core is embedded in both Swing and Web and is not independently shippable. Creates coordination burden with no clear user benefit. |
| **(iv) Core+shell decomposition** — separate semver for Core vs shell versions. | Requires defining a fuzzy "core change" boundary (e.g. ResultFactory). The git SHA in the manifest already provides full traceability without a separate core release process. |

### Build Identity Source

| Rejected option | Rationale |
|-----------------|-----------|
| **(a) Status quo** — `gradle.properties version=` embedded verbatim. | Not unique between releases — all interim commits share the same string. |
| **(b) SNAPSHOT suffix** — `1.4.1-SNAPSHOT`. | Still shared build-to-build; marks "not a release" but doesn't distinguish individual commits. |
| **GITHUB_RUN_NUMBER** — auto-incrementing CI counter. | Not portable outside CI. Not meaningful to users. Doesn't collapse to a clean label at tagged releases. |
| **Hand-maintained Build-Qualifier** — the old `alpha`+SHA mechanism. | Inconsistent (`1.4.1-alpha-abc1234` in CI vs `1.4.1` at release). Redundant once git-describe is adopted. Potential NPE if attribute absent. |

### Candidate Trigger

| Rejected option | Rationale |
|-----------------|-----------|
| **(B) Deploy on every green master push** (original nightly design) — `workflow_run` triggered after each successful CI on master, deployed to `/nightly/` with a daily cron. | Replaced by per-commit candidate builds triggered on `workflow_run` after successful CI, deployed to `/<YYYY-MM-DD>/<N>-<shortsha>/` with automatic pruning (14-day retention). The new design provides fresher feedback on every green commit, indexed by date for discoverability, without a stale daily schedule. |
| **Longer fixed interval** (e.g. weekly cron, previous alternative) | Stale for too long; defeats the purpose of giving contributors a fast feedback loop on merged work. Per-commit deployment solves this. |

### PR Grouping

| Rejected option | Rationale |
|-----------------|-----------|
| **Multi-PR approach** — split into separate PRs per phase (Phase 0 PR, Phase 1-2 PR, Phase 3 PR, etc). | Adds coordination overhead with no safety benefit. All changes are additive; disabling `deploy-prod` + adding `promote-web.yml` in the same PR is safe because promotion is `workflow_dispatch` (Andrea controls when). Pages-repo structure is bootstrapped automatically on first promotion run. |

### Hosting Platform for Production

| Rejected option | Rationale |
|-----------------|-----------|
| **(B) Azure Blob Storage** — host prod on Azure Blob with CDN for custom domain. Path-prefix versioning is native, no file duplication. | Architecturally cleaner but introduces DNS migration risk, non-zero monthly cost, and more infra to maintain. GitHub Pages is free, reliable, and already configured. Remains an optional Phase 4 evolution if needed. |
| **(C) Hybrid** — GitHub Pages for root + Azure for versioned history at a different subdomain. | Two hosting systems, `/prev` on a different domain (bad UX), complexity without clear benefit. |

---

## Phased Rollout

| Phase | Scope | Effort | Outcome |
|-------|-------|--------|---------|
| **Phase 0** ✅ | Disable auto-deploy: `if: false` on `deploy-prod` in `release.yml`. | 5 min | Agents can merge without shipping to prod. |
| **Phase 1–2** ✅ | `promote-web.yml` with full versioned layout (`/v/N/`, `/prev/`, `manifest.json`, prune at 50). | 2-3 hours | Manual promotion + rollback history. |
| **Phase 3** ✅ | `rollback-web.yml` for emergency swap. Git-describe build identity in desktop+CLI (`build.gradle.kts` `sharedManifest` → `MetaInfo`), web (`webpack.config.js` `GitRevisionPlugin`), docs (`common_conf.py`). `fetch-depth: 0` across workflows. | 2-3 hours | Complete versioning + rollback. |
| **Phase 3.5** ✅ | `candidate-web.yml` (per-commit `workflow_run` after green master CI + manual dispatch) + `/<YYYY-MM-DD>/<N>-<shortsha>/` + `/candidates.json` + UI CANDIDATE badge (purple, `Header.js`). Replaces nightly (daily cron removed). | 2-3 hours | Per-commit preview for contributors, indexed by date, 14-day retention. |
| **Phase 4** (future, optional) | Migrate prod hosting to Azure Blob + CDN. | 4-8 hours | Eliminates file duplication, enables pointer-based routing. |

---

## In-app Previous-Version Navigator

**Date:** 2026-06-08  
**Status:** IMPLEMENTED  
**Feature:** Users can browse and open previous web versions from the About tab in the Help dialog.

### Implementation

The web UI's About tab now fetches the root `/manifest.json` (with `cache:'no-cache'` to bypass browser cache) and, if successful and the build is not a PR preview, displays a **"Previous Versions"** list. Each retained version (up to 50) is listed with its promotion number, build identity, SHA, and promotion date/actor. The current version is marked. Clicking an older version opens it at `https://web.edumips.org/v/<N>/` in a new tab, preserving the user's current work.

When viewing an archived snapshot at `/v/<N>/`, the About tab displays a "Return to latest" link instead, making navigation back to the current version obvious.

The `manifest.json` `history` array entry schema:
```json
{ "n": 44, "build": "1.4.0-2-gabc1234", "sha": "abc1234", "targetRelease": "1.4.1", "promotedAt": "2026-06-07T18:49:37Z", "promotedBy": "lupino3" }
```

**Gating:** The navigator only appears if:
1. `/manifest.json` fetch succeeds and parses.
2. The build is NOT a PR preview (detected via `window.GIT_DESCRIBE`).

This ensures the navigator is available on stable production and candidate builds but hidden for temporary PR preview builds.

**Related fix:** Promotion numbers now use **monotonic numbering** — the next version is always `max(ever used) + 1`, never re-used. This fixes a latent bug where a promote after a rollback could re-use and mutate an existing immutable snapshot.

### Rejected Alternatives

| Option | Rationale |
|--------|-----------|
| **(A) Per-snapshot `/v/<N>/manifest.json`** — Each archived snapshot carries its own metadata file; the UI probes `/v/1/manifest.json`, `/v/2/manifest.json`, etc. in a loop to discover versions. | Inefficient (many 404s on missing versions), no centralized source-of-truth, harder to back-fill history on the first post-feature promotion. |
| **(B) UI-only blind enumeration** — UI enumerates `/v/1/`, `/v/2/`, etc. by trying to fetch `index.html` from each and uses the HTTP status code to detect existence. Labels are number-only (no dates, no build identity). | Relies on HTTP probe pattern-matching (brittle), no date/build metadata, poor UX (just "Version 44" with no context). |

---

## Resolved Decisions (2026-06-07)

| # | Question | Decision |
|---|----------|----------|
| 1 | Who can promote? | Andrea (lupino3) only. `workflow_dispatch` + actor check is sufficient. |
| 2 | Phase 0 now? | Yes — no pending releases. |
| 3 | Artifact retention? | Accept 90-day limit; rebuild from SHA if expired. No durability backstop. |
| 4 | GitHub Pages vs Azure? | Keep GitHub Pages. Azure = optional Phase 4. |
| 5 | Versions to retain in `/v/`? | 50. |
| 6 | Build identity surfaces? | git-describe everywhere (desktop, CLI, web, docs). |
| 7 | `package.json` version? | Leave stale at `1.0.0`; add comment noting it is unused. |
| 8 | Workflow naming? | `promote-web.yml` (unifies build+promote with optional `run_id`), `rollback-web.yml`, `candidate-web.yml`. `promote-web.yml` serves both "build+promote current master" (empty `run_id`) and "promote a specific validated run" (with `run_id`). |
| 9 | Candidate trigger? | Per-commit `workflow_run` on green master CI + `workflow_dispatch`. (Per-commit replaces the original daily 01:00 UTC cron design, providing fresher feedback on every merge.) |
| 10 | PR grouping? | Single PR (PR #1826) for all `edumips64` repo changes. Pages-repo layout bootstraps on first promotion run. |
| 11 | Version navigator? | Fetch root `/manifest.json` with `cache:'no-cache'`, render history list in About tab (gated on valid manifest + non-PR build). Monotonic numbering (next n = max ever used + 1) fixes post-rollback collision bug. |
