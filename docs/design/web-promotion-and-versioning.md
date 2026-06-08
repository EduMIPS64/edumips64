# Design: Web UI Production Promotion & Versioning

**Author:** Morpheus (Lead/Architect)  
**Date:** 2026-06-05 (decisions locked 2026-06-07)  
**Status:** IMPLEMENTED (PR #1826)  
**Scope:** Gated production deploys to web.edumips.org, rollback via /prev, versioning split, nightly channel

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

**Inputs:** `run_id` (required) — the CI run whose `web` artifact to promote. Optional `sha` (derived from run if omitted).

**Access control:** `if: github.actor == 'lupino3'` — only Andrea can trigger.

**Steps:**
1. **Validate** the source CI run: confirms it belongs to this repo, ran `ci.yml`, concluded `success`, on `master` branch, and has a `web` artifact.
2. **Checkout** this repo at the validated SHA with `fetch-depth: 0`.
3. **Compute build identity:** `git describe --tags --match 'v*' --always <SHA> | sed 's/^v//'` → e.g. `1.4.0-2-gabc1234`. Also reads `targetRelease` from `gradle.properties`.
4. **Download** the immutable `web` artifact from the validated run.
5. **Clone** the Pages repo (`EduMIPS64/web.edumips.org`).
6. **Run** `deploy-web-pages.py promote <artifact_dir> <build> <sha> <targetRelease> <actor>`:
   - Archives current root files → `/prev/` (on first run, seeds `/prev/` from existing root).
   - Copies artifact → `/v/<N+1>/` (immutable snapshot) and to repo root (current).
   - Writes `manifest.json` with promotion number, SHA, build string, timestamp, actor.
   - Prunes `/v/` entries beyond the 50 most recent.
   - Ensures `CNAME` and `.nojekyll` are preserved.
7. **Commit and push** to Pages repo.

**Concurrency:** All Pages writes (promote, rollback, nightly) share a single `concurrency: group: web-pages-deploy` with `cancel-in-progress: false` to serialise safely.

### Rollback

**Workflow:** `.github/workflows/rollback-web.yml` (`workflow_dispatch`, no inputs)

**Access control:** `if: github.actor == 'lupino3'`

**Mechanism:** `deploy-web-pages.py rollback <actor>` swaps `/prev/` contents into the repo root and updates `manifest.json` (sets `current` to what was `prev`, marks `rolledBackFrom`). No artifact download needed — instant emergency recovery.

**Normal rollback** (non-emergency): re-run `promote-web.yml` with the `run_id` of the previous good build; the system treats it as a new promotion.

### Nightly Channel

**Workflow:** `.github/workflows/nightly-web.yml`

**Trigger:** Scheduled cron `0 1 * * *` (daily at 01:00 UTC) + `workflow_dispatch` for on-demand use.

**Mechanism:**
1. Finds the latest successful `ci.yml` run on `master` via `gh run list`.
2. Downloads its `web` artifact.
3. Runs `deploy-web-pages.py nightly <artifact_dir>` — replaces the `/nightly/` directory with the artifact contents.
4. Commits and pushes to Pages repo.

**Ungated** — no actor restriction. Nightly is a preview channel for contributors and agents.

**UI indication:** The web UI's `Header.js` component detects the `/nightly/` path prefix at runtime and displays a "NIGHTLY" badge so users clearly see they are not on the stable production channel.

**Build identity:** Nightly is identified only by its git-describe string and SHA (no promotion number — those are reserved for gated production promotions).

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
├── nightly/                ← overwritten daily by nightly-web.yml
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
| `https://web.edumips.org/nightly/` | Latest green master (daily, ungated) |
| `https://web.edumips.org/manifest.json` | Machine-readable version metadata |

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

### Nightly Trigger

| Rejected option | Rationale |
|-----------------|-----------|
| **(B) Deploy on every green master push** — `workflow_run` triggered after each successful CI on master. | Multiple deploys per day on active days wastes resources and makes the nightly channel less predictable. A daily cadence keeps the preview fresh enough while avoiding a Pages deploy on every single merge. Manual `workflow_dispatch` covers the "I want it now" case. |
| **Longer fixed interval** (e.g. weekly cron) | Stale for too long; defeats the purpose of giving contributors a fast feedback loop on merged work. |

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
| **Phase 3.5** ✅ | `nightly-web.yml` (daily 01:00 UTC cron + manual dispatch) + `/nightly/` + UI NIGHTLY badge (`Header.js`). | 1-2 hours | Continuous preview for contributors. |
| **Phase 4** (future, optional) | Migrate prod hosting to Azure Blob + CDN. | 4-8 hours | Eliminates file duplication, enables pointer-based routing. |

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
| 8 | Workflow naming? | `promote-web.yml`, `rollback-web.yml`, `nightly-web.yml`. |
| 9 | Nightly trigger? | Daily 01:00 UTC cron + `workflow_dispatch`. (Changed from initial "on-every-green-master-push" recommendation.) |
| 10 | PR grouping? | Single PR (PR #1826) for all `edumips64` repo changes. Pages-repo layout bootstraps on first promotion run. |
