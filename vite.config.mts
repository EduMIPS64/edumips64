import { defineConfig, type Plugin } from 'vite';
import react from '@vitejs/plugin-react';
import istanbul from 'vite-plugin-istanbul';
import { execSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
import fs from 'node:fs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// Get the git-describe version string and strip the leading 'v'.
// The glob MUST be quoted — an unquoted v* is expanded by the shell against
// the repo root; since vitest.config.js exists it became `--match
// vitest.config.js`, no tag matched, and the version silently degraded to a
// bare commit sha (see prod incident #1917).
const gitDescribe = (() => {
  try {
    return execSync("git describe --tags --match 'v*' --always --dirty", {
      encoding: 'utf8',
    })
      .trim()
      .replace(/^v/, '');
  } catch {
    return 'dev';
  }
})();

// Copy out/docs → out/web/docs after every build.
// noErrorOnMissing semantics: if out/docs doesn't exist yet (e.g. the Sphinx
// build hasn't run), do nothing silently.
function copyDocsPlugin(): Plugin {
  return {
    name: 'copy-docs',
    // closeBundle runs after Rollup has written all files to disk.
    closeBundle() {
      const src = path.resolve(__dirname, 'out/docs');
      const dest = path.resolve(__dirname, 'out/web/docs');
      if (fs.existsSync(src)) {
        fs.cpSync(src, dest, { recursive: true, force: true });
      }
    },
  };
}

// Serve the GWT-compiled simulator core (worker.js) from out/web on the dev
// server.  Vite's root is src/webapp, but worker.js is a build artifact that
// lives in out/web (produced by `gradlew assembleWebApp`), so `new
// Worker("worker.js")` would otherwise hit the SPA fallback and receive
// index.html — surfacing as "Failed to load the EduMIPS64 simulator core /
// Unexpected token '<'".  This middleware bridges that gap for `npm start`;
// the production build already emits worker.js next to ui.js, so it is
// dev-only (apply: 'serve').
function serveGwtWorkerPlugin(): Plugin {
  // The only artifacts the worker entrypoint needs at runtime.  worker.js is
  // self-contained; clear.cache.gif is included for completeness.
  const CONTENT_TYPES: Record<string, string> = {
    '/worker.js': 'application/javascript',
    '/clear.cache.gif': 'image/gif',
  };
  return {
    name: 'serve-gwt-worker',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const pathname = (req.url || '').split('?')[0];
        const contentType = CONTENT_TYPES[pathname];
        if (!contentType) {
          return next();
        }
        const file = path.resolve(__dirname, 'out/web', pathname.slice(1));
        if (!fs.existsSync(file)) {
          // Fall through so the user still gets a clear 404 rather than the
          // HTML fallback; the console error then points at the missing
          // build artifact instead of a confusing syntax error.
          res.statusCode = 404;
          res.end(
            `${pathname} not found in out/web — run \`./gradlew assembleWebApp\` to build the GWT worker.`,
          );
          return;
        }
        res.setHeader('Content-Type', contentType);
        fs.createReadStream(file).pipe(res);
      });
    },
  };
}

export default defineConfig(({ mode }) => {
  const isDev = mode === 'development';
  return {
    // Vite root is src/webapp so that index.html, index.tsx, etc. are
    // resolved from there without needing path prefixes.
    root: 'src/webapp',

    // src/webapp/static/ contains favicon, CSS, and image assets that are
    // copied verbatim to the output directory (no hashing).
    publicDir: 'static',

    // Relative base so that all asset URLs are relative to index.html.
    // This is CRITICAL for subpath hosting: the app is served both at /
    // and at /c/<sha>/ — absolute paths would break deep-path deploys.
    base: './',

    define: {
      // Inject the build version (git-describe output, e.g. "1.4.0-74-ge1b45a15")
      // as a global constant.  Declared in vendor.d.ts.
      VERSION: JSON.stringify(gitDescribe),
    },

    build: {
      // Write to out/web/ at the repo root (absolute to avoid confusion with
      // root-relative resolution).
      outDir: path.resolve(__dirname, 'out/web'),

      // Do NOT wipe out/web before building.  Gradle puts worker.js there
      // (GWT compile output) and copyWebHelp puts docs/ there; we must
      // preserve both.
      emptyOutDir: false,

      // Development builds get fast inline source maps; production gets an
      // external .map file.
      sourcemap: isDev ? 'inline' : true,

      // Development builds are unminified for easier debugging.
      minify: isDev ? false : 'esbuild',

      rollupOptions: {
        output: {
          // Keep the entry bundle named ui.js — deploy tooling, the version-
          // drift monitor (monitor-webui.yml), and tests all grep for it by
          // name.
          entryFileNames: 'ui.js',
          // Worker chunks and code-split chunks go to a subdirectory so they
          // don't clutter the root alongside worker.js and docs/.
          chunkFileNames: 'chunks/[name]-[hash].js',
          assetFileNames: 'assets/[name]-[hash][extname]',
        },
      },
    },

    plugins: [
      // @vitejs/plugin-react uses esbuild for fast JSX/TS transformation.
      // It uses the automatic JSX runtime (react/jsx-runtime) by default —
      // no need for a Babel configuration.
      react(),

      // Istanbul code coverage instrumentation.  Only active when COVERAGE=true
      // is set at build time.  Checked here via process.env rather than via
      // the plugin's requireEnv option (which checks Vite's VITE_-prefixed env)
      // so that the existing `COVERAGE=true npm run build-dbg` CI invocation
      // works without renaming.  The global window.__coverage__ it populates
      // is read by the Playwright fixtures in src/test/webapp/fixtures.js.
      ...(process.env.COVERAGE === 'true'
        ? [
            istanbul({
              include: 'src/webapp/**/*',
              exclude: ['src/test/**', 'node_modules/**'],
              extension: ['.ts', '.tsx'],
              requireEnv: false,
              forceBuildInstrument: true,
              checkProd: false,
            }),
          ]
        : []),

      copyDocsPlugin(),

      // Dev-only: serve the GWT worker.js from out/web so `npm start` can
      // load the simulator core (see plugin comment above).
      serveGwtWorkerPlugin(),
    ],
  };
});
