# Design: Web UI Production Promotion & Versioning

**Author:** Morpheus (Lead/Architect)  
**Date:** 2026-06-05 (decisions locked 2026-06-07)  
**Status:** DECISIONS LOCKED — ready for implementation  
**Scope:** Gated production deploys to web.edumips.org, rollback via /prev, versioning split, nightly channel

---

## Problem Statement

Andrea wants to let AI agents merge to `master` freely for development agility, but production deploys to `web.edumips.org` must be **gated by an explicit human action**. Today, every push to master auto-deploys to production via `release.yml`'s `deploy-prod` job. This is incompatible with "agents loose on master."

Three sub-problems:
1. **Manual promotion** — decouple merge-to-master from deploy-to-prod.
2. **Fast rollback** — `https://web.edumips.org/prev` serves the previous promoted release.
3. **Versioning** — rethink unified version (`1.4.1`) given web ships continuously while desktop rarely.

---

## Current State (verified)

| Aspect | Details |
|--------|---------|
| **Prod hosting** | GitHub Pages from `EduMIPS64/web.edumips.org` repo (branch `master`). DNS: `web.edumips.org`. |
| **Prod deploy trigger** | `release.yml` → `deploy-prod` job runs on every push to master + workflow_dispatch. Uses `crazy-max/ghaction-github-pages@v5` with `keep_history: true`. Auth: `PAT_WEBUI`. |
| **Prod deploy content** | Downloads `out/web` artifact from the same workflow run, pushes to Pages repo. |
| **PR previews** | Azure Blob Storage (`edumips64ci`, container `$web`, path `/<PR>/`). URL: `https://edumips64ci.z16.web.core.windows.net/<PR>/`. Uses OIDC + GitHub Environments (`Staging` = approval-gated for forks, `staging-dependabot` = auto). |
| **Versioning** | `gradle.properties`: `version=1.4.1`, `codename="WalkOfLife"`. `package.json`: `version=1.0.0` (stale). Single version covers all artifacts. Git tags: `v1.4.1`. |
| **Build output** | GWT worker.js + React/webpack bundle → `out/web/`. Core + web UI ship together. |
| **Merge settings** | Squash + rebase only (no merge commits). Protected `master`. |
| **Artifact retention** | GitHub Actions: 90 days default. |

---

## Part 1 — Manual Promotion System

### Options

| Option | How it works | Pros | Cons |
|--------|-------------|------|------|
| **(A) Environment approval gate** | Add a GitHub Environment (e.g. `Production`) with required reviewers to the existing `deploy-prod` job. Workflow still triggers on push to master, but the job waits for manual approval before deploying. | Minimal change (add env + approval rule). Familiar GitHub UX. Full audit trail via Environment deployments tab. | Every master push creates a pending deployment request → notification noise. No way to skip/batch — you approve or reject each one. Artifact is rebuilt per push (not immutable promotion). |
| **(B) Dedicated `workflow_dispatch` promote workflow** | New workflow `promote-web.yml` triggered manually. Input: a git SHA or run ID. Downloads the `web` artifact from that CI run (or rebuilds from SHA), then deploys to prod. Remove auto-deploy from `release.yml`. | Full control — promote any build at any time. No notification noise. Can promote older builds. Decouples CI from CD cleanly. | Requires the artifact to still exist (90-day retention). If artifact expired, must rebuild (non-immutable). Slightly more setup. |
| **(C) Git tag triggers deploy** | Pushing a tag like `web-v<N>` triggers prod deploy. Could rebuild from tag or download artifact from the CI run on that commit. | Familiar release model. Tags are permanent (no retention issue if rebuilding). Clear audit trail in git history. | Rebuilding from tag means what you tested ≠ what you deploy (non-immutable). Agents could accidentally push tags (must restrict). Extra manual step (tag + push). |
| **(D) Immutable artifact store + pointer swap** | Each CI run on master stores a versioned, immutable build artifact in a durable location (Azure Blob at `/v/<N>/` or GitHub release asset). Promotion = copy/symlink that version to the "current" slot. | True immutable promotion — what you tested is exactly what ships. No 90-day retention problem (blobs persist until deleted). Supports rollback by re-pointing. Maps to PR preview infra you already have. | More infra setup (Azure blob for prod or GitHub release assets). DNS/hosting change if moving off GitHub Pages. |

### Analysis

The key architectural question is: **rebuild vs. promote an already-built artifact?**

