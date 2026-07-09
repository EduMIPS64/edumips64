/**
 * Minimal type declarations for third-party modules and non-JS assets that
 * lack TypeScript type information and for which we have not added @types/*
 * packages.
 *
 * This file is intentionally written as an ambient (non-module) declaration
 * file — it has no top-level import or export statements — so that the
 * module declarations and the `declare const VERSION` apply globally.
 *
 * Window augmentation uses `typeof import(...)` expressions so that we can
 * reference Monaco types without promoting the file to a module.
 */

// ---------------------------------------------------------------------------
// Vite client types
//
// Provides TypeScript definitions for Vite-specific import suffixes such as
// `?worker` (used in monacoSetup.ts to import Monaco's editor worker) and
// `import.meta.env`.
// ---------------------------------------------------------------------------

/// <reference types="vite/client" />

// ---------------------------------------------------------------------------
// Global injected by Vite define
//
// Vite bakes VERSION (the git-describe output, e.g. "1.4.0-74-ge1b45a15")
// into the bundle at build time via define in vite.config.ts.  TypeScript
// needs to know about it so references to the bare `VERSION` identifier are
// not flagged as unknown names.
// ---------------------------------------------------------------------------

declare const VERSION: string;

// ---------------------------------------------------------------------------
// lodash/isEqual
//
// lodash v4 ships without built-in TypeScript types and @types/lodash is
// intentionally omitted to keep the lockfile stable.  Only the single
// function used in useSimulatorData.ts is declared here.
// ---------------------------------------------------------------------------

declare module 'lodash/isEqual' {
  /**
   * Performs a deep comparison between two values to determine if they are
   * equivalent.
   */
  const isEqual: (value: unknown, other: unknown) => boolean;
  export default isEqual;
}

// ---------------------------------------------------------------------------
// lodash/debounce
//
// Same rationale as lodash/isEqual: @types/lodash is not installed; only the
// debounce function used in Simulator.tsx is declared here.
// ---------------------------------------------------------------------------

declare module 'lodash/debounce' {
  // biome-ignore lint/suspicious/noExplicitAny: generic constraint needs to accept any function signature.
  function debounce<T extends (...args: any[]) => any>(
    func: T,
    wait?: number,
  ): T & { cancel(): void; flush(): void };
  export default debounce;
}

// ---------------------------------------------------------------------------
// react-dom/client
//
// react-dom v19 ships without bundled TypeScript types, and @types/react-dom
// is intentionally omitted to keep the lockfile stable.  Only the single
// `createRoot` API used in index.tsx is declared here.
// ---------------------------------------------------------------------------

declare module 'react-dom/client' {
  interface Root {
    render(children: unknown): void;
    unmount(): void;
  }

  interface CreateRootOptions {
    identifierPrefix?: string;
    onRecoverableError?: (error: unknown) => void;
  }

  function createRoot(
    container: Element | DocumentFragment,
    options?: CreateRootOptions,
  ): Root;

  export { createRoot };
  export type { Root };
}

// ---------------------------------------------------------------------------
// Static CSS imports (webpack css-loader)
//
// webpack resolves `import './css/main.css'` as a side-effect-only import.
// Without this declaration TypeScript would reject the import as a module
// with no type information.
// ---------------------------------------------------------------------------

declare module '*.css' {
  // Side-effect import — no value is exported.
}

// ---------------------------------------------------------------------------
// Static image assets (webpack file-loader / url-loader)
//
// webpack resolves `.png` imports to a URL string at build time.  Declare
// the module shape so TypeScript doesn't reject `import foo from '*.png'`
// in .tsx files.
// ---------------------------------------------------------------------------

declare module '*.png' {
  const url: string;
  export default url;
}

declare module '*.jpg' {
  const url: string;
  export default url;
}

declare module '*.svg' {
  const url: string;
  export default url;
}

// ---------------------------------------------------------------------------
// Window augmentation
//
// Code.tsx assigns `window.monaco` and `window.editor` in `editorDidMount`
// so that Playwright test helpers can access the Monaco editor instance from
// the browser context.  Declare these properties on the global Window
// interface so TypeScript doesn't reject the property assignments.
//
// Using `typeof import(...)` keeps this file as an ambient (non-module)
// declaration file while still referencing Monaco types.
// ---------------------------------------------------------------------------

interface Window {
  monaco: typeof import('monaco-editor') | null;
  editor: import('monaco-editor').editor.IStandaloneCodeEditor | null;
}
