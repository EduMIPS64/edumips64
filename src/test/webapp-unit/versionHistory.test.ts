import { describe, expect, it } from 'vitest';
import { buildVersionList } from '../../webapp/versionHistory';

const entry = (n: number, opts: Partial<Record<string, unknown>> = {}) => ({
  sha: `${n}`.repeat(40).slice(0, 40),
  seq: n,
  build: `1.4.0-${n}-gabc`,
  promoted: false,
  pushedAt: '2026-07-01T00:00:00Z',
  ...opts,
});

const index = (versions: unknown[], current: string | null = null) => ({
  current,
  versions,
});

describe('buildVersionList', () => {
  it('returns entries sorted by seq descending', () => {
    const list = buildVersionList(index([entry(1), entry(3), entry(2)]), null);
    expect(list.map((v) => v.seq)).toEqual([3, 2, 1]);
  });

  it('excludes pruned versions (their snapshot no longer exists)', () => {
    // Regression: the retention policy marks old promoted versions with
    // pruned=true and deletes their c/<sha>/ snapshot; the About tab used
    // to render 404 links for them.
    const list = buildVersionList(
      index([
        entry(3, { promoted: true }),
        entry(2, { promoted: true, pruned: true }),
        entry(1, { promoted: true, pruned: false }),
      ]),
      null,
    );
    expect(list.map((v) => v.seq)).toEqual([3, 1]);
  });

  it('flags the current version', () => {
    const cur = entry(2, { promoted: true });
    const list = buildVersionList(
      index([entry(1), cur], cur.sha as string),
      null,
    );
    expect(list.find((v) => v.isCurrent)?.seq).toBe(2);
  });

  it('returns [] for invalid data', () => {
    expect(buildVersionList(null, null)).toEqual([]);
    expect(buildVersionList({ versions: 'nope' }, null)).toEqual([]);
  });
});
