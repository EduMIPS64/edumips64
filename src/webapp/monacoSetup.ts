/**
 * Monaco editor setup: wire the editor web worker and configure
 * @monaco-editor/react to use the locally bundled Monaco (not CDN).
 *
 * This module MUST be imported before any @monaco-editor/react component
 * mounts (i.e. at the top of index.tsx, before React renders).
 *
 * Worker setup
 * ------------
 * The `?worker` Vite query tells Rollup/Vite to emit the Monaco editor
 * worker as a separate chunk and return a Worker constructor.  Vite embeds
 * a `new URL('./chunk.js', import.meta.url)` reference so the URL resolves
 * correctly regardless of the host path (CRITICAL for /c/<sha>/ subpath
 * hosting — the base is set to './' in vite.config.ts).
 *
 * We only need the base `editorWorkerService` (text model operations,
 * bracket matching, etc.).  No language-specific workers are required
 * because the app uses its own custom 'mips' Monarch grammar registered
 * at startup.
 *
 * Offline / CDN-free loading
 * --------------------------
 * `loader.config({ monaco })` passes the already-imported Monaco ESM
 * module to @monaco-editor/react's internal loader, bypassing CDN entirely.
 * This makes the app usable without internet access and avoids any version
 * drift between the bundled Monaco and the CDN copy.
 */

import { loader } from '@monaco-editor/react';
// monaco-editor 0.53+ restructured its ESM distribution: the package's `.`
// export now resolves to `esm/vs/editor/editor.main`, which is both the
// typed Monaco API surface (formerly editor.api) *and* activates all editor
// feature contributions (hover provider, find-replace, bracket matching,
// etc.) as a side effect of import — there is no longer a separate
// type-only `editor.api` entry point exposed via the package's `exports`
// map (the file still exists on disk, but resolving it deeply is
// unsupported and breaks under `moduleResolution: bundler`).
// At runtime this is the same singleton imported everywhere else (e.g.
// Code.tsx's `import * as monacoEditor from 'monaco-editor'`), so it sees
// the registered features.
// editor.main also loads built-in language grammars; our custom MIPS grammar
// overrides the stock one via registerTokensProviderFactory in Code.tsx.
import * as monaco from 'monaco-editor';
// Vite-specific ?worker import: emits the worker as a separate chunk and
// returns a Worker constructor whose URL resolves via import.meta.url.
import EditorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';

// Wire the Monaco editor worker.  MonacoEnvironment.getWorker is the
// non-deprecated replacement for getWorkerUrl.
// Use `window` (typed as Window, which monaco-editor augments with
// MonacoEnvironment) rather than `self` or `globalThis` to avoid a TS
// narrowing gap in bundler moduleResolution.
window.MonacoEnvironment = {
  // Monaco calls getWorker to obtain the editor background worker.
  // The moduleId and label parameters identify the requested worker type;
  // since we only need the single `editorWorkerService`, we ignore them.
  getWorker(): Worker {
    return new EditorWorker();
  },
};

// Tell @monaco-editor/react to use our already-imported Monaco ESM module.
// Without this call the library falls back to loading Monaco from CDN
// (unpkg), which breaks in offline / air-gapped environments.
loader.config({ monaco });
