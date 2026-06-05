# Squad Decisions

## Active Decisions

## 2026-06-05: Issue Triage — Session Morpheus

Prioritized backlog recommendation for the squad based on 18 open issues.

### A. Top Picks (tackle now)

| # | Issue | Why | Effort | Risk | Route to |
|---|-------|-----|--------|------|----------|
| 1808 | Bump transitive `uuid` (CVE-2026-41907) | Security hygiene — moderate CVE, transitive via sockjs/istanbul-lib-processinfo. Likely just an npm override or bumping parent deps. | S | Low | Trinity / @copilot |
| 1717 | Codacy react-hooks/exhaustive-deps (3 sites) | Good-first-issue, improves code quality, low risk. | S | Low | @copilot |
| 1643 | Label references in .word64/.word32 directives | Real parser feature gap — common MIPS pattern (storing addresses in data). Scoped to Parser.java. | M | Med | Tank + Cypher (ISA validation) |
| 1799 | Make stats coherent with architecture | Removes misleading WAW stats; educational correctness matters. Needs audit of what stats exist vs. what's architecturally possible. | M | Low | Tank |
| 588 | Add more web UI tests | Priority:1, up-for-grabs. Playwright infra already exists (editor-persistence tests just landed). Incremental. | M | Low | Smith + Trinity |

### B. Quick Wins

- **#1717** — exhaustive-deps lint fix, 3 sites. Perfect for @copilot one-shot.
- **#1808** — npm override in package.json (`"overrides": {"uuid": ">=11.1.1"}`), verify build passes. @copilot or Trinity.
- **#222** — Codacy items (broad, but individual items can be cherry-picked as good-first-issues).

### C. Bigger Bets

| # | Issue | Effort | Notes |
|---|-------|--------|-------|
| 4 | Delay slot | L-XL | Touches pipeline core + UI cycle display. Fresh demand: 300-400 student/year course wants EduMIPS64 as WinDLX replacement specifically for pipelining study. High pedagogical value but architecturally invasive. **Next step:** Cypher + Tank produce a design spike (2-3 days) scoping pipeline changes, then decide go/no-go. |
| 692 | Branch Taken / Misprediction stalls | L | Related to #4 — both improve pipeline fidelity. Could be sequenced after delay slot or done independently. Needs design doc. |
| 703 | Non-aligned memory accesses | M-L | Core memory subsystem change. Needs decision: raise exception (MIPS64 spec) or silently handle? Cypher to clarify ISA semantics first. |
| 709 | Customizable FPU latency | M | Core + UI. Useful for teaching, moderate scope. Lower urgency than pipeline items. |

### D. Close / No-Action

| # | Action | Reason |
|---|--------|--------|
| 1709 | **CLOSE** | Fully resolved by PR #1736 (merged 2026-06-05). localStorage persistence + Restore default sample button implemented, tested, documented. |
| 619 | Leave as-is | Renovate's auto-managed Dependency Dashboard. Not actionable work. |

### E. Recommended Next Step

**Immediate sprint (1-2 weeks):** Close #1709 as done. Assign #1808 and #1717 to @copilot for same-day resolution (both are S-sized, mechanical fixes). Start Tank on #1643 (label references in data directives) with Cypher reviewing the ISA semantics. In parallel, kick off a time-boxed design spike on #4 (delay slot) — Cypher + Tank, 3 days max — to produce a concrete scope/effort estimate given the university adoption signal. That spike will tell us whether to commit to #4 this quarter or defer. Everything else stays in backlog ordered as above.

---

## 2026-06-05: Trinity decision — uuid CVE override

**Date:** 2026-06-05T16:05:59+02:00

**Context:** GitHub issue #1808 reports CVE-2026-41907 in transitive npm `uuid@8.3.2`, pulled by dev/build-time tooling (`sockjs` via `webpack-dev-server`, `istanbul-lib-processinfo` via coverage tooling).

**Decision:** Add a top-level npm `overrides` entry pinning `uuid` to `^11.1.1` instead of upgrading parent tooling, because this is the least invasive fix and keeps the existing web build pipeline stable.

**Verification:** `npm ls uuid` resolves both transitive paths to `uuid@11.1.1 overridden`; `npm run build` succeeds.

---

## 2026-06-05: Learning — Classic JSX runtime requirement

**Context:** EduMIPS64 web UI (src/webapp) uses the classic JSX runtime via @babel/preset-react WITHOUT `runtime:automatic`.

**Finding:** In PR #1814 (react-hooks/exhaustive-deps fix), Trinity removed the `import React` statement from Code.js to clean up unused imports per ESLint. This compiled/built successfully but crashed at runtime with "React is not defined", causing all 57 Playwright tests to fail.

**Learning:** Every src/webapp component MUST keep `import React` in scope, even if no JSX appears to use it directly. The classic JSX runtime requires React in scope for JSX compilation. Removing it breaks the application at runtime despite clean compilation.

**Action:** All developers must be aware that classic JSX runtime is in use. ESLint rules that suggest removing unused imports must be reviewed manually before committing React component files.

---

## 2026-06-05: Trinity decision — react-hooks/exhaustive-deps fix

**Date:** 2026-06-05T16:05:59+02:00

**Context:** Codacy reported 3 react-hooks/exhaustive-deps violations: Code.js (missing dependencies in useEffect), plus 2 other sites.

**Decision:** Fix violations by:
1. Adding missing useEffect dependencies (refs validated for correctness).
2. Creating Playwright regression test `src/test/webapp/exhaustive-deps-regressions.spec.js` to catch future regressions.
3. Ensuring `import React` remains in scope (classic JSX runtime requirement).

**Verification:** All 57 Playwright tests pass after applying `import React` fix.

**Merged:** PR #1814 (squash merge).

---

## Governance

- All meaningful changes require team consensus
- Document architectural decisions here
- Keep history focused on work, decisions focused on direction
