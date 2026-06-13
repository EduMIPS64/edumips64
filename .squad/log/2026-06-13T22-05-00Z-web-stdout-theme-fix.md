# Session Log: Web stdout theme fix

**Date:** 2026-06-13  
**Timestamp:** 2026-06-13T22-05-00Z  

## Work Completed

**PR #1846** merged (squash commit 99b56ff4) and promoted to production (run 27480409438).

**Root cause:** Stdout text invisible due to CSS desync — raw element styles gated on OS `prefers-color-scheme` instead of user-selectable MUI theme.

**Fix:** Published MUI `paletteMode` to `html[data-theme]` attribute; re-gated static CSS rules accordingly. Added theme-aware inline styles in StdOut.js for defense-in-depth.

**Tests:** Regression test validates both desync directions (OS×app theme combinations).

**Promotion:** Authorized by @lupino3; deployed to production.
