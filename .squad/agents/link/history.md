# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Recent Completions

- 2026-06-05T12:43:38Z: Added `## Unreleased` section to CHANGELOG.md (PR #1812), documenting 3 user-facing changes from PRs #1736, #1804, #1713/#1697. SUCCESS.

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

## Learnings — 2026-06-07: Build identity in user manual & web promotion docs

### common_conf.py fallback chain (docs/user/common_conf.py)
Replaced the fragile `open('../../../../gradle.properties')` (cwd-relative) with a five-step fallback:
1. `EDUMIPS64_BUILD_VERSION` env var (explicit override, highest priority).
2. `git describe --tags --match v* --always --dirty`, run via `git -C <repo_root>` where `<repo_root>` is resolved from `os.path.abspath(__file__)` — works regardless of the working directory (RTD, local, Gradle subproject invocations).  Leading `v` stripped.
3. `READTHEDOCS_GIT_COMMIT_HASH` shortened to 8 chars (RTD shallow-clone fallback when git tags aren't available).
4. `gradle.properties` `version=` line, resolved via `__file__` (not cwd) — fixes the existing fragility.
5. `"unknown"` as final fallback.
Each step is wrapped in `try/except` so any failure falls through silently.

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
