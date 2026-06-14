# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Archive

Detailed learnings from 2026-06-05 through 2026-06-09 (build identity, web promotion, version navigator, contextual run controls) are archived in prior git history of this file.

## Recent Work Summary

- **2026-06-07:** Implemented robust docs/user/common_conf.py fallback chain for version resolution; added versioning.rst pages (EN/IT/ZH); updated developer-guide.md.

- **2026-06-08:** Documented unified promote-web.yml workflow with optional run_id; created version-navigator pages (en/versions.rst + IT/ZH).

- **2026-06-09:** Documented contextual run-controls feature in user UI pages (en/ui.rst + IT/ZH translations).

- **2026-06-13:** Candidate builds documentation session: updated docs/developer-guide.md (CI/CD count → 5, nightly → candidate section, versionHistory gating notes), design doc (architecture, versioning scheme, deployment flow), user docs (EN/IT/ZH — feature overview, deployment behavior, UI navigation, retention policy). Badge color fixes (candidate blue vs nightly orange). Documentation merged in PR #1845.
### Files changed
- `docs/user/common_conf.py` — robust fallback chain for `version`/`release`.
- `docs/user/en/src/index.rst` — "This manual was generated for EduMIPS64 build |version|."
- `docs/user/it/src/index.rst` — "Questo manuale è stato generato per la build |version| di EduMIPS64."
- `docs/user/zh/src/index.rst` — "本手册是针对 EduMIPS64 构建版本 |version| 生成的。"
- `docs/developer-guide.md` — Added "Versioning model" and "Web production promotion" sections; updated CI workflows list to reflect `deploy-prod` disabled, nightly workflow added.

### Where developer docs live
Developer guide: `docs/developer-guide.md`. New sections inserted before "Manual release checklist", with ToC entries at the top of the file. Versioning model explains the release-label/build-identity split; web promotion section covers `promote-web.yml`, `rollback-web.yml`, `nightly-web.yml`, and the Pages layout (root / prev / v/N / nightly).

### Pre-existing note
The `latex_preamble` in `common_conf.py` uses `\D` in a non-raw string (produces a `SyntaxWarning` in Python 3.12+). This is pre-existing and unrelated to this change — left untouched per minimal-change policy.


### New page: `versioning.rst`
Created `docs/user/{en,it,zh}/src/versioning.rst` and added `versioning` as
the fifth entry in the first (UI-independent) toctree block of each
`docs/user/{en,it,zh}/src/index.rst` (after `examples`).

### Content summary (for future reference)
The page covers three topics:
1. **Version string format** — plain release number (e.g. `1.4.1`) vs.
   between-release build (e.g. `1.4.0-74-geec1768`: release + number of
   changes + short unique identifier). No dev jargon.
2. **Where to find the version** — desktop: window title / Help→About;
   CLI: `--version` flag; web: About tab + toolbar "Web Version" label.
