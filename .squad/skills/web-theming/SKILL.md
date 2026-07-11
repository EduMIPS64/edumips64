# Skill: data-theme Bridge — MUI Palette Mode → Static CSS

**Author:** Trinity (Frontend Developer)  
**First applied:** PR #1846 (fix/web-stdout-sample-program), 2026-06-13T21:48:00Z

---

## Problem

When a React/MUI app supports a **user-selectable theme** (light / dark / auto), static CSS files (loaded via `<link>`) cannot intrinsically know which mode the user chose. Using `@media (prefers-color-scheme: dark)` reads the **OS** preference, which desyncs when the user overrides the theme in the app.

Failure modes:
- OS=dark, app=light → OS media query fires → e.g. white textarea text on light bg (invisible)
- OS=light, app=dark → OS media query doesn't fire → dark textarea text on dark bg (invisible)

## Solution: data-theme Bridge

### 1. Publish the resolved palette mode to the DOM

In the component that computes `paletteMode` (in EduMIPS64: `Simulator.js`), add a `useLayoutEffect` that stamps the value onto `<html>`:

```js
// paletteMode is already resolved: 'light' | 'dark'
React.useLayoutEffect(() => {
  document.documentElement.setAttribute('data-theme', paletteMode);
}, [paletteMode]);
```

**Why `useLayoutEffect`?** It fires synchronously after DOM mutations but before the browser paints, eliminating the flash-of-wrong-theme that would occur with `useEffect` (which fires after paint).

**Why `<html>`?** It is always present and ancestral to all elements, making `html[data-theme='dark'] <selector>` specificity clean and universal.

### 2. Gate static CSS rules on the app theme

Replace:
```css
/* OLD — OS-driven, desyncs from app theme override */
@media screen and (prefers-color-scheme: dark) {
  textarea { color: white; }
  /* other raw-element rules */
}
```

With:
```css
/* NEW — app-driven, always in sync */
html[data-theme='dark'] textarea { color: white; }
/* other raw-element rules */
```

The `@media (max-width: 900px)` responsive breakpoints that were nested inside the dark block must be un-nested and their selectors prefixed:
```css
@media (max-width: 900px) {
  html[data-theme='dark'] #right-panel { border-top-color: ...; }
}
```

(CSS nesting `@media` inside `html[data-theme]` would require `:is()` or native CSS nesting, which has spotty support; un-nesting is simpler and universal.)

## EduMIPS64 Specifics

- `paletteMode` is computed at `Simulator.js` ~line 594 from `themeMode` + `useMediaQuery(prefers-color-scheme: dark)` fallback for `'auto'`. The JS `useMediaQuery` calls are the **legitimate** 'auto' fallback — do NOT remove them.
- `style.css` is a static `<link>` asset, not a CSS module. Changes take effect after `npm run build-dbg`.
- After `npm run build-dbg`, confirm `out/web/style.css` contains `html[data-theme='dark']` rules.

## Defense-in-Depth

For MUI-rendered components that contain raw HTML elements (e.g. `<textarea>` in `StdOut.js`), also apply inline styles from `useTheme()`:

```js
const theme = useTheme();
// ...
<textarea style={{ color: theme.palette.text.primary, backgroundColor: theme.palette.background.paper }} />
```

Inline styles beat all stylesheet rules (except `!important`), making this the most reliable override. The `data-theme` bridge covers elements that are not MUI-managed (scrollbars, borders, third-party widgets).

## Testing Pattern

Always test both directions:

```js
// Direction 1: OS=dark, app=light → text must be dark (luminance < 150)
test('text visible in light theme with dark OS', async ({ page }) => {
  await page.emulateMedia({ colorScheme: 'dark' });
  await page.addInitScript(() => localStorage.setItem('edumips64:v1:themeMode', '"light"'));
  // ... run program, assert luminance < 150
});

// Direction 2: OS=light, app=dark → text must be light (luminance > 150)
test('text visible in dark theme with light OS', async ({ page }) => {
  await page.emulateMedia({ colorScheme: 'light' });
  await page.addInitScript(() => localStorage.setItem('edumips64:v1:themeMode', '"dark"'));
  // ... run program
  // Also assert the data-theme attribute:
  const dataTheme = await page.evaluate(() => document.documentElement.getAttribute('data-theme'));
  expect(dataTheme).toBe('dark');
  // ... assert luminance > 150
});
```

Luminance helper:
```js
function parseBrightness(cssColor) {
  const m = cssColor.match(/\d+/g);
  if (!m || m.length < 3) return null;
  const [r, g, b] = m.map(Number);
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}
```
