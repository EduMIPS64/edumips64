# Squad Team

> edumips64 — free, cross-platform visual MIPS64 CPU simulator (Java core, GWT/React web UI, Sphinx docs)

## Coordinator

| Name | Role | Notes |
|------|------|-------|
| Squad | Coordinator | Routes work, enforces handoffs and reviewer gates. |

## Members

| Name | Role | Charter | Status |
|------|------|---------|--------|
| Morpheus | Lead / Architect | `.squad/agents/morpheus/charter.md` | 🏗️ Active |
| Tank | Core Dev | `.squad/agents/tank/charter.md` | 🔧 Active |
| Trinity | Frontend Dev | `.squad/agents/trinity/charter.md` | ⚛️ Active |
| Smith | Tester (Reviewer) | `.squad/agents/smith/charter.md` | 🧪 Active |
| Link | Docs / DevRel | `.squad/agents/link/charter.md` | 📝 Active |
| Cypher | MIPS64 ISA Expert | `.squad/agents/cypher/charter.md` | 🧠 Active |
| @copilot | Coding Agent | uses `.github/copilot-instructions.md` | 🤖 Active |
| Andrea Spadaccini | Reviewer / Product (Human) | — | 👤 Human |
| Scribe | Session Logger | `.squad/agents/scribe/charter.md` | 📋 Silent |
| Ralph | Work Monitor | — | 🔄 Monitor |

<!-- copilot-auto-assign: false -->

### @copilot — Capability Profile

The GitHub Copilot coding agent works asynchronously via issue assignment (label `squad:copilot`), creates `copilot/*` branches, and opens draft PRs. It is **not** spawnable. Auto-assign is **off** — Morpheus assigns `squad:copilot` explicitly during triage; otherwise issues route to other members.

| Suitability | Issue Type |
|-------------|-----------|
| 🟢 Good fit | Well-scoped bug fixes, small self-contained features, lint/Codacy cleanups, mechanical refactors, doc tweaks with clear instructions |
| 🟡 With care | Multi-file features needing context, test additions, changes touching the GWT/JS boundary |
| 🔴 Avoid | MIPS64 ISA-semantics calls (Cypher), architecture decisions (Morpheus), ambiguous/underspecified issues, anything needing human judgment |

### Andrea Spadaccini — Human Member

- **Badge:** 👤 Human. Real name, no casting, no charter/history files.
- **Role:** Reviewer and product/opinion authority. Not spawnable — the coordinator presents work and waits for Andrea to relay input.
- **Reviewer gate:** When Andrea reviews and rejects, the standard lockout applies — a *different* agent revises (not the original author).
- Non-dependent work continues immediately while waiting on Andrea.

## Project Context

- **Owner:** Andrea Spadaccini
- **Project:** EduMIPS64 — educational MIPS64 CPU simulator (CPU pipeline, Memory, Parser, Instructions, FPU)
- **Stack:** Java 17+ (Gradle) core, Swing UI, GWT-compiled Web Worker, React/JS web UI (npm/webpack), Sphinx docs (Python, EN/IT/ZH); tests via JUnit + Playwright
- **Casting universe:** The Matrix
- **Created:** 2026-06-05

## Issue Source

- **Repository:** EduMIPS64/edumips64
- **Connected:** 2026-06-05
- **Filters:** open issues
- **Triage:** Morpheus triages the base `squad` label → assigns `squad:{member}`. Members include @copilot (manual only) and Andrea (human, for review/opinion items).