- **Rebuild from SHA/tag** (Options A, C): simpler infra, but violates immutability. A Gradle/npm update between build and promotion could subtly change output. Acceptable if you trust reproducibility, but philosophically weaker.
- **Promote existing artifact** (Options B, D): stronger guarantee. The exact bytes tested in CI are what reach production. Requires artifact durability beyond 90 days (GitHub's limit) OR promoting within 90 days (realistic for a continuously-shipping web app).

**Retention concern:** If Andrea promotes within ~60 days of a build (likely, given continuous merges), GitHub Actions artifact retention (90 days) is fine for Option B. For longer retention, artifacts can be attached to GitHub Releases (permanent) or stored in Azure Blob (permanent until deleted).

### Recommendation: Option B + D hybrid

**Primary mechanism:** A `workflow_dispatch` workflow (`promote-web.yml`) that:
1. Takes a **CI run ID** (or commit SHA) as input.
2. Downloads the immutable `web` artifact from that CI run.
3. Deploys it to production (GitHub Pages — decided 2026-06-07).
4. Records the promotion in a manifest (version, SHA, timestamp, promoter).

**Promoter:** Andrea (lupino3) only for now. The `workflow_dispatch` trigger is admin-only by default, which provides sufficient access control. No additional GitHub Environment approval gate needed.

**Artifact retention:** Accept the 90-day GitHub Actions limit. If an artifact expires before promotion, rebuild from the SHA. This is acceptable — we can always rebuild, and promotion within 90 days is realistic for a continuously-merging project. No durability backstop (GitHub Release asset / Azure Blob) needed for now.

**Migration path:**
- Phase 1: Add `promote-web.yml`, remove auto-deploy from `release.yml`'s `deploy-prod` job (or gate it behind `if: false` / delete it).
- Phase 2 (optional): Move to Azure Blob hosting for prod to unlock path-prefix versioning (see Part 2).

### Open Questions (Part 1) — RESOLVED

1. ~~Should the promote workflow require a GitHub Environment approval too?~~ **No** — `workflow_dispatch` admin-only is sufficient. Andrea is the sole promoter for now. *(Resolved 2026-06-07)*
2. Should we keep `release.yml`'s desktop release logic intact and only remove/gate the web deploy job, or refactor into separate workflows? *(Deferred — keep desktop logic intact, only disable `deploy-prod`.)*
3. ~~Who besides Andrea should be able to trigger the promote workflow?~~ **Andrea only for now.** *(Resolved 2026-06-07)*

---

## Part 2 — "Last Known Good" / Rollback URL

### Goal

- `https://web.edumips.org/` → current promoted version
- `https://web.edumips.org/prev/` → immediately previous promoted version
- Fast rollback = swap current ↔ prev (or re-promote an older version)
- Keep N recent versions accessible at `/v/<N>/`

### Hosting Options

| Option | How | Pros | Cons |
|--------|-----|------|------|
| **(A) Stay on GitHub Pages** with directory layout | Deploy each promotion into `/v/<N>/` subdirectory. Root `/index.html` = copy of current version's files (or JS redirect). `/prev/index.html` = copy of previous version's files. On promotion: copy current→prev, copy new→current, store in /v/N/. | No infra change. Free hosting. Existing DNS works. | No server-side redirects on GitHub Pages — must physically copy files into `/`, `/prev/`, `/v/N/` (bloats repo). Alternatively use `<meta http-equiv="refresh">` or JS redirect from `/` to `/v/current/` (visible URL change, bad UX). Repo size grows with copies. |
| **(A') GitHub Pages** with single-deploy + git-history rollback | Keep deploying to repo root as today. Rollback = `git revert` to a previous commit in the Pages repo. No /prev URL. | Simplest. Already works (keep_history: true retains commits). | No /prev URL. Rollback requires git operations on the Pages repo (slow, manual). No version browsing. |
| **(B) Azure Blob Storage** (same as previews) | Host prod on Azure Blob (`edumips64ci` or a dedicated storage account). Structure: `/v/<N>/`, `/current/` (copy of latest promoted), `/prev/` (copy of previous). Root `/` could be a small index.html that redirects to `/current/` or files are duplicated at root. CDN (Azure CDN or Cloudflare) in front for custom domain. | Path-prefix versioning is native. True instant rollback (re-copy blobs). No repo bloat. Same infra pattern as PR previews — team knows it. Scales to N versions trivially. | Requires DNS change (move `web.edumips.org` from GitHub Pages to Azure/CDN). Monthly cost (likely <$1/mo for static assets, but non-zero). More infra to maintain. Azure OIDC already configured. |
| **(C) Hybrid: GitHub Pages root + Azure for versioned history** | Keep GitHub Pages for `/` (current). Azure hosts `/prev/` and `/v/<N>/` at a different subdomain (e.g. `archive.web.edumips.org`). | Keeps current prod infra for the happy path. Versioned history is separate. | Two hosting systems. `/prev` URL would be on a different domain (bad UX). Complexity without clear benefit. |

### How `/prev` advances

On each promotion:
```
/v/<N+1>/  ← new promoted build lands here
/current/  ← overwritten with /v/<N+1>/ content (or pointer)
/prev/     ← overwritten with /v/<N>/ content (previous current)
```

Rollback = re-promote version N (which re-runs the same logic: current→prev, N→current). Or, for emergency: swap current↔prev at the file/blob level.

### GitHub Pages directory layout (Option A, detailed)

```
EduMIPS64/web.edumips.org repo structure:
├── index.html          ← current version (physical copy)
├── [all current files] ← current version assets
├── prev/
│   ├── index.html      ← previous version
│   └── [prev assets]
├── v/
│   ├── 42/             ← version 42
│   ├── 43/             ← version 43 (= prev)
│   └── 44/             ← version 44 (= current)
└── manifest.json       ← {current: 44, prev: 43, versions: [...]}
```

The promote workflow would:
1. Download artifact for the new version.
2. Compute next version number (from manifest.json).
3. Deploy into `/v/<N+1>/`.
4. Copy `/v/<N+1>/` files to repo root (current).
5. Copy `/v/<N>/` files to `/prev/`.
6. Update `manifest.json`.
7. Push to Pages repo.

**Concern:** File duplication (3 copies of each release: root + prev + v/N). Each build is ~2-5 MB, so after 50 releases: ~250 MB repo. Manageable but inelegant. Prune `/v/` entries older than 50 versions on each promotion (decided 2026-06-07).

### Recommendation: Option A (GitHub Pages with directory layout) — CONFIRMED

**Decision (2026-06-07):** Stay on GitHub Pages for now. Azure is architecturally cleaner but introduces migration risk and cost for marginal benefit at current scale.

**Rationale:**
- No infra migration needed. GitHub Pages is free, reliable, already configured.
- The `promote-web.yml` workflow already touches the Pages repo — extending it to manage `/prev/` and `/v/N/` is straightforward.
- Repo size growth is manageable (prune `/v/` entries older than **50 versions** — decided 2026-06-07).
- `/prev/` as a physical directory with copied files works on GitHub Pages without redirects.
- Moving to Azure (Option B) remains an optional Phase 4 evolution if needed later.

**If Andrea prefers Azure (Option B):** The architecture is cleaner (no file duplication, true pointer-based routing), and the infra is already partially set up. The main cost is DNS migration (point `web.edumips.org` to Azure CDN). I'd support this as a Phase 2 evolution.

### URL scheme (recommended)

| URL | Content |
|-----|---------|
| `https://web.edumips.org/` | Current promoted version (files at repo root) |
| `https://web.edumips.org/prev/` | Previous promoted version |
| `https://web.edumips.org/v/44/` | Specific version by promotion number |
| `https://web.edumips.org/manifest.json` | Machine-readable version metadata |

### Open Questions (Part 2) — RESOLVED

1. ~~Is the repo size growth from file duplication acceptable, or should we move to Azure now?~~ **Acceptable. Stay on GitHub Pages.** *(Resolved 2026-06-07)*
2. Should `/prev` always exist (even on first promotion), or only after the second promotion? *(Deferred — natural to create on second promotion.)*
3. ~~How many versions to retain in `/v/`?~~ **50.** *(Resolved 2026-06-07)*
4. Should the in-app UI show which version is running + link to manifest.json? *(Part of build identity work in Phase 3.)*

---

## Part 3 — Versioning: Unified vs Split

### Current State

One version (`1.4.1` in `gradle.properties`) covers:
- Core simulator (Java, compiled to both JVM bytecode and GWT JS)
- Swing UI (desktop JAR)
- Web UI (React + GWT worker)
- Electron app (wraps web UI)

### The real problem: release label vs build identity

`gradle.properties version=` is a **release label**, not a build identity. It is constant by design between releases — it advances only when the team decides to tag a new release.

**Concrete current state (verified 2026-06-07):**
- Latest released tag: `v1.4.0`
- PR #1803 (first PR after the release) bumped `gradle.properties` to `1.4.1` — but no `v1.4.1` tag exists yet.
- Every commit merged to master since #1803 — uuid CVE fix (#1808), exhaustive-deps fix (#1814), structural-stall fix (#1819), and more — all report `version=1.4.1`.
- Two commits with different code, different SHAs → same version label. The version is **not point-in-time** during the development window.
- The only true point-in-time identifier between releases is the **git SHA**.

This matters for the web promotion scheme: if the web UI displays `1.4.1` and the manifest records `coreVersion: "1.4.1"`, we lose the ability to tell *which* `1.4.1` commit is deployed without also recording the SHA separately.

### Build identity options

Three options were considered; **(c) is the decided approach** (approved by Andrea, 2026-06-07):

| Option | Scheme | Point-in-time? | Notes |
|--------|--------|---------------|-------|
| **(a) Status quo** | `gradle.properties version=` embedded verbatim → `1.4.1` | ✗ — shared by all interim commits | Honest as a target label, but not unique between releases. |
| **(b) SNAPSHOT suffix** | `1.4.1-SNAPSHOT` until release is tagged | Partial — marks "not a release" | Still shared build-to-build; no way to distinguish individual commits. |
| **(c) git-describe-derived identity** ✅ **DECIDED** | `git describe --tags` → e.g. `1.4.0-2-gabc1234` | ✓ — unique, monotonic, auto-derived | "2 commits after v1.4.0 at SHA abc1234." At a tagged commit, collapses to e.g. `1.4.1` exactly. Free — no manual bookkeeping. Applies to **all shipped artifacts** (desktop JAR, Electron, web build). |

**Decision rationale:** option (c) is unique per commit, monotonic, requires zero manual bookkeeping, and dovetails with the web promotion scheme naturally. When a release tag is pushed (e.g. `v1.4.1`), `git describe` yields exactly `1.4.1` at that commit — no special release handling needed.

### Options (unified vs split)

| Option | Scheme | Pros | Cons |
|--------|--------|------|------|
| **(i) Keep unified** | `1.4.1` for everything. Web promotions use this version + build metadata (e.g. `1.4.1+build.44`). | Simple mental model. No coordination overhead. Users see one version. | Web ships continuously → version doesn't advance between desktop releases. Confusing: many builds share `1.4.1`. Agents merging to master don't bump versions → version is stale. |
| **(ii) Separate web version** | `gradle.properties` keeps `1.4.1` for core+Swing releases. Web gets its own version: an auto-incrementing **promotion number** (e.g. `web-44`) or date-based (`2026.06.05`). | Web version advances independently. Clear identity for each promotion. Desktop release cadence unaffected. Simple — just a counter. | Two versioning schemes to explain. Core changes affect both but only one version bumps. |
| **(iii) Three independent versions** | Core: `core-2.0.0`. Swing: `swing-1.4.1`. Web: `web-44`. Each advances independently. | Full decoupling. Each component has its own release lifecycle. | Over-engineered for current scale. Core is embedded in both Swing and Web — it's not independently shippable. Creates coordination burden (which core version does web-44 include?). |
| **(iv) Core+shell decomposition** | Core: semver (`core-1.4.1`), advances when ISA/simulator behavior changes. Web shell: promotion number (`web-44`), advances on any web deploy. Swing shell: tied to core version (desktop releases are infrequent). | Matches the real dependency graph. Core version tracks semantic CPU/ISA changes. Web version tracks deploy cadence. Two versions, not three, reduces overhead. | Requires defining "core change" vs "web-only change" — boundary may be fuzzy (e.g., ResultFactory changes). |

### Analysis

The real architectural seam is:

```
┌─────────────────────────────────────┐
│  Core Simulator (Java/GWT)          │  ← semver, bumps on ISA/behavior changes
│  (CPU, Memory, Parser, Instructions)│
└────────────┬────────────────────────┘
             │ compiled to
     ┌───────┴───────┐
     │               │
┌────▼────┐    ┌─────▼─────┐
│ Swing UI │    │  Web UI    │  ← promotion number, bumps on every prod deploy
│ (JVM)   │    │ (GWT+React)│
└─────────┘    └────────────┘
```

Core changes automatically flow into both Swing and Web. The question is: does core need its own independent version *visible to users*?

**Argument against triple-split:** Core is never released independently. Users consume it through either Swing or Web. Giving it a separate user-facing version adds confusion without clear benefit.

**Argument for core awareness:** When debugging, you want to know "which core does web-44 include?" Answer: the git SHA is sufficient. A manifest entry like `{"promotion": 44, "sha": "abc123", "coreVersion": "1.4.1"}` gives full traceability without requiring a separate core release process.

### Recommended scheme: Option (ii) + decision (c) — layered identity

Three complementary identifiers, not competing:

| Layer | Identifier | Source | Purpose | When it advances |
|-------|-----------|--------|---------|-----------------|
| **Release / target number** | `1.4.1` | `gradle.properties version=` | Release naming and git tag label | On tagged desktop releases (manual, infrequent) |
| **Build identity** | `1.4.0-2-gabc1234` | `git describe --tags` at build time | Embedded in every shipped artifact; displayed to users | Every commit — unique, monotonic, auto-derived |
| **Web promotion number** | `44` | `manifest.json` in Pages repo | Monotonic deploy counter for web; rollback UX | On each production promotion |

These are complementary: a web promotion entry carries **all three** — promotion number + git-describe build string + SHA.

**What users see — all surfaces, same git-describe string:**

| Surface | Example (non-release) | Example (tagged release) |
|---------|-----------------------|--------------------------|
| **Web UI** footer/about | `EduMIPS64 Web — build 44 · 1.4.0-2-gabc1234` | `EduMIPS64 Web — build 44 · 1.4.1` |
| **Swing** window title | `EduMIPS64 1.4.0-2-gabc1234` | `EduMIPS64 1.4.1` |
| **Swing** status-bar welcome | `Welcome to EduMIPS64 1.4.0-2-gabc1234` | `Welcome to EduMIPS64 1.4.1` |
| **Swing** crash/ReportDialog | `EduMIPS64 1.4.0-2-gabc1234 (master@abc1234) …` | `EduMIPS64 1.4.1 (v1.4.1@abc1234) …` |
| **CLI** `--version` | `EduMIPS64 1.4.0-2-gabc1234 …` | `EduMIPS64 1.4.1 …` |

All five surfaces collapse to the clean label (e.g. `1.4.1`) at a tagged commit, and show the full git-describe string in-between releases.

**What lives where:**
- `gradle.properties`: `version=1.4.1` (release/target label — used for tagging and release naming only; **not** the displayed build identity)
- `package.json`: not the primary versioning source; may be aligned to target label or left as-is
- `manifest.json` (in Pages repo): see example below
- Git tags: `v1.4.1` for desktop releases (unchanged). Tagging is what collapses the git-describe string to the clean label.

**manifest.json example:**
```json
{
  "current": 44,
  "prev": 43,
  "sha": "abc1234",
  "build": "1.4.0-2-gabc1234",
  "targetRelease": "1.4.1",
  "promotedAt": "2026-06-07T18:49:37+02:00",
  "promotedBy": "lupino3"
}
```

(`targetRelease` = the `gradle.properties version=` — the release the team is working toward. `build` = the `git describe --tags` string — unique point-in-time identity. `sha` = short commit SHA for direct reference.)

**Why not semver for web?** The web UI ships continuously. A simple monotonic promotion number is honest: "this is the 44th time we deployed to prod." The git-describe build string + SHA provide full point-in-time traceability without inventing a separate web semver.

**Current version mechanism (verified 2026-06-07):**

The single shared version source is `src/main/java/org/edumips64/utils/MetaInfo.java`, which reads JAR manifest attributes at class-load time. The manifest is populated by a `sharedManifest` action in `build.gradle.kts`:

| Manifest attribute | Current value | Notes |
|-------------------|--------------|-------|
| `Signature-Version` | `gradle.properties version=` (e.g. `1.4.1`) | Static release label |
| `Codename` | `gradle.properties codename=` | Human name (e.g. "WalkOfLife") |
| `Build-Date` | Build timestamp | — |
| `Full-Buildstring` | `${gitBranch}@${gitCommitHash}` | e.g. `master@abc1234` |
| `Git-Revision` | Short commit SHA | `git rev-parse --verify --short HEAD` |
| `Build-Qualifier` | `"alpha"` under GitHub Actions (`GITHUB_ACTIONS` env), else `""` | CI vs local/release distinction |

`MetaInfo` augments the version: if `Build-Qualifier` is non-empty, `VERSION` = `version + "-" + qualifier + "-" + sha` (e.g. `1.4.1-alpha-abc1234` on CI). Release builds show just `1.4.1`.

- **Swing consumers:** `Main.java` (window title, crash `ReportDialog`), `StatusBar.java` (welcome message), `CPUSwingWorker.java` (crash dialog). All read `MetaInfo.VERSION`, `MetaInfo.FULL_BUILDSTRING`, `MetaInfo.BUILD_DATE`, `MetaInfo.CODENAME` — no direct manifest access.
- **CLI consumer:** `src/main/java/org/edumips64/utils/cli/Version.java` — reads the same `MetaInfo` constants for `--version` output.
- **Web (GWT) consumer:** None today — `org.edumips64.client` has no `MetaInfo` equivalent; GWT-compiled JS cannot read a JAR manifest at runtime.

**Implementation note (follow-up task):** Adopting git-describe splits into two paths of very different complexity:

**Desktop + CLI — easy path (one Gradle change).** Replace the `sharedManifest` logic in `build.gradle.kts` so the version/build identity comes from `git describe --match 'v*'` instead of the static `Signature-Version` + `Build-Qualifier` mechanism. All downstream Swing and CLI consumers pick up the new string automatically through the existing `MetaInfo` plumbing — no UI code changes needed. This also removes today's inconsistency (`1.4.1-alpha-<sha>` in CI vs `1.4.1` at release): under git-describe, non-release builds show e.g. `1.4.0-2-gabc1234` and a tagged release shows exactly `1.4.1`. The `Build-Qualifier` hack and `MetaInfo`'s qualifier-concatenation logic become redundant and should be removed. Minor cleanup: `MetaInfo` currently calls `attributes.getValue("Build-Qualifier").isEmpty()`, which would NPE if that attribute were ever absent — tidy this in the same change.

**Web (GWT) — new plumbing required.** GWT-compiled JavaScript cannot read a JAR manifest at runtime, and `org.edumips64.client` has no `MetaInfo` equivalent today. The web UI needs a genuinely new injected build-time constant — e.g. a generated Java/GWT constant or a webpack `DefinePlugin` entry — carrying the same git-describe string.

**CI requirement (both paths):** Workflows using `fetch-depth: 1` in `actions/checkout` must switch to **`fetch-depth: 0`** so that `git describe` has full tag history; without this it fails or returns only the SHA.

### Open Questions (Part 3) — RESOLVED

*Build identity (decision (c) — git-describe) was already resolved. Remaining questions now also resolved:*

1. **`git describe` format flags:** Use `--match 'v*'` (only version tags) to exclude non-release tags from the describe walk. *(Unchanged recommendation)*
2. **Shallow checkout / dirty tree:** Fallback to `<sha>-dirty` or `UNKNOWN-<sha>` when `git describe` fails. CI workflows must use `fetch-depth: 0`. *(Unchanged recommendation)*
3. ~~**`package.json` version:**~~ **Leave stale at `1.0.0`; add a comment noting it is unused for versioning.** *(Resolved 2026-06-07)*
4. ~~**Build identity in all surfaces:**~~ **Yes — git-describe everywhere (desktop, CLI, web).** Implementation: desktop+CLI via `sharedManifest` change; web via webpack `DefinePlugin` or GWT constant. *(Resolved 2026-06-07)*

---

## Recommended End-to-End Design

Tying the three parts together into one coherent system:

### Promotion flow

```
Developer/Agent merges PR to master
         │
         ▼
┌─────────────────────────────┐
│  CI Build (ci.yml)          │
│  - Builds out/web/          │
│  - Uploads `web` artifact   │
│  - Runs tests               │
└─────────────┬───────────────┘
              │ (artifact available for 90 days)
              │
              │  Andrea manually triggers:
              ▼
┌─────────────────────────────────────────┐
│  promote-web.yml (workflow_dispatch)     │
│  Inputs: run_id (or SHA)                │
│  Promoter: Andrea (lupino3) only        │
│                                         │
│  1. Download `web` artifact from run    │
│  2. Read current manifest.json from     │
│     EduMIPS64/web.edumips.org repo      │
│  3. Compute next_version = current + 1  │
│  4. In Pages repo:                      │
│     - Copy root files → /prev/          │
│     - Copy new build → root + /v/<N+1>/ │
│     - Write manifest.json               │
│     - Prune /v/ entries > 50 versions   │
│  5. Push to Pages repo                  │
└─────────────────────────────────────────┘
         │
         ▼
    web.edumips.org updated
    /prev/ = previous version
    /v/<N+1>/ = new version
    manifest.json = metadata
```

### Rollback

- **Normal rollback:** Re-run `promote-web.yml` with the run_id of the previous good build. The system treats it as a new promotion (N+2), current becomes the old version, prev becomes what just failed.
- **Emergency rollback (no CI needed):** A second workflow `rollback-web.yml` (workflow_dispatch, no inputs) that swaps root ↔ /prev/ in the Pages repo. Instant, no artifact download needed.

### What changes in existing workflows

| File | Change |
|------|--------|
| `.github/workflows/release.yml` | Disable `deploy-prod` job (`if: false`). Keep desktop release logic. *(Phase 0 — GO)* |
| `.github/workflows/ci.yml` | No change (already builds + uploads `web` artifact on master pushes). Add `fetch-depth: 0` in Phase 3 for git-describe. |
| NEW: `.github/workflows/promote-web.yml` | Workflow_dispatch. Downloads artifact, deploys to Pages repo with versioned layout. Promoter: Andrea only. |
| NEW: `.github/workflows/rollback-web.yml` | Workflow_dispatch. Swaps root ↔ /prev/ for emergency rollback. |
| NEW: `.github/workflows/nightly-web.yml` *(Phase 3.5)* | Triggers on green master push. Deploys to /nightly/. |

### What stays the same

- PR preview system (Azure Blob) — unchanged.
- Desktop release process — unchanged.
- `gradle.properties` version — unchanged (remains the release/target label; build identity now comes from `git describe --tags`).
- CI checks on PRs — unchanged.

---

## Phased Rollout

| Phase | Scope | Effort | Outcome |
|-------|-------|--------|---------|
| **Phase 0** ✅ GO | Disable auto-deploy: comment out / `if: false` the `deploy-prod` job in `release.yml`. Production stays on last deployed version. | 5 min | Immediate safety — agents can merge without shipping to prod. |
| **Phase 1** | Create `promote-web.yml` with simple deployment (no /prev, no /v/N yet). Manual trigger deploys artifact to Pages repo root. Promoter: Andrea only. | 1-2 hours | Manual promotion working. |
| **Phase 2** | Add versioned layout: /prev/, /v/N/, manifest.json. Update promote workflow to manage directories. Retain **50 versions** in /v/. | 2-3 hours | Full rollback + version history. |
| **Phase 3** | Add `rollback-web.yml` for emergency swap. Add version display: (a) desktop+CLI — one `build.gradle.kts` `sharedManifest` change sets git-describe as the build identity, flowing automatically through `MetaInfo` to Swing title/status-bar/ReportDialog and CLI `--version`, removing the `Build-Qualifier` hack; (b) web UI — separate injected build-time constant (webpack `DefinePlugin` or generated GWT/Java constant) since GWT JS can't read the JAR manifest. Both paths require `fetch-depth: 0` in CI. | 2-3 hours | Complete system. |
| **Phase 3.5** (optional) | Add nightly channel: `nightly-web.yml` workflow deploys latest green master to `/nightly/`. Add visible "NIGHTLY" banner in UI. See Part 4. | 1-2 hours | Continuous preview for agents/contributors. |
| **Phase 4** (optional) | Migrate prod hosting to Azure Blob + CDN for cleaner architecture. | 4-8 hours | Eliminates file duplication, enables true pointer-based routing. |

**Phase 0 should be done immediately** — it's the "stop the bleeding" step that makes agents-on-master safe. *(Confirmed GO 2026-06-07 — no pending releases.)*

---

## Resolved Decisions (2026-06-07)

All open questions answered by Andrea. Each decision is locked in and propagated into the relevant sections above.

| # | Question | Decision | Impact |
|---|----------|----------|--------|
| 1 | **Who can promote?** | Andrea (lupino3) only for now. `workflow_dispatch` admin-only is sufficient access control. | Part 1: promoter = Andrea only; no environment approval gate needed. |
| 2 | **Phase 0 now?** | Yes — no pending releases. Safe to disable `deploy-prod` auto-deploy in `release.yml` immediately. | Phase 0 confirmed GO. |
| 3 | **Artifact source / retention?** | Accept 90-day limit; rebuild from SHA if artifact expired — we can always rebuild. No durability backstop needed for now. | Part 1: no GitHub Release asset / Azure Blob backstop required. |
| 4 | **GitHub Pages vs Azure for prod?** | Keep GitHub Pages for now. Azure remains an optional Phase 4 evolution. | Part 2: directory-based versioning on Pages (file duplication accepted). |
| 5 | **Versions to retain in `/v/`?** | **50** (not 10). | Part 2: prune `/v/` entries older than 50 versions on each promotion. |
| 6 | **Build identity in all surfaces?** | Yes — git-describe build identity everywhere (desktop, CLI, web). | Part 3 / Phase 3: implement build identity injection across all surfaces. |
| 7 | **`package.json` version?** | Leave stale at `1.0.0`; add a comment noting it is unused for versioning. | Part 3: no change to npm version value; add clarifying comment. |
| 8 | **Workflow naming?** | `promote-web.yml`. | Part 1 / Phase 1: workflow file is `promote-web.yml`. |

**Note:** Original question #9 ("auto-promote on green CI + time delay") is superseded by the **Nightly Channel** design in Part 4 below.

---

## Part 4 — Nightly Channel (debate)

**Question from Andrea (2026-06-07):** "In addition to the manual promotion, should we have a `nightly` version that is auto-deployed every day?"

### The Proposal

A separate **nightly channel** auto-deployed from latest green master, living at a distinct URL (e.g. `web.edumips.org/nightly/`), completely separate from the gated production at the root.

| Channel | URL | Trigger | Audience |
|---------|-----|---------|----------|
| **Production (gated)** | `web.edumips.org/` | Manual `promote-web.yml` by Andrea | End users, stable |
| **Nightly (ungated)** | `web.edumips.org/nightly/` | Automated (daily or on-green-master) | Contributors, agents, preview |

### Why It's Attractive

1. **Continuously-deployed target for agents/contributors.** Agents and devs can see their merged work live without waiting for Andrea to manually promote. Shortens feedback loop.
2. **Reduces pressure on manual promotion.** Andrea doesn't need to promote often — nightly handles the "latest master" use case. Promotion becomes a deliberate "ship to users" act.
3. **Early integration issue detection.** If something breaks visibly, nightly users notice before it reaches prod.
4. **Preview for Andrea.** Before promoting to prod, Andrea can test nightly as a staging environment.

### How It Coexists With Gated Promotion

The key architectural insight: **nightly and prod are independent channels** that never interfere.

```
┌────────────────────────────────────────────────────────────────────┐
│                         master branch                               │
│  (agents + devs merge freely; CI builds artifact on every push)    │
└───────────────────────────┬────────────────────────────────────────┘
                            │
        ┌───────────────────┴───────────────────┐
        │                                       │
        ▼                                       ▼
┌───────────────────┐               ┌───────────────────────┐
│  NIGHTLY (fast)   │               │  PROD (slow, gated)   │
│  web.edumips.org  │               │  web.edumips.org/     │
│     /nightly/     │               │       (root)          │
│                   │               │                       │
│ Auto-deploy daily │               │ Manual promote-web    │
│ or on-green-push  │               │ by Andrea only        │
│                   │               │                       │
│ Overwrites daily  │               │ Immutable /v/N/       │
│ No version history│               │ /prev/ rollback       │
└───────────────────┘               └───────────────────────┘
```

- **Nightly never touches prod.** It writes only to `/nightly/` — a single overwritten directory.
- **Prod promotion copies immutable artifacts** to `/`, `/prev/`, `/v/N/` — nightly doesn't affect this.
- **No shared mutable state.** They can coexist safely in the same Pages repo.

### Mechanics

**Trigger options:**

| Option | How | Pros | Cons |
|--------|-----|------|------|
| **(A) Daily cron** | Scheduled workflow (e.g. `0 3 * * *` = 3 AM UTC daily) | Predictable, simple. Low workflow runs. | May deploy stale build if no commits. |
| **(B) On-every-green-master** | Trigger on `push` to master after CI passes | Always fresh. No stale nightly. | More workflow runs (every merge triggers deploy). |

**Recommendation: Option (B) — deploy on every green master push.** Freshness matters more than minimizing workflow runs. Each merge to master already runs CI; adding a deploy step is marginal cost. Nightly is always the exact latest master.

**Guard against no-change deploys:** The workflow can check `git diff HEAD~1 -- src/` or compare artifact checksums; skip deploy if nothing changed. But for nightly, over-deploying is harmless — the concern is minor.

**Workflow: `nightly-web.yml`**

```yaml
name: Nightly Web Deploy
on:
  workflow_run:
    workflows: ["CI"]      # Trigger after CI workflow completes
    types: [completed]
    branches: [master]

jobs:
  deploy-nightly:
    if: ${{ github.event.workflow_run.conclusion == 'success' }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0   # For git describe
      - name: Download web artifact
        uses: actions/download-artifact@v4
        with:
          name: web
          run-id: ${{ github.event.workflow_run.id }}
          github-token: ${{ secrets.GITHUB_TOKEN }}
      - name: Deploy to /nightly/
        # Checkout Pages repo, copy artifact to /nightly/, push
        # ...
```

### Nightly Build Identity

Nightly uses the same `git describe --tags` build identity as everything else:
- Example: `1.4.0-15-g789abcd` — clearly not a clean release tag, obviously an interim build.
- **No promotion number.** Promotion numbers are reserved for gated prod promotions. Nightly is identified by its git-describe string + SHA only.
- The nightly directory could include a small `nightly-meta.json`:
  ```json
  {
    "build": "1.4.0-15-g789abcd",
    "sha": "789abcd",
    "deployedAt": "2026-06-07T03:00:00Z"
  }
  ```

### UI Indication

Users on nightly should **clearly see they're not on prod**. Options:

1. **Badge/banner:** "⚠️ NIGHTLY BUILD — may be unstable" in a visible header.
2. **Footer text:** "Nightly build 1.4.0-15-g789abcd — not for production use."
3. **Different favicon or color accent** (optional).

Recommendation: A simple banner at the top of the page. Minimal code change — the nightly deploy step can inject a `NIGHTLY=true` build-time constant.

### GitHub Pages Directory Layout With Nightly

```
EduMIPS64/web.edumips.org repo:
├── index.html              ← current prod (physical copy)
├── [all current files]     ← current prod assets
├── prev/                   ← previous prod
├── v/
│   ├── 42/                 ← prod version 42
│   ├── 43/                 ← prod version 43 (= prev)
│   └── 44/                 ← prod version 44 (= current)
├── nightly/                ← auto-deployed, overwritten daily
│   ├── index.html
│   ├── [nightly assets]
│   └── nightly-meta.json
└── manifest.json           ← prod manifest (unchanged)
```

All channels coexist cleanly. `/nightly/` is just another directory that gets overwritten on each deploy.

### Trade-offs

| Pro | Con |
|-----|-----|
| Agents/devs get immediate feedback | Extra workflow + directory to manage |
| Reduces manual promotion burden | Users may confuse nightly with prod |
| Early bug detection | Nightly may be broken (that's the point, but visible) |
| Staging environment for Andrea | Marginal hosting cost (trivial for static assets) |

**Mitigation for confusion:** The nightly banner + distinct URL make it clear. Power users who want stable should use the root URL; adventurous users can opt into nightly.

### Phasing

Nightly is **optional and should come after gated prod promotion is solid**.

| Phase | Content |
|-------|---------|
| Phases 0-3 | Gated prod promotion + rollback + build identity (current design) |
| **Phase 3.5 (optional)** | Add `nightly-web.yml` workflow + `/nightly/` directory + UI badge |
| Phase 4 | Azure migration (if desired) |

Nightly can be added any time after Phase 1 is working. It's additive and low-risk.

### Recommendation

**Yes — add a nightly channel.** The design is clean:
- Deploy on every green master push (freshest, no stale nightly).
- Lives at `/nightly/` — separate from gated prod.
- Uses git-describe build identity (no promotion number).
- Clear UI badge so users know they're on nightly.
- Implement as Phase 3.5 after gated promotion is working.

This gives agents and contributors a continuously-deployed preview without compromising the gated, stable production channel Andrea controls.

---

## Implementation Sequencing (PR breakdown)

**Analysis date:** 2026-06-07  
**Question:** Can everything ship in a single PR?

### Answer: No — but close

The design spans **two GitHub repositories**:
1. **`EduMIPS64/edumips64`** (this repo): workflows, `build.gradle.kts` versioning, web UI badge, `fetch-depth: 0` updates.
2. **`EduMIPS64/web.edumips.org`** (separate Pages repo): directory layout (`/`, `/prev/`, `/v/N/`, `/nightly/`), `manifest.json`.

A GitHub PR cannot span two repositories. However, the Pages-repo structure is *created automatically* by the first run of `promote-web.yml` — no manual PR needed there.

### Minimal safe grouping: **1 PR + 1 manual action**

| Step | Type | Scope |
|------|------|-------|
| **PR-A** | Pull Request | All changes in `EduMIPS64/edumips64` |
| **First Promotion** | Manual workflow run | Creates structure in `web.edumips.org` |

**PR-A contains (in one atomic PR):**
- Phase 0: Disable `deploy-prod` in `release.yml` (`if: false`)
- Phase 1–2: Add `promote-web.yml` with versioned layout logic (`/v/N/`, `/prev/`, `manifest.json`)
- Phase 3: Add `rollback-web.yml`, git-describe versioning (`build.gradle.kts` + webpack `DefinePlugin`), `fetch-depth: 0` across workflows
- Phase 3.5 (optional): Add `nightly-web.yml` + nightly banner UI component
- Tests: Unit tests for git-describe output parsing

**After PR-A merges:**
- Andrea runs `promote-web.yml` manually → creates `/v/1/`, `manifest.json` in Pages repo
- Andrea runs `nightly-web.yml` (if included) → creates `/nightly/` directory

### Ordering safety

No race condition: disabling `deploy-prod` and adding `promote-web.yml` in the same PR is safe because:
1. `deploy-prod` triggers on push to master — disabling it stops the *next* push from auto-deploying.
2. `promote-web.yml` is `workflow_dispatch` — Andrea controls when to run it.
3. The `web` artifact from CI already exists before the PR merges; promotion just downloads it.

The only risk is that *after* PR-A merges but *before* Andrea runs `promote-web.yml`, nothing can deploy to prod. This is intentional (the "gated" behavior).

### What's testable in-PR vs only on first run

| Aspect | Testable in PR |
|--------|---------------|
| `git describe` output in JAR manifest | ✅ Unit test |
| Webpack `DefinePlugin` injection | ✅ Build + inspect bundle |
| `fetch-depth: 0` in workflows | ✅ CI run itself |
| `promote-web.yml` logic | ⚠️ Syntax check only; actual deploy = first run |
| Pages repo structure | ❌ Only created on first promotion |

### Nightly: include or defer?

**Recommendation: include in PR-A.** It's additive (new workflow file + UI component), shares the same `fetch-depth: 0` requirement, and doesn't interfere with gated prod. Deferring to PR-B adds overhead with no safety benefit.

### Summary

**Single PR: technically no (cross-repo boundary), effectively yes.**

One PR handles all `edumips64` repo changes. The Pages-repo layout is created automatically on first `promote-web.yml` run — no second PR needed. Nightly can be included.
