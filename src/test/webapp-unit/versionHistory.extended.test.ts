/**
 * Extended unit tests for versionHistory.ts
 *
 * Covers:
 *   - getViewedSha path parsing
 *   - fetchVersions with mocked fetch (ok, invalid, error)
 *   - buildVersionList edge cases not covered by versionHistory.test.ts
 */

import { describe, it, expect, vi, afterEach } from 'vitest';
import {
  getViewedSha,
  fetchVersions,
  buildVersionList,
  isValidVersions,
} from '../../webapp/versionHistory';

// Full 40-character hex SHA for use in path tests.
const SHA = 'b'.repeat(40);

afterEach(() => vi.restoreAllMocks());

// ---------------------------------------------------------------------------
// getViewedSha
// ---------------------------------------------------------------------------

describe('getViewedSha', () => {
  it('returns null when called with null', () => {
    expect(getViewedSha(null)).toBeNull();
  });

  it('returns null for a root path ("/")', () => {
    expect(getViewedSha({ pathname: '/' })).toBeNull();
  });

  it('returns null for an empty pathname', () => {
    expect(getViewedSha({ pathname: '' })).toBeNull();
  });

  it('returns the sha for a /c/<sha>/ path', () => {
    expect(getViewedSha({ pathname: `/c/${SHA}/` })).toBe(SHA);
  });

  it('returns the sha even without a trailing slash', () => {
    // BUILD_PATH_RE uses (?:\/|$) so the trailing slash is optional.
    expect(getViewedSha({ pathname: `/c/${SHA}` })).toBe(SHA);
  });

  it('returns null for a malformed path that looks like /c/ but has the wrong sha length', () => {
    // 39-char sha — not a valid 40-char hex sha.
    const shortSha = 'c'.repeat(39);
    expect(getViewedSha({ pathname: `/c/${shortSha}/` })).toBeNull();
  });

  it('returns null for a path with non-hex characters in the sha segment', () => {
    const nonHexSha = 'z'.repeat(40);
    expect(getViewedSha({ pathname: `/c/${nonHexSha}/` })).toBeNull();
  });

  it('returns null when pathname is undefined', () => {
    expect(getViewedSha({})).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// isValidVersions
// ---------------------------------------------------------------------------

describe('isValidVersions', () => {
  it('returns false for null', () => {
    expect(isValidVersions(null)).toBe(false);
  });

  it('returns false for a non-object', () => {
    expect(isValidVersions('string')).toBe(false);
    expect(isValidVersions(42)).toBe(false);
  });

  it('returns false when versions is not an array', () => {
    expect(isValidVersions({ current: null, versions: 'bad' })).toBe(false);
  });

  it('returns false when current is a number (must be null or string)', () => {
    expect(isValidVersions({ current: 42, versions: [] })).toBe(false);
  });

  it('returns true for a valid empty versions array', () => {
    expect(isValidVersions({ current: null, versions: [] })).toBe(true);
  });

  it('returns true with current as a string', () => {
    const entry = { sha: SHA, seq: 1, build: '1.0', promoted: false };
    expect(isValidVersions({ current: SHA, versions: [entry] })).toBe(true);
  });

  it('returns false when an entry is missing the required sha field', () => {
    const bad = { seq: 1, build: '1.0', promoted: false };
    expect(isValidVersions({ current: null, versions: [bad] })).toBe(false);
  });

  it('returns false when an entry has a non-integer seq', () => {
    const bad = { sha: SHA, seq: 1.5, build: '1.0', promoted: false };
    expect(isValidVersions({ current: null, versions: [bad] })).toBe(false);
  });

  it('returns false when promoted is not a boolean', () => {
    const bad = { sha: SHA, seq: 1, build: '1.0', promoted: 'yes' };
    expect(isValidVersions({ current: null, versions: [bad] })).toBe(false);
  });
});

// ---------------------------------------------------------------------------
// fetchVersions — mocked fetch
// ---------------------------------------------------------------------------

describe('fetchVersions', () => {
  it('returns a valid VersionsIndex when fetch returns valid JSON', async () => {
    const payload = {
      current: SHA,
      versions: [{ sha: SHA, seq: 1, build: '1.4.0-1-gabc', promoted: true }],
    };
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(payload),
    }));
    const result = await fetchVersions();
    expect(result).not.toBeNull();
    expect(result!.current).toBe(SHA);
    expect(result!.versions).toHaveLength(1);
  });

  it('returns null when fetch returns ok but invalid JSON shape', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ current: null, versions: 'not-an-array' }),
    }));
    expect(await fetchVersions()).toBeNull();
  });

  it('returns null when the response is not ok (e.g. 404)', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      json: () => Promise.reject(new Error('should not parse')),
    }));
    expect(await fetchVersions()).toBeNull();
  });

  it('returns null when fetch throws a network error', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('Network failure')));
    expect(await fetchVersions()).toBeNull();
  });

  it('passes the correct URL and cache option to fetch', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ current: null, versions: [] }),
    });
    vi.stubGlobal('fetch', mockFetch);
    await fetchVersions();
    expect(mockFetch).toHaveBeenCalledWith('/versions.json', { cache: 'no-cache' });
  });
});

