/**
 * Minimal type declarations for third-party modules and non-JS assets that
 * lack TypeScript type information and for which we have not added @types/*
 * packages.
 */

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
// Static image assets (webpack file-loader / url-loader)
//
// webpack resolves `.png` imports to a URL string at build time.  Declare
// the module shape so TypeScript doesn't reject `import foo from '*.png'`
// in .tsx files.  (Previously these imports were only in .js files where
// checkJs:false suppressed the check; .tsx files are fully type-checked.)
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
