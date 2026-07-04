/**
 * Unit tests for buildInfo.ts
 *
 * getBuildInfo() accepts an optional LocationLike parameter, so no browser or
 * jsdom stub is needed: we pass synthetic location objects directly.
 */

import { describe, it, expect } from 'vitest';
import { getBuildInfo } from '../../webapp/buildInfo';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const loc = (hostname: string, pathname = '/') => ({ hostname, pathname });

// Full 40-char hex SHA used in archive-build URLs.
const SHA = 'a'.repeat(40);

// ---------------------------------------------------------------------------
// dev — default when location is absent or unrecognised
// ---------------------------------------------------------------------------

describe('getBuildInfo — dev', () => {
  it('returns dev when called with null', () => {
    expect(getBuildInfo(null)).toEqual({ kind: 'dev', prNumber: null, prUrl: null });
  });

  it('returns dev for localhost', () => {
    expect(getBuildInfo(loc('localhost'))).toEqual({
      kind: 'dev',
      prNumber: null,
      prUrl: null,
    });
  });

  it('returns dev for an empty hostname (file:// context)', () => {
    expect(getBuildInfo({ hostname: '', pathname: '/' })).toEqual({
      kind: 'dev',
      prNumber: null,
      prUrl: null,
    });
  });

  it('returns dev for an arbitrary unrecognised hostname', () => {
    expect(getBuildInfo(loc('staging.example.com'))).toEqual({
      kind: 'dev',
      prNumber: null,
      prUrl: null,
    });
  });

  it('returns dev when hostname is undefined', () => {
    expect(getBuildInfo({ pathname: '/' })).toEqual({
      kind: 'dev',
      prNumber: null,
      prUrl: null,
    });
  });
});

// ---------------------------------------------------------------------------
// production — root path on the Pages hostname
// ---------------------------------------------------------------------------

describe('getBuildInfo — production', () => {
  it('classifies the Pages root as production', () => {
    expect(getBuildInfo(loc('web.edumips.org', '/'))).toEqual({
      kind: 'production',
      prNumber: null,
      prUrl: null,
    });
  });

  it('is case-insensitive for the hostname', () => {
    expect(getBuildInfo(loc('WEB.EDUMIPS.ORG', '/'))).toEqual({
      kind: 'production',
      prNumber: null,
      prUrl: null,
    });
  });
});

// ---------------------------------------------------------------------------
// archive-build — /c/<sha>/ path on the Pages hostname
// ---------------------------------------------------------------------------

describe('getBuildInfo — archive-build', () => {
  it('classifies /c/<sha>/ as archive-build and extracts the sha', () => {
    const result = getBuildInfo(loc('web.edumips.org', `/c/${SHA}/`));
    expect(result.kind).toBe('archive-build');
    expect(result.sha).toBe(SHA);
    expect(result.prNumber).toBeNull();
    expect(result.prUrl).toBeNull();
    expect(result.buildUrl).toBe(`https://web.edumips.org/c/${SHA}/`);
  });

  it('does not classify /c/ with a short (non-40-char) sha as archive-build', () => {
    // A truncated SHA would not match the BUILD_PATH_RE (requires exactly 40 hex chars).
    const result = getBuildInfo(loc('web.edumips.org', '/c/abc/'));
    expect(result.kind).toBe('production'); // falls through to the production branch
  });

  it('does not classify a path with non-hex chars in the sha', () => {
    const badSha = 'z'.repeat(40);
    const result = getBuildInfo(loc('web.edumips.org', `/c/${badSha}/`));
    // Does not match BUILD_PATH_RE — treated as production root variant.
    expect(result.kind).toBe('production');
  });
});

// ---------------------------------------------------------------------------
// pr — CI staging hostname
// ---------------------------------------------------------------------------

describe('getBuildInfo — pr', () => {
  it('classifies a PR-preview URL as pr and extracts the PR number', () => {
    const result = getBuildInfo(
      loc('edumips64ci.z16.web.core.windows.net', '/42/index.html'),
    );
    expect(result.kind).toBe('pr');
    expect(result.prNumber).toBe(42);
    expect(result.prUrl).toBe('https://github.com/EduMIPS64/edumips64/pull/42');
  });

  it('handles a path ending with just the PR number and a slash', () => {
    const result = getBuildInfo(
      loc('edumips64ci.z16.web.core.windows.net', '/1234/'),
    );
    expect(result.kind).toBe('pr');
    expect(result.prNumber).toBe(1234);
  });

  it('falls through to dev when the CI hostname has an unrecognised path', () => {
    // If the CI hostname is used but the path doesn't start with a PR number
    // segment the function returns dev (the fall-through at the bottom).
    const result = getBuildInfo(
      loc('edumips64ci.z16.web.core.windows.net', '/'),
    );
    expect(result.kind).toBe('dev');
  });
});