// ---------------------------------------------------------------------------
// buildVersionList — additional edge cases
// ---------------------------------------------------------------------------

const makeEntry = (seq: number, opts: Record<string, unknown> = {}) => ({
  sha: `${seq}`.repeat(40).slice(0, 40),
  seq,
  build: `1.4.0-${seq}-gabc`,
  promoted: false,
  pushedAt: '2026-01-01T10:00:00Z',
  ...opts,
});

describe('buildVersionList — isViewed flag', () => {
  it('marks the viewed sha as isViewed=true', () => {
    const e2 = makeEntry(2);
    const data = { current: null, versions: [makeEntry(1), e2] };
    const list = buildVersionList(data, e2.sha);
    const viewed = list.find((v) => v.isViewed);
    expect(viewed?.seq).toBe(2);
  });

  it('marks no entry as isViewed when viewedSha is null', () => {
    const data = { current: null, versions: [makeEntry(1), makeEntry(2)] };
    const list = buildVersionList(data, null);
    expect(list.every((v) => !v.isViewed)).toBe(true);
  });
});

describe('buildVersionList — dateLabel', () => {
  it('uses promotedAt for a promoted version', () => {
    const e = makeEntry(1, {
      promoted: true,
      promotedAt: '2026-03-15T00:00:00Z',
      pushedAt: '2026-03-10T00:00:00Z',
    });
    const list = buildVersionList({ current: null, versions: [e] }, null);
    // dateLabel comes from promotedAt for promoted entries.
    // We just verify it's non-empty and does not equal the pushedAt label.
    const promotedDate = new Date('2026-03-15T00:00:00Z').toLocaleDateString();
    const pushedDate = new Date('2026-03-10T00:00:00Z').toLocaleDateString();
    expect(list[0].dateLabel).toBe(promotedDate);
    expect(list[0].dateLabel).not.toBe(pushedDate);
  });

  it('uses pushedAt for a non-promoted version', () => {
    const e = makeEntry(1, {
      promoted: false,
      pushedAt: '2026-02-20T00:00:00Z',
      promotedAt: '2099-01-01T00:00:00Z', // should be ignored
    });
    const list = buildVersionList({ current: null, versions: [e] }, null);
    const expected = new Date('2026-02-20T00:00:00Z').toLocaleDateString();
    expect(list[0].dateLabel).toBe(expected);
  });

  it('returns empty string for missing date fields', () => {
    const e = makeEntry(1, { promoted: false, pushedAt: undefined });
    const list = buildVersionList({ current: null, versions: [e] }, null);
    expect(list[0].dateLabel).toBe('');
  });
});

describe('buildVersionList — href and shortsha', () => {
  it('constructs the correct href from the sha', () => {
    const e = makeEntry(1);
    const list = buildVersionList({ current: null, versions: [e] }, null);
    expect(list[0].href).toBe(`/c/${e.sha}/`);
  });

  it('uses the provided shortsha when available', () => {
    const e = makeEntry(1, { shortsha: 'abcdef1' });
    const list = buildVersionList({ current: null, versions: [e] }, null);
    expect(list[0].shortsha).toBe('abcdef1');
  });

  it('falls back to the first 7 chars of sha when shortsha is absent', () => {
    const e = makeEntry(1); // no shortsha
    const list = buildVersionList({ current: null, versions: [e] }, null);
    expect(list[0].shortsha).toBe(e.sha.slice(0, 7));
  });
});

describe('buildVersionList — targetRelease', () => {
  it('passes through targetRelease when present', () => {
    const e = makeEntry(1, { targetRelease: 'v1.4.0' });
    const list = buildVersionList({ current: null, versions: [e] }, null);
    expect(list[0].targetRelease).toBe('v1.4.0');
  });

  it('defaults targetRelease to empty string when absent', () => {
    const list = buildVersionList({ current: null, versions: [makeEntry(1)] }, null);
    expect(list[0].targetRelease).toBe('');
  });
});
