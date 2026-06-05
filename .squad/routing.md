# Work Routing

How to decide who handles what.

## Routing Table

| Work Type | Route To | Examples |
|-----------|----------|----------|
| Core simulator (Java) | Tank | CPU pipeline, Memory, Registers, Parser, Instruction impl, Dinero tracefile, `org.edumips64.core` |
| Web UI | Trinity | React components, CSS, GWT web worker (`./gradlew war`), `src/webapp/`, npm/webpack builds |
| MIPS64 ISA / assembly | Cypher | Instruction semantics, FPU behavior, spec compliance, assembly syntax, expected test results |
| Documentation | Link | Sphinx user docs (EN/IT/ZH), developer guide, README, changelog |
| Code review | Morpheus | Review PRs, check quality, enforce minimal changes, keep `master` green |
| Testing | Smith | JUnit, end-to-end MIPS64 tests, Playwright web tests, coverage, edge cases |
| Scope & priorities | Morpheus | What to build next, trade-offs, architectural decisions |
| Session logging | Scribe | Automatic — never needs routing |

## Issue Routing

| Label | Action | Who |
|-------|--------|-----|
| `squad` | Triage: analyze issue, assign `squad:{member}` label | Morpheus (Lead) |
| `squad:{name}` | Pick up issue and complete the work | Named member |
| `squad:copilot` | Assign issue to @copilot (manual only — async draft PR) | @copilot |
| `squad:andrea` | Present to Andrea for review/decision; wait for input | Andrea (human) |

### Triage Routing Guide (for Morpheus)

- Core simulator / Java internals → `squad:tank`
- Web UI (React/JS/GWT) → `squad:trinity`
- MIPS64 instruction semantics / ISA correctness → `squad:cypher`
- Docs (EN/IT/ZH), README, dev guide → `squad:link`
- Testing / coverage / flaky tests → `squad:smith`
- Architecture, scope, ambiguous direction → `squad:morpheus` (or escalate to Andrea)
- Product/UX opinion, prioritization, "should we do this?" → `squad:andrea`
- Well-scoped fix/cleanup suitable for autonomy (e.g. lint/Codacy, small self-contained) → `squad:copilot` (assign explicitly; auto-assign is OFF)

### How Issue Assignment Works

1. When a GitHub issue gets the `squad` label, **Morpheus** triages it — analyzing content, assigning the right `squad:{member}` label, and commenting with triage notes.
2. When a `squad:{member}` label is applied, that member picks up the issue in their next session.
3. Members can reassign by removing their label and adding another member's label.
4. The `squad` label is the "inbox" — untriaged issues waiting for Lead review.

## Rules

1. **Eager by default** — spawn all agents who could usefully start work, including anticipatory downstream work.
2. **Scribe always runs** after substantial work, always as `mode: "background"`. Never blocks.
3. **Quick facts → coordinator answers directly.** Don't spawn an agent for "which Gradle task builds the worker?"
4. **When two agents could handle it**, pick the one whose domain is the primary concern. ISA semantics → Cypher; implementing them in Java → Tank.
5. **"Team, ..." → fan-out.** Spawn all relevant agents in parallel as `mode: "background"`.
6. **Anticipate downstream work.** Core change → spawn Smith for tests and Link for docs (esp. all 3 doc languages) simultaneously.
7. **User- or developer-facing change → Link updates docs.** User-facing changes go in all three languages (en/it/zh); developer-facing changes go in `docs/developer-guide.md`.
8. **MIPS64 fidelity gate.** Cypher reviews any instruction-behavior change against the MIPS64 ISA Reference Manuals (not R4000).
9. **Issue-labeled work** — `squad:{member}` routes to that member; Morpheus handles all base `squad` triage.
