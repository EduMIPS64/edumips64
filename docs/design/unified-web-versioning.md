# Design: Unified Web UI Versioning (commit-addressed builds)

Status: **Accepted** — being implemented (supersedes the dual-index model in
`web-promotion-and-versioning.md`).

Owner: @lupino3

## Resolved decisions

The four open questions raised during review are resolved as follows:

1. **Default promote target:** `promote-web.yml` with no `sha` input promotes
   the **newest candidate** in `versions.json` (the highest `seq`).
2. **Old-URL redirects:** migration emits `/v/<n>/` → `/c/<sha>/` redirect stubs
   (kept for one release cycle) so existing links do not break.
3. **Candidate volume in About:** show **all** pending candidates (they are
   expected to be few between promotions); no cap.
4. **`maxPromoted` cap (updated 2026-07-02):** the site reached 0.31 GB of
   GitHub Pages' 1 GB limit because promoted snapshots under `c/` were never
   pruned.  The retention policy is now `KEEP_PROMOTED = 10` — see
   [Promoted-snapshot retention](#promoted-snapshot-retention-task-a) below.
5. **Deploy verification (added 2026-07-02):** both a promotion and a rollback
   "succeeded" (workflow green) while the GitHub Pages legacy build errored
   instantly (0 ms, transient queue glitch), silently leaving production stale.
   A post-push verification step was added to both workflows — see
   [Deploy verification](#deploy-verification-task-b) below.

---

## Problem statement

The current web deployment to `EduMIPS64/web.edumips.org` has **two parallel
versioning mechanisms** with two index files and two directory layouts:

| Concern | Index file | Directory layout | Reached from UI via |
|---------|-----------|------------------|---------------------|
| Promoted production history (past) | `manifest.json` | `/v/<n>/` (promotion number) | "Previous versions" list |
| Per-commit candidate builds (future) | `candidates.json` | `/<YYYY-MM-DD>/<n>-<shortsha>/` | "Candidate builds" list |

Plus `prev/` (rollback target) as a third copy of a production build.

This is redundant and confusing:

- Two JSON schemas, two fetchers, two list builders, two UI sections.
- Two different identifiers for "a build": a promotion number `n` (production)
  vs a date+counter `YYYY-MM-DD #n` (candidate) — for what is fundamentally the
  same thing: *a web build of a specific master commit*.
- Promotion **rebuilds** the web artifact from master HEAD
  (`promote-web.yml` has a `build` job), even though the exact commit was
  already built and pushed as a candidate. Redundant work and a subtle risk
  that the promoted bytes differ from the candidate the maintainer reviewed.

## Goals

1. **One** index file and **one** directory layout for every web build.
2. Every master build is pushed to a directory named by its **commit ID**.
3. Both *future* builds (candidates awaiting promotion) and *past* builds
   (already promoted) are reachable from the web UI.
4. Manually **promoted** versions are visually **more prominent** in the About
   tab than ordinary candidates.
5. Retention rule:
   - **Past** (older than the live version): keep **only promoted** versions.
   - **Future** (newer than the live version): keep **all** candidates, in the
     web root, until a promotion happens.
6. **Promotion never rebuilds.** It is a pure metadata + file-copy + cleanup
   operation against builds that were already pushed.
7. The whole scheme is **well documented** (design doc + user docs ×3 langs +
   developer guide).

## Non-goals

- Changing the PR-preview system (Azure Blob) — unchanged.
- Changing the desktop/CLI release process or `gradle.properties version=`.
- Changing the build-identity string (`git describe`) semantics.

---

## Core model: a single linear timeline of commit-addressed builds

Master is a protected, linear, fast-forward-only branch (no force-push). Every
successful master CI build produces a web artifact for a specific commit. We
model the deployment as **one ordered list of builds**, each identified by its
commit SHA, with a single boolean: was it manually promoted or not.

### The invariant (the heart of the design)

Let `current` be the SHA of the live production version (served at `/`). Each
build has a monotonic `seq` (commit number, see below). The Pages repo keeps a
build **iff**:

```
keep(V)  ⇔  V.promoted == true   OR   V.seq > current.seq
```

In words: **keep every promoted version, plus every candidate newer than the
live one.** Everything else (a never-promoted candidate that is now in the past)
is pruned. Every deploy operation re-establishes this invariant; promotion and
rollback both fall out of it naturally (see Operations).

### `seq` — the commit number

`seq = git rev-list --count <sha>` evaluated on master.

- Strictly monotonic along master's linear history; unique per commit.
- Robust to **out-of-order CI completion** (ordering follows commit ancestry,
  not push wall-clock).
- Doubles as a friendly, human-readable build ordinal (SVN-style revision).
- Requires `fetch-depth: 0` in the workflow that computes it (already the norm
  for the workflows that need `git describe`).

> **Assumption (documented constraint):** master is never force-pushed or
> rebased. `branch protection` already enforces this. If history were rewritten,
> `seq` values could shift; this is explicitly unsupported.

---

## Directory layout (new)

```
EduMIPS64/web.edumips.org/
├── index.html, ui.js, style.css, ...   ← physical copy of the CURRENT promoted build
├── c/                                   ← every retained build, addressed by commit SHA
│   ├── 5f3ae1a…/                        ← a build (candidate or promoted)
│   ├── 99b56ff…/
│   └── a08b8d5…/
├── versions.json                        ← the ONE index file
├── CNAME                                ← preserved
└── .nojekyll                            ← preserved
```

Gone: `manifest.json`, `candidates.json`, `prev/`, `v/<n>/`, `/<YYYY-MM-DD>/…`.

- Builds live at **`/c/<full-sha>/`**. Full SHA (not short) → unambiguous,
  collision-free, immutable once written.
- The root `/` remains a **physical copy** of the current promoted build (the
  production site served at `web.edumips.org`). This preserves deep links,
  caching, and the URL-based build detection; only **promotion** rewrites root.
- `c/`, `versions.json`, `CNAME`, `.nojekyll` are **reserved** root names
  (never deleted by a root replace).

### `versions.json` schema (the single index)

```json
{
  "current": "a08b8d56ebc959216ea1d576dc465fab0a5cfc22",
  "versions": [
    {
      "sha": "a08b8d56ebc959216ea1d576dc465fab0a5cfc22",
      "shortsha": "a08b8d5",
      "seq": 1185,
      "build": "1.4.0-116-ga08b8d56",
      "targetRelease": "1.4.1",
      "pushedAt": "2026-06-14T06:00:00Z",
      "promoted": true,
      "promotedAt": "2026-06-14T06:49:01Z",
      "promotedBy": "lupino3"
    },
    {
      "sha": "99b56ff4218fdf4c3c6a1e172339ecbea9defcc4",
      "shortsha": "99b56ff",
      "seq": 1184,
      "build": "1.4.0-114-g99b56ff4",
      "targetRelease": "1.4.1",
      "pushedAt": "2026-06-13T22:00:00Z",
      "promoted": false
    }
  ]
}
```

- `versions` is sorted **newest-first by `seq`**.
- `promoted: false` candidates omit the `promoted*` audit fields.
- `current` is the SHA served at `/`. `current`'s entry always has
  `promoted: true`.

---

## Operations (deploy-web-pages.py rewrite)

Three subcommands replace today's `promote` / `candidate` / `rollback`.

### `push <artifact_dir> <sha> <seq> <build> <targetRelease>`

Runs on **every** master commit — it is a `ci.yml` job (`publish-web-candidate`)
that depends only on `build-web`, so it publishes as soon as the web build
finishes (in parallel with the slow desktop/snap/electron jobs), plus a nightly
schedule and a manual `workflow_dispatch` fallback. The privileged logic lives
in the reusable `push-web.yml` workflow.

1. If `/c/<sha>/` already exists (CI re-run) → idempotent no-op for the files.
2. Copy the artifact into `/c/<sha>/` (immutable snapshot).
3. Upsert a `versions.json` entry with `promoted: false`, `pushedAt`, `seq`,
   `build`, `targetRelease`.
4. **Does not touch root.** Production stays on the last promoted build.
5. **No pruning.** Candidates accumulate until a promotion prunes the skipped
   ones.

### `promote <sha> <actor>`  — **never builds**

Runs manually (gated to the maintainer). `<sha>` is a build that was **already
pushed** as a candidate.

1. **Verify** `/c/<sha>/` exists and has a `versions.json` entry. If not →
   error: *"refusing to promote a version that was never pushed."* This is the
   guardrail that makes "promotion never builds" safe.
2. Mark the entry `promoted: true`, set `promotedAt`, `promotedBy`.
3. Set `current = <sha>`.
4. **Copy `/c/<sha>/` → root** (clean replace: delete current root prod files,
   keep reserved names, copy the snapshot in). No webpack run, no artifact
   download.
5. **Prune to invariant:** delete every version with `promoted == false` AND
   `seq < current.seq`, removing both its `versions.json` entry and its
   `/c/<sha>/` directory. (These are exactly the candidates "between" the
   previous promotion and `X` — see below.)

Promoting the already-current SHA is a no-op refresh. Promoting an **older
promoted** SHA performs a rollback (current moves back; nothing is pruned
because lowering `current` only *grows* the kept set) — so rollback is just a
special case of promote.

#### Why prune = "remove all versions between current and X"

Before promoting `X`, by the invariant every version with `seq < current.seq`
is already promoted, and every version with `seq > current.seq` is an unpruned
candidate. Promoting `X` (with `seq_X > seq_current`) makes the half-open range
`(seq_current, seq_X)` become "past." Those are all non-promoted candidates, so
the invariant deletes exactly them — i.e. **all versions strictly between the
old live version and X**, which is the requested behaviour. `X` itself and the
old live version stay (both promoted); candidates newer than `X` stay (still
future).

### `rollback <actor>`  (thin convenience)

Find the newest promoted version with `seq < current.seq`; set `current` to it
and copy its `/c/<sha>/` into root. No pruning (invariant preserved). Equivalent
to `promote <that-sha>`; kept as a one-click "undo the last promotion."

---

## Worked example

Live = `P` (promoted, seq 100). New master commits push candidates
`c101, c102, c103, c104` (seqs 101–104), all kept in `/c/…/`, root still serves
`P`.

Maintainer reviews `c103` and promotes it:

- `current = c103`; `c103.promoted = true`; root ← copy of `/c/c103/`.
- Prune non-promoted with `seq < 103` → delete `c101`, `c102`.
- Kept: `P` (past, promoted), `c103` (live, promoted), `c104` (future
  candidate). Removed: `c101`, `c102`.

About tab now shows `P` and `c103` as **promoted** (prominent) and `c104` as a
**candidate**. Later promoting `c104` prunes nothing extra (no candidates
between `c103` and `c104`).

---

## URL scheme

| URL | Content |
|-----|---------|
| `https://web.edumips.org/` | Current promoted build (stable) |
| `https://web.edumips.org/c/<sha>/` | Any retained build (candidate or promoted), immutable |
| `https://web.edumips.org/versions.json` | The single machine-readable index |

Removed: `/v/<n>/`, `/prev/`, `/candidates.json`, `/manifest.json`,
`/<YYYY-MM-DD>/<n>-<sha>/`.

---

## UI changes

All version metadata now comes from **one** `versions.json` fetch.

- **`buildInfo.js`** — replace the candidate path regex with `/c/<sha>/`
  detection. `getBuildInfo` stays URL-only and returns
  `kind: 'production' | 'archive-build' | 'pr' | 'dev'` where `archive-build`
  means "served from `/c/<sha>/`". Whether that build is a *promoted* archive or
  an unpromoted *candidate* is resolved against `versions.json` (async), not
  from the URL.
- **`versionHistory.js`** — collapse `fetchManifest` + `fetchCandidates` into a
  single `fetchVersions()` returning the unified object; one validator; one list
  builder that partitions into **Promoted** and **Candidates** by the
  `promoted` flag and `seq` relative to `current`.
- **`HelpDialog.js`** — one fetch; render a single "Versions" section with
  promoted entries shown prominently (e.g. bold + date + "current" chip) and
  candidates listed below in a lighter style. **All** pending candidates are
  shown (no cap — they are few between promotions). Keep the "you are viewing an
  older/candidate build → open latest" banner, driven by `current` vs the
  viewed SHA.
- **`Header.js`** — badge logic: at `/c/<sha>/`, show `CANDIDATE` (purple) when
  the build is not promoted, or a subtler "older version" indicator when it is a
  promoted archive; no badge at root (production). The promoted/candidate
  distinction is resolved from `versions.json`.

---

## Workflows

| File | Today | After |
|------|-------|-------|
| `candidate-web.yml` → **`push-web.yml`** | deploys candidate to date dir, updates `candidates.json` | computes `seq = git rev-list --count <sha>`, runs `push` → `/c/<sha>/`, updates `versions.json`. Now a **reusable workflow** (`workflow_call`) invoked by `ci.yml`'s `publish-web-candidate` job on every master push/schedule (right after `build-web`), plus `workflow_dispatch`. |
| `promote-web.yml` | has a **`build` job** (fresh-mode) + downloads artifact + promotes | **`build` job removed**. Input: `sha` (full or short; default = **newest candidate** in `versions.json`). Verifies `/c/<sha>/` exists in Pages, runs `promote`. No artifact download, no webpack. Still gated to maintainer + master. |
| `rollback-web.yml` | swaps `root` ↔ `prev/` | runs `rollback` (newest promoted below current) or `promote <prev promoted sha>`. Gated. |

The `web-pages-deploy` concurrency lock is retained on every Pages-writing job
so promotions/pushes/rollbacks serialize.

---

## Migration (one-shot)

A `deploy-web-pages.py migrate` subcommand (with `--dry-run`), run once by the
maintainer against a full clone of both repos:

1. Read existing `manifest.json.history` (promoted) and `candidates.json`
   (candidates). Both already carry a `sha` per entry.
2. For each SHA compute `seq = git rev-list --count <sha>` from the edumips64
   clone.
3. Move `/v/<n>/` → `/c/<sha>/` (promoted) and `/<date>/<n>-<sha>/` → `/c/<sha>/`
   (candidate). De-duplicate if a SHA appears in both.
4. Synthesize `versions.json` (promoted flags from manifest; `current` =
   manifest `current` SHA).
5. Delete `manifest.json`, `candidates.json`, `prev/`, `v/`, and the date dirs.
   Replace each old `/v/<n>/` directory with a tiny redirect stub
   (`index.html` with a `<meta http-equiv="refresh">` + canonical link) that
   forwards to the corresponding `/c/<sha>/`. These stubs are retained for one
   release cycle so existing deep links keep working, then removed.
6. Commit. The live root copy is left untouched (it already equals the current
   promoted build).

Old candidate URLs (`/<date>/…`) hard-cut to 404 (they were only ever surfaced
through the in-app navigator). Promoted `/v/<n>/` URLs get redirect stubs as
above.

---

## Storage considerations

- **Promoted** versions: only the `KEEP_PROMOTED = 10` most-recent promoted
  snapshots (plus the current production version, always) are kept on disk.
  Older promoted entries remain in `versions.json` with `"pruned": true` so the
  audit trail and `seq` history survive, but their `c/<sha>/` directory is
  deleted to reclaim space.  This is the "promoted-snapshot retention" policy
  that replaced the earlier "keep all promoted forever" rule, motivated by the
  site reaching 0.31 GB of GitHub Pages' 1 GB limit (incident 2026-07-02).
- **Candidates** are bounded by promotion cadence: promote regularly → few
  pending candidates. The old 14-day candidate retention is replaced by
  "prune-on-promote," which is deterministic rather than time-based.
- Source maps (`*.map`) and the bundled `docs/` dominate per-build size. A
  future optimization (out of scope) is to exclude `*.map` from immutable
  snapshots while keeping them for the live root only.

---

## Promoted-snapshot retention

*Tracking issue: 2026-07-02 incident — 0.31 GB of 1 GB Pages limit consumed.*

### Policy

A promoted version's `c/<sha>/` snapshot is retained on disk iff:

1. It is the current production version (`index["current"]`), **OR**
2. It is among the `KEEP_PROMOTED = 10` most-recent promoted versions by `seq`,
   **OR**
3. Its `seq` > `current.seq` (it is a future candidate — unchanged rule).

Promoted versions that fail all three conditions have their `c/<sha>/` directory
deleted, but their `versions.json` entry is preserved with `"pruned": true`.
This keeps the full audit trail and `seq` ordering intact while reclaiming disk
space.

### `"pruned": true` field

A pruned entry carries this extra field:

```json
{
  "sha": "...",
  "seq": 1150,
  "promoted": true,
  "pruned": true,
  "promotedAt": "...",
  "promotedBy": "lupino3"
}
```

- `promote` and `rollback` refuse to target a pruned version (the snapshot is
  gone; `push` must be used to re-upload it first).
- `prune_to_invariant` clears the `pruned` flag if a version re-enters the
  retention window (e.g. after a rollback makes it current).

### Rollback safety

Rollback targets the newest promoted version older than `current` whose entry is
**not** pruned (i.e. whose snapshot exists on disk).  With `KEEP_PROMOTED = 10`
this is always in the retention window (it is the second-highest-seq promoted
version), so rollback will not encounter a pruned target in normal operation.
A pytest (`test_rollback_works_after_pruning`) verifies this invariant explicitly.

---

## Deploy verification

*Tracking issue: 2026-07-02 incident — both a promotion and a rollback workflow
reported success while the GitHub Pages legacy build errored instantly (0 ms,
transient queue glitch), silently leaving production stale until a build was
manually re-requested via `POST /repos/EduMIPS64/web.edumips.org/pages/builds`.*

### Implementation

`.github/scripts/verify-pages-deploy.sh <expected-build-string> <pages-repo-path>`

Both `promote-web.yml` and `rollback-web.yml` call this script after the
"Commit and push to Pages repo" step, provided the step set `pushed=true` (the
script is skipped when nothing was committed, e.g. idempotent re-run).

#### Phase 1 — Pages deploy workflow (up to 10 min)

Since 2026-07-03 the web.edumips.org repo deploys through GitHub Actions
(`.github/workflows/deploy-pages.yml` in that repo: checkout →
`upload-pages-artifact` → `deploy-pages`; the Pages source is
`build_type: workflow`).  This replaced the legacy branch-based Jekyll
build, whose shared queue produced transient instant-"errored" builds
(2026-07-02 incident) and whose duration had grown past 15 minutes.
Actions deploys complete in ~1–2 minutes with explicit logs.

Verification polls
`GET /repos/EduMIPS64/web.edumips.org/actions/workflows/deploy-pages.yml/runs?head_sha=<pages-repo HEAD>`
using the `PAT_WEBUI` secret, until the run for the pushed commit completes
with `conclusion == "success"`.  If no run appears within 3 minutes (or the
run fails), the script re-dispatches the workflow once via
`workflow_dispatch` — the modern equivalent of the old "re-request a Pages
build" recovery.

Additionally, `monitor-webui.yml` (every 10 minutes) compares the *intended*
build string (from the Pages repo's raw `versions.json`) against what
`web.edumips.org/ui.js` actually serves, and fails loudly on persistent
drift — the backstop for deploys that never happened at all.

#### Phase 2 — CDN propagation (up to 12 min)

Once the Pages build is confirmed, poll
`https://web.edumips.org/ui.js?cb=<random>` (cache-busting) until the response
body contains the `expected-build-string`.  GitHub Pages CDN `max-age` is 600 s,
so 12 minutes is generous.

- For **promote**: the expected string is the promoted version's `build` field
  from `versions.json` (e.g. `1.4.0-173-g5e4edfa1`).
- For **rollback**: the expected string is the new current entry's `build` field
  read from `versions.json` before the push step exits.

Both workflows capture `build=<string>` as a step output from the push step and
pass it to the verify script.

#### Failure mode

On timeout the script exits 1 with explicit instructions for manual recovery,
failing the workflow loudly rather than silently succeeding with stale content.

---

## Alternatives considered

- **Builds at `/<sha>/` (root) instead of `/c/<sha>/`.** Rejected: pollutes the
  root namespace and complicates reserved-name handling on every root replace.
  `/c/` cleanly namespaces immutable builds.
- **Root as a redirect to `/c/<sha>/` instead of a physical copy.** Rejected:
  breaks deep links/caching and the URL-based build detection, and changes the
  visible production URL. Physical copy keeps behaviour identical to today.
- **Keep `seq` as push-order counter instead of `git rev-list --count`.**
  Rejected: not robust to out-of-order CI; commit-number is also a nicer UX.
- **Time-based candidate retention (as today).** Rejected in favour of the
  deterministic prune-on-promote rule the user requested.

---

## Risks

- **buildInfo/Header now need an async fetch** to distinguish promoted-archive
  from candidate. Mitigation: default to the safe "candidate/older" badge until
  `versions.json` resolves; covered by Playwright specs.
- **Migration correctness.** Mitigation: `--dry-run`, run once, reviewed diff,
  and the live root copy is never touched.
- **Large rewrite touching script + 3 workflows + 4 JS modules + docs.**
  Mitigation: phased rollout below; the Python deploy script is fully unit-
  tested (today 16 tests), so the new model lands with new tests first.

---

## Phased rollout / implementation plan

Each phase is independently reviewable; the new model is dark until Phase 5.

1. **Design sign-off** — this document (this PR).
2. **`deploy-web-pages.py` rewrite + unit tests** — implement `push` /
   `promote` (no-build) / `rollback` / `migrate` against `versions.json` and the
   `keep(V)` invariant. Port/extend the existing pytest suite; add tests for the
   prune-between rule, rollback-via-promote, idempotent re-push, and the
   "promote-unpushed → error" guard.
3. **UI rewrite + Playwright specs** — `versionHistory.js`, `buildInfo.js`,
   `HelpDialog.js`, `Header.js`; merge `version-history.spec.js` +
   `candidate-builds.spec.js` + `version-and-candidate-badge.spec.js` into the
   unified model.
4. **Workflows** — rename `candidate-web.yml` → `push-web.yml`; strip the build
   job from `promote-web.yml` (sha input, default newest candidate); update
   `rollback-web.yml`. Keep the concurrency lock.
5. **Migration + cutover** — run `migrate --dry-run`, review, then migrate the
   live Pages repo; flip workflows; delete the old design doc's superseded
   sections.
6. **Docs** — update `docs/user/{en,it,zh}/src/versioning.rst` and
   `user-interface-web.rst`, `docs/developer-guide.md`, and mark
   `web-promotion-and-versioning.md` as superseded by this document.

---

## Open questions for the maintainer

All resolved — see **Resolved decisions** at the top of this document.
