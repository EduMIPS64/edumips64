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
