# Session Log — Windows CI Colon-in-Filenames Fix

**Date:** 2026-06-08  
**Context:** Coordinator maintenance — fixing master Release workflow failure

---

## What Broke

Earlier Scribe runs wrote `.squad/log/` and `.squad/orchestration-log/` files with ISO-8601 timestamps containing colons, e.g., `2026-06-08T13:55:00Z-tank.md`. On Windows, colons are illegal in filenames. When GitHub Actions checked out the repo on Windows CI runners, `actions/checkout` detected these files and failed with exit code 128, turning the master Release workflow red and blocking the Build Windows MSI and build-electron win32-x64 jobs.

---

## Fix Applied

**Commit:** ed03753b (pushed to master)

1. **Renamed all 6 offending files** to colon-free format:
   - `2026-06-08T13:55:00Z-...` → `2026-06-08T13-55-00Z-...`
   
2. **Updated Scribe charter** (`.squad/agents/scribe/charter.md`):
   - Added mandatory filename convention: use `YYYY-MM-DDTHH-MM-SSZ` format
   - Example: `2026-06-08T14-35-00Z-task.md`
   - Rationale clearly documented

3. **Activated windows-compatibility skill** (`.squad/skills/windows-compatibility/`)

4. **Updated orchestration-log template** to enforce colon-free timestamps

---

## Verification

**Release Run:** 27144326413 on ed03753b — **SUCCESS**

- ✅ Build Windows MSI job passed checkout (was the failure point)
- ✅ build-electron win32-x64 job passed checkout (was the failure point)
- ✅ CodeQL & Code Quality jobs green

---

## Separate Finding (no fix required)

"Monitor production web UI" failed once on f6d96f13 at 13:46 — it tested the live site BEFORE the 13:55 v1 promotion, receiving stale version "HEAD-70956f8". Post-promotion, the live ui.js serves "1.4.0-92-g1d037b23" (verified; matches the test's version pattern). Next scheduled monitor run will pass.

---

## Convention Summary

**Mandatory from 2026-06-08 onward:**  
All timestamped files in `.squad/log/` and `.squad/orchestration-log/` must use hyphens, not colons:
- ✅ `2026-06-08T14-35-00Z-task.md`
- ❌ `2026-06-08T14:35:00Z-task.md` (illegal on Windows)

This prevents repeated Windows CI failures and keeps master Release workflow green.