3. **Web build badges** — the key user-facing rule:
   - No badge → stable production (https://web.edumips.org).
   - `NIGHTLY` (orange) → rebuilt every night, newest features, may be unstable.
   - `PR #N` (yellow) → temporary preview for a pull request.
   - `dev` (blue) → local development build.

### Translation confidence
- IT: full Italian translation, high confidence — matches tone of existing IT pages.
- ZH: full Simplified Chinese translation using `unicodedata`-verified underlines;
  moderate confidence — technically accurate, may benefit from native-speaker review.

### CJK / full-width punctuation rST inline-markup rule
In reStructuredText, inline markup (`**bold**`, `` ``literal`` ``) must be bounded by whitespace or ASCII punctuation. CJK characters and full-width punctuation (e.g. `（`, `）`, `。`, `；`) do NOT satisfy this boundary rule, so adjacent markup is silently dropped (renders as literal `**`/backticks). Fix: insert `\ ` (backslash-space, produces no visible output) between the inline markup delimiter and the adjacent CJK/full-width character. For nested or spanning bold+literal, split into separate non-nested markup runs joined with `\ ` — e.g. `**text；**\ ``NIGHTLY``\ **→ more text。**`.

## Learnings — 2026-06-08: Unified build+promote workflow with optional run_id

### Design change: `promote-web.yml` now unified

The `promote-web.yml` workflow has been refactored to consolidate "manual build" + "manual promote" into ONE workflow with an optional `run_id` input:
- **No `run_id`** (empty): Build current master from scratch (via reusable `build-web.yml`, no secrets) and promote it. One-click "ship current master" mode.
- **`run_id` provided**: Skip build; validate and promote that specific CI run. Enables rollback, re-promote, and promoting an arbitrary validated run.

Both modes:
- Dispatch from `master` only.
- Gated to `lupino3` (Andrea).
- Promote job serialised via concurrency `web-pages-deploy` (cancel-in-progress false).
- Build job (fresh mode) is not serialised.

### Files updated

- **`docs/design/web-promotion-and-versioning.md`**:
  - Section "Manual Gated Promotion" (lines 43–65): Expanded to describe both modes, inputs, dispatch requirement, and concurrency.
  - Section "Alternatives Considered" → "Promotion Trigger" (lines 174–181): Added rows (D) "Hard build+promote, dropping run_id" and (E) "Separate build→promote orchestrator" with rationales for rejection.
  - Section "Resolved Decisions" (line 251): Updated row 8 (workflow naming) to clarify that `promote-web.yml` unifies both modes.

- **`docs/developer-guide.md`**:
  - Section "Web production promotion" (lines 446–489): Restructured to clearly present two modes (Mode 1: empty `run_id` for build+promote current master; Mode 2: provided `run_id` for rollback/re-promote/audit). Simplified the normal-case procedure (just leave `run_id` empty) while preserving documented rollback capability.

### Key design points preserved

- Security: `build-web.yml` has `contents: read` only; `PAT_WEBUI` credential lives only in promote job.
- Immutability: Build+promote of current master is immutable once dispatched; re-promoting with external `run_id` validates artifact exists before deploy.
- Audit trail: manifest.json records all deployments; promotion numbers are monotonic.


## Learnings — 2026-06-08: In-app version navigator documentation

### Version navigator feature (in-app browsing of previous versions)

Users can now browse and open previous released versions of the web UI from the Help → About tab. The navigator reads `/manifest.json` and displays a list of retained versions (up to 50) with dates, build identifiers, and promotion metadata. Clicking an older version opens it at `/v/<N>/` in a new tab. The feature is gated on a successfully-fetched valid manifest and a non-PR build (PR previews don't show the navigator).

Key implementation detail: **monotonic version numbering** fixes a latent bug where a promote after a rollback could re-use and mutate an existing immutable snapshot. The deploy script maintains a `next_version` counter to ensure `n` is always `max(ever used) + 1`.

### Files changed

- `docs/user/en/src/versioning.rst` — Added "Viewing previous versions" subsection explaining the feature in simple user terms.
- `docs/user/it/src/versioning.rst` — Italian translation.
- `docs/user/zh/src/versioning.rst` — Simplified Chinese translation.
- `docs/developer-guide.md` — Added "Manifest and version history" and "Monotonic version numbering" subsections under "Web production promotion" section, documenting the `manifest.json` `history` array schema, the About-tab navigator behavior, gating conditions, backfill logic, prune-in-lockstep behavior, and the monotonic-numbering fix.
- `docs/design/web-promotion-and-versioning.md` — Added new "In-app Previous-Version Navigator" decision section describing the implementation, rejected alternatives (per-snapshot manifest.json and UI-only blind enumeration), and the monotonic-numbering fix. Added row 11 to "Resolved Decisions" table.

### Translation notes

- **IT:** Full Italian translation, high confidence — technically accurate and matches tone of existing IT pages.
- **ZH:** Full Simplified Chinese translation; technically accurate but may benefit from native-speaker review for phrasing naturalness.

## Learnings — 2026-06-09: Web UI contextual run controls documentation

### Toolbar redesign: execution controls now appear contextually

The web UI toolbar has been redesigned to show run controls contextually based on simulator state, reducing visual clutter and making available actions immediately obvious. This follows the pattern of modern debugger toolbars (e.g. VSCode).

**Simulator states and visible controls:**
- **EMPTY** (no program loaded) — only Load is shown.
- **READY** (program loaded, ready to execute) — Load, Single Step, Multi Step, Run All, Stop are shown.
- **EXECUTING** (program running) — Pause and Stop (disabled) are shown.
- **ENDED** (program finished) — only Load is shown.
- **Waiting for input** (input dialog open) — execution controls hidden; input dialog must be resolved first.

Editor controls (Open Code, Save Code, Clear, Restore sample, Help) remain always visible but with disable states when appropriate.

### Files changed

- `docs/user/en/src/user-interface-web.rst` — replaced lines 48-100 with comprehensive contextual behavior section (states, individual button descriptions, editor controls).
- `docs/user/it/src/user-interface-web.rst` — Italian translation of contextual behavior.
- `docs/user/zh/src/user-interface-web.rst` — Simplified Chinese translation of contextual behavior.

### Translation notes

- **EN:** Primary source document, comprehensive description of all five states and control visibility rules.
- **IT:** Full Italian translation by native-equivalent phrasing; technically accurate and consistent with existing IT documentation tone.
- **ZH:** Full Simplified Chinese translation; technically accurate, uses standard locale terminology for UI elements (`EMPTY`, `READY`, `EXECUTING`, `ENDED` states remain in English as they are technical identifiers). Applied CJK inline-markup spacing rules (backslash-space before/after CJK characters bounding inline markup) to ensure reStructuredText renders correctly.

## 2026-06-09 — Floating toolbar documentation update (PR #1835)

Updated web UI toolbar documentation to reflect implementation change: execution controls now appear as a **floating, draggable, icon-only toolbar** that overlays the content (like VS Code debug toolbar), instead of inline labeled buttons in the header.

**Key changes to documentation:**
- Execution controls are in a floating toolbar, not inline
- Toolbar is draggable to any position on screen
- Load button and editor controls (Open, Save, Clear, Restore sample, Help) remain in top header bar
- Toolbar visibility tied to simulator state (EMPTY/READY/EXECUTING/ENDED/Waiting for input)

**Files updated (all three languages):**
- `docs/user/en/src/user-interface-web.rst` — primary source with comprehensive description
- `docs/user/it/src/user-interface-web.rst` — Italian translation
- `docs/user/zh/src/user-interface-web.rst` — Chinese translation with CJK spacing rules

**Commit:** caa78112 on squad/streamline-run-controls (pushed to origin)

## 2026-06-09 — Floating Toolbar Documentation Iteration 2: Complete ✅

PR #1835 documentation committed (caa78112). All three languages (en/it/zh) updated with floating toolbar behavior, context-aware visibility, and header-resident controls. Inbox decision merged, orchestration log written. Ready for merge.

## 2026-06-09 — Program Dropdown Menu Documentation Update

### Program menu consolidation (Alternative A)

The web UI toolbar header has been redesigned to consolidate four separate program-management buttons into a **single "Program" dropdown menu**. Trinity implemented the UI change on branch `squad/program-menu`.

**Button mapping:**
- **Clear** → **New** (empty editor, leaving assembly skeleton)
- **Open Code** → **Open…** (open local file)
- **Save Code** → **Save…** (save to code.s)
- **Restore default sample** → **Load Example** (restore bundled sample)

The Program menu is **disabled while the CPU is executing** (during RUNNING/EXECUTING states) to prevent accidental program changes mid-simulation.

### Files changed

- `docs/user/en/src/user-interface-web.rst` — lines 69–130: Replaced four separate "Editor controls" with new "Program menu" subsection describing the dropdown and its four items; moved Help into its own "Help button" subsection for clarity.
- `docs/user/it/src/user-interface-web.rst` — Italian translation of Program menu (Menu Program) with equivalent subsection structure.
- `docs/user/zh/src/user-interface-web.rst` — Simplified Chinese translation of Program menu (Program 菜单) with CJK inline-markup spacing rules applied.

### Validation & commit

- Verified reStructuredText syntax: all heading underlines match heading lengths (EN, IT, ZH).
- **Commit:** 30f0fa7e on squad/program-menu (pushed to origin)
- Git message: "docs: Update web UI user docs for new Program dropdown menu"


## 2026-06-09: Trilingual Program Menu Docs — Completed

**Status:** All three languages updated, verified

Updated docs/user/{en,it,zh}/src/user-interface-web.rst for Program dropdown menu. PR #1836 ready.

## 2026-06-09: Refined Program Menu Availability Description

Program menu now described as unavailable when a program is **loaded** in the simulator (not just executing), with clarification that it becomes available again once the program finishes or simulator is reset. Updated EN, IT, ZH; commit f8e72894.

