---
name: "html-mockup-screenshots"
description: "Render HTML/CSS design mockups to PNG using Playwright + headless Chromium, suitable for toolbar/component exploration without touching src/"
domain: "design, prototyping, tooling"
confidence: "high"
source: "earned"
---

## Context

When producing design exploration mockups for EduMIPS64 UI components (e.g., AppBar toolbar alternatives), you need to render HTML to PNG without modifying `src/`. The project has Playwright installed in its own `node_modules`; Chromium is at `/usr/bin/chromium-browser`.

## Patterns

### 1. Write HTML mockups to the session state files dir

Write standalone HTML files (no bundler, CDN fonts/icons are fine for mockups) directly to the session state output directory, e.g.:
```
/home/andrea/.copilot/session-state/<session-id>/files/mock-altX.html
```

### 2. Write the Node screenshot script to the project root

The script must live inside the project directory so `require('playwright')` resolves via the project's `node_modules`. Do **not** run `node /path/outside/project/script.js` — Node.js won't find project-local packages that way.

```js
// /home/andrea/source/github/edumips64/screenshot-mockups.js
const { chromium } = require('playwright');
const path = require('path');
const fs   = require('fs');

const SESS = '/home/andrea/.copilot/session-state/<id>/files';
const PROJ = '/home/andrea/source/github/edumips64';

const shots = [
  { html: 'mock-altA.html', out: 'mock-altA.png', height: 80 },
  // …
];

(async () => {
  const browser = await chromium.launch({
    executablePath: '/usr/bin/chromium-browser',
    args: ['--no-sandbox', '--disable-dev-shm-usage', '--disable-gpu'],
  });
  for (const s of shots) {
    const page = await browser.newPage();
    await page.setViewportSize({ width: 1280, height: s.height });
    // Use setContent, NOT page.goto('file://…') — file:// is blocked by sandbox
    const html = fs.readFileSync(path.join(PROJ, s.html), 'utf8');
    await page.setContent(html, { waitUntil: 'networkidle' });
    await page.waitForTimeout(1500); // let CDN fonts render
    await page.screenshot({ path: path.join(SESS, s.out), clip: { x: 0, y: 0, width: 1280, height: s.height } });
    await page.close();
  }
  await browser.close();
})();
```

### 3. Run from the project root

```bash
cd /home/andrea/source/github/edumips64
node screenshot-mockups.js
```

### 4. Clean up throwaway files

After screenshots are captured, remove the HTML files and the script from the project root:
```bash
rm -f mock-alt*.html screenshot-mockups.js
```

### 5. Verify output

```bash
ls -lh /home/andrea/.copilot/session-state/<id>/files/mock-alt*.png
# Each PNG should be > 5 KB; toolbar strips at 1280×80 are typically 15–30 KB
```

### Important: embed fonts as base64 — CDN fonts don't load in headless setContent()

Google Fonts / Material Icons CDN URLs **do not load** when using `page.setContent()` in Playwright headless mode. Icons render as their text ligature names instead of glyphs.

**Fix:** read the local font file and inject it as a base64 data URI via `page.addStyleTag()`:

```js
const fontB64 = fs.readFileSync(
  '/usr/share/fonts/woff/material-design-icons-iconfont/MaterialIcons-Regular.woff2'
).toString('base64');

const FONT_CSS = `
@font-face {
  font-family: 'Material Icons';
  font-style: normal; font-weight: 400; font-display: block;
  src: url('data:font/woff2;base64,${fontB64}') format('woff2');
}
.mi {
  font-family: 'Material Icons'; font-weight: normal; font-style: normal;
  display: inline-block; white-space: nowrap; direction: ltr;
  -webkit-font-smoothing: antialiased; text-rendering: optimizeLegibility;
  font-feature-settings: 'liga'; vertical-align: middle;
}`;

// After setContent:
await page.addStyleTag({ content: FONT_CSS });
await page.waitForTimeout(300); // let font paint
```

Use `font-family: 'Material Icons'` in HTML/CSS (not `'Material Icons Round'` — the local file is the Regular variant). The local font is at `/usr/share/fonts/woff/material-design-icons-iconfont/MaterialIcons-Regular.woff2`.

## Examples

- **2026-06-09 session 359dc6eb:** 6 PNGs for program-controls alternatives (Alt A–D), each 19–29 KB, rendered with 1280×80 viewport for toolbar strips and 1280×230–260 for open-menu states. Embedded Material Icons Regular woff2 as base64 via addStyleTag.

## Anti-Patterns

- **`page.goto('file://…')`** — Chromium's renderer in Playwright sandbox denies `file://` URIs for external HTML files. Use `page.setContent(html)` instead (read the file with `fs.readFileSync`).
- **Running the script outside the project root** — `require('playwright')` will fail with MODULE_NOT_FOUND. Always `cd` to the project dir first.
- **CDN fonts in setContent()** — `https://fonts.googleapis.com/icon` does not load in Playwright headless setContent mode. Always embed fonts as base64 data URIs injected via `addStyleTag`.
- **Leaving HTML/script files in src/** — These are throwaway exploration artefacts. Keep them in the session state dir (for HTML) and project root temporarily (for the script), then delete.
- **Loading MUI/React from CDN for production fidelity** — CDN MUI is fine for mockups but produces slightly different rendering than the built app. For pixel-perfect comparisons, build a real storybook instead.

## Update — 2026-06-09

**Inline SVG icons are more reliable than icon-font ligatures for headless screenshots.** Even with the Material Icons woff2 embedded as base64, font subsetting or rendering timing can cause some ligature names to render as plain text instead of glyphs (inconsistent across icon elements). The 100% reliable alternative is to embed inline `<svg viewBox="0 0 24 24"><path d="…"/></svg>` elements using MUI's SVG path data (grep from `node_modules/@mui/icons-material/*.js` for the `d:` prop). Use `fill="currentColor"` so the SVG inherits color from the parent element. This eliminates all dependency on font loading and produces consistent glyph rendering across every icon in every mock.
