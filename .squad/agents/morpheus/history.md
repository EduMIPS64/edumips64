# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

### 2026-06-05: Web production deployment infra

- **Prod deploy:** `release.yml` → `deploy-prod` job triggers on every push to master (+ workflow_dispatch). Uses `crazy-max/ghaction-github-pages@v5` to push `out/web` artifact to `EduMIPS64/web.edumips.org` repo (GitHub Pages). Auth: `PAT_WEBUI` secret. `keep_history: true` retains git history.
- **PR previews:** Azure Blob Storage account `edumips64ci`, container `$web`, path `/<PR-number>/`. Served at `https://edumips64ci.z16.web.core.windows.net/<PR>/`. Uses Azure OIDC login. GitHub Environments: `Staging` (approval-gated for forks) and `staging-dependabot` (auto for maintainers/bots).
- **Versioning:** Single unified version in `gradle.properties` (`version=1.4.1`, `codename="WalkOfLife"`). Covers core + Swing + Web + Electron. `package.json` version (`1.0.0`) is stale/unused. Git tags: `v1.4.1`.
- **Build output:** GWT worker.js + React/webpack → `out/web/`. Core and web UI always ship together.
- **Artifact retention:** GitHub Actions default 90 days.

### 2026-06-07: Versioning — release label vs build identity (decided)

- `gradle.properties version=` is a **release label**, not a build identity. Constant between releases. Verified: latest tag `v1.4.0`; master is many commits into `1.4.1` (untagged, no `v1.4.1` yet). Every interim commit reports the same label.
- **Decision (approved by Andrea, 2026-06-07):** Use `git describe --tags` as the build identity — e.g. `1.4.0-2-gabc1234`. Unique per commit, monotonic, auto-derived, no bookkeeping. At a tagged commit collapses to the clean label (e.g. `1.4.1`). Applies to all shipped artifacts (desktop JAR, Electron, web build).
- Web promotion `manifest.json` carries: `{ "current": N, "build": "1.4.0-2-gabc1234", "sha": "abc1234", "targetRelease": "1.4.1", ... }`.
- **Implementation (follow-up task):** Gradle task to inject git-describe output into GWT worker + React UI. CI workflows need `fetch-depth: 0` in `actions/checkout` — shallow clones break `git describe`. Desktop About box to display same string.

### 2026-06-07: Version-display implementation paths (verified)

Version-display impl (verified 2026-06-07): desktop+CLI get git-describe via one `build.gradle.kts` `sharedManifest` change flowing through `MetaInfo` (manifest) → Swing title (`Main.java`) / `StatusBar.java` / crash `ReportDialog` (`Main.java`, `CPUSwingWorker.java`) + CLI `Version.java`; removes the `alpha` `Build-Qualifier` hack and `MetaInfo`'s qualifier-concatenation logic (plus latent NPE on missing `Build-Qualifier` attribute). Web (GWT) needs a separate injected build-time constant (webpack `DefinePlugin` or generated GWT/Java constant) since GWT JS can't read the JAR manifest at runtime. Both paths require `fetch-depth: 0` in CI workflows.

### 2026-06-07: Web promotion design — decisions locked

All 8 open questions in `docs/design/web-promotion-and-versioning.md` resolved by Andrea:

| Decision | Value |
|----------|-------|
| Who can promote | Andrea (lupino3) only — workflow_dispatch admin-only sufficient |
| Phase 0 now | YES — no pending releases, disable `deploy-prod` immediately |
| Artifact retention | Accept 90-day limit; rebuild from SHA if expired |
| GitHub Pages vs Azure | Stay on GitHub Pages |
| Versions to retain | 50 in `/v/` |
| Build identity everywhere | Yes — git-describe in desktop, CLI, web |
| package.json version | Leave stale; add "unused" comment |
| Workflow name | `promote-web.yml` |

**Nightly channel recommendation (new Part 4):** YES — add optional `/nightly/` channel auto-deployed on every green master push. Separate from gated prod; uses git-describe identity (no promotion number); visible "NIGHTLY" banner; implement as Phase 3.5 after gated promotion works.

### 2026-06-07: Single-PR feasibility — verdict NO (cross-repo boundary)

**Question:** Can the entire web-promotion + rollback + versioning + nightly design ship in one PR?

**Answer: NO** — the design spans two independent GitHub repositories:
1. **`EduMIPS64/edumips64`** (this repo): workflow files, `build.gradle.kts` versioning changes, web UI badge code, `fetch-depth: 0` updates.
2. **`EduMIPS64/web.edumips.org`** (separate Pages repo): directory layout (`/prev/`, `/v/N/`, `/nightly/`), `manifest.json`.

A single GitHub PR cannot span two repositories. The Pages-repo directory structure must be bootstrapped by the *first run* of `promote-web.yml`, not by a PR.

**Recommended minimal PR breakdown (2 PRs):**
1. **PR-A (edumips64 repo):** All workflow changes + versioning + UI badge. Contains: disable `deploy-prod` (Phase 0), add `promote-web.yml` + `rollback-web.yml` + `nightly-web.yml`, `build.gradle.kts` git-describe, webpack `DefinePlugin`, `fetch-depth: 0` across workflows, nightly banner component. Self-contained and testable except for actual deployment.
2. **Manual first promotion:** After PR-A merges, Andrea runs `promote-web.yml` once. This *creates* the `/v/1/`, `/prev/`, `manifest.json` structure in the Pages repo automatically. No separate PR needed.

**Nightly:** Can be included in PR-A (same repo, additive workflow). Optionally defer to PR-B if Andrea wants to validate gated promotion first.

**Ordering/safety:** Disabling `deploy-prod` + adding `promote-web.yml` in the same PR is safe — there is no race because both are admin-only triggers and Andrea controls when to run the new workflow. The artifact already exists from CI.

**Testing:** Versioning changes are testable in-repo (unit tests for git-describe output). Workflows are only fully testable on first real run.
