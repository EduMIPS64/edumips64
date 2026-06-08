# Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — free cross-platform visual MIPS64 CPU simulator for education (CPU, Memory, Parser, Instructions, FPU).
- **Stack:** Java 17+ (Gradle) core simulator, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH). Tests: JUnit + Playwright.
- **Created:** 2026-06-05T07:18:06+02:00

## Learnings

<!-- Append new learnings below. Each entry is something lasting about the project. -->

- **2026-06-07:** When restructuring a design doc from "debate" to "authoritative," verify each section against the actual implementation files (workflows, scripts) rather than trusting earlier prose. The nightly trigger had changed from `workflow_run` to cron but the doc still said "on every green master push." Always cross-reference repo artifacts as ground truth.
