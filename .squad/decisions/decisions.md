# Decisions

## 2026-06-13: Web stdout text invisible in light theme — root cause desync (data-theme bridge)

**Date:** 2026-06-13  
**Author:** Trinity (Frontend Developer)  
**PR:** https://github.com/EduMIPS64/edumips64/pull/1846  
**Branch:** `fix/web-stdout-sample-program`  
**Status:** MERGED (squash commit 99b56ff4), promoted to production via run 27480409438  

---

## Context

User reported: "Default sample program produces no Standard Output in web UI."

Initial investigation found the bug could not be reproduced. Deployed `https://web.edumips.org/worker.js` was bit-for-bit identical to a fresh `./gradlew war` build of `master` (md5 `3566127f96df40a03db7f3d371b594ae`, deployed at HEAD `fdd86763`). Ran default-sample flow against both local and production builds in headed Chromium across multiple variants — all green.

## Root Cause (Confirmed)

The stdout **text was always present** in the textarea value. It was **invisible** due to color contrast desync:

- `src/webapp/static/style.css` contained: `@media screen and (prefers-color-scheme: dark) { textarea { color: white; ... } }`
- This media query tracks the **OS** color scheme preference, NOT the user-selectable MUI theme
- When OS=dark but app=light, the media query fires (white text) while MUI renders light background → invisible
- When OS=light but app=dark, media query doesn't fire (dark text) while MUI renders dark background → also invisible

Root cause: stylesheet CSS rules gated on OS `prefers-color-scheme`, but MUI theme is user-selectable via `localStorage['edumips64:v1:themeMode']`.

## Decision: data-theme Bridge Fix

Publish the resolved MUI `paletteMode` to the DOM via `html[data-theme]` attribute, re-gate static CSS rules on this attribute instead of OS media query.

### Implementation

#### 1. Simulator.js (~line 601)
```js
React.useLayoutEffect(() => {
  document.documentElement.setAttribute('data-theme', paletteMode);
}, [paletteMode]);
```
Added after `paletteMode` is resolved. Uses `useLayoutEffect` to avoid flash-of-wrong-theme on first render. The `paletteMode` variable already correctly resolves `auto→OS, light→light, dark→dark`.

#### 2. style.css
Replaced `@media screen and (prefers-color-scheme: dark) { ... }` block with:
```css
html[data-theme='dark'] #right-panel { border-left-color: rgba(255,255,255,0.188); }
html[data-theme='dark'] div.error-accordion { border-bottom-color: rgba(255,255,255,0.188); }
html[data-theme='dark'] textarea { background: transparent; color: white; }
html[data-theme='dark'] ::-webkit-scrollbar { background-color: #1e1e1e; }
html[data-theme='dark'] ::-webkit-scrollbar-thumb { background-color: rgb(97,97,97); }
```

#### 3. StdOut.js (Defense-in-Depth)
Added MUI theme-aware inline styles:
```js
const theme = useTheme();
// In JSX: style={{ color: theme.palette.text.primary, backgroundColor: theme.palette.background.paper }}
```
Ensures exact MUI palette match even if stylesheet rules lag.

### What Was NOT Changed
- JS `useMediaQuery('(prefers-color-scheme: dark)')` calls in Simulator.js/Code.js — legitimate 'auto' mode fallback; `paletteMode` already resolves this
- Java core or GWT compilation

## Tests

Two regression directions validated:
1. **OS=dark, app=light** → text luminance < 150 (dark on light, visible) ✅
2. **OS=light, app=dark** → text luminance > 150 (light on dark, visible) ✅
   - Also asserts `document.documentElement.getAttribute('data-theme') === 'dark'`

Playwright recipe:
```js
await page.emulateMedia({ colorScheme: 'dark' });
await page.addInitScript(() => {
  window.localStorage.setItem('edumips64:v1:themeMode', '"light"');
});
// Run simulation...
const styles = await page.locator('#stdout-view').evaluate((el) => {
  const s = getComputedStyle(el);
  return { color: s.color };
});
const brightness = parseBrightness(styles.color); // luminance calc
expect(brightness).toBeLessThan(150);
```

## Files

- `src/webapp/components/Simulator.js` — added data-theme bridge in useLayoutEffect
- `src/webapp/components/StdOut.js` — added useTheme() inline styles
- `src/webapp/static/style.css` — re-gated rules on `html[data-theme='dark']`
- `src/test/webapp/sample-program-stdout.spec.js` — new regression test (+ 2 test directions)

## Verification

- Regression test: PASS (both OS × app theme combinations)
- Related sub-suite: 12 passed
- Full Playwright suite: 74 passed, 1 skipped (pre-existing)
- Build: `npm run build-dbg` succeeded; ESLint clean
- **PR merged:** squash commit 99b56ff4
- **Promoted to production:** run 27480409438

## Reusable Pattern

See `.squad/skills/web-theming/SKILL.md` for the general data-theme bridge pattern.
