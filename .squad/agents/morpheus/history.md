# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

- **2026-06-07:** When restructuring a design doc from "debate" to "authoritative," verify each section against the actual implementation files (workflows, scripts) rather than trusting earlier prose. The nightly trigger had changed from `workflow_run` to cron but the doc still said "on every green master push." Always cross-reference repo artifacts as ground truth.

- **2026-06-09:** Web UI run controls are in `src/webapp/components/Header.js` (lines 184-347). Simulator state is managed in `src/webapp/components/Simulator.js` with `status` (READY/RUNNING/STOPPED), `executing` (boolean), and derived `simulatorRunning`. The `CpuStatusDisplay.js` component renders the status chip. Playwright tests for buttons are in `src/test/webapp/`, with shared helpers in `test-utils.js`. Designed contextual run controls (VSCode debugger-style) with a 5-state model: EMPTY, READY_TO_RUN, EXECUTING, PAUSED, ENDED.

- **2026-06-09:** Finalized run-controls design: 4-state model (EMPTY/READY/EXECUTING/ENDED) + WAITING_FOR_INPUT overlay; contextual hiding via conditional render (not opacity/visibility — Playwright compat); Stop disabled (not hidden) during EXECUTING; PAUSED collapsed into READY; no STOPPING React state; keyboard shortcuts deferred to follow-up PR. Design implemented in PR #1835 (squad/streamline-run-controls): Trinity coded Header.js + Simulator.js, Smith wrote 8-test spec (all PASS), Link updated user docs (EN/IT/ZH). Full suite 68/70 PASS. Design locked and handed off to implementation.
