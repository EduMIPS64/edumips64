# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

## Learnings

### 2026-06-05 — Structural Stall total fix (#1818)

**ResultFactory → Statistics.js stat mapping (structural stalls):**
- `ResultFactory.java` (~line 298-301) exports per-counter structural stall fields: `dividerStalls`, `memoryStalls`, `exStalls`, `funcUnitStalls` (the latter two were added in this fix).
- `Statistics.js` (~line 34 / 73) destructures these props and sums all four for the `stat-structural-stalls` display row.

**Key file paths:**
- `src/main/java/org/edumips64/client/ResultFactory.java` — Java → JSON bridge; add new stat fields here.
- `src/webapp/components/Statistics.js` — React component; destructure and render stats.
- `src/test/webapp/pipeline-stalls.spec.js` — Playwright tests for pipeline hazards; `readStat(page, id)` helper reads numeric stat values from the UI.

**Pattern:** When a new CPU getter needs to appear in the web UI, add a `.put("key", cpu.getX())` in `ResultFactory.java` and destructure + render it in `Statistics.js`.
