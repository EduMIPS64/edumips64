// Utilities for browsing the web UI's unified version history.
//
// Every master build is pushed to an immutable directory at /c/<full-sha>/.
// A single root /versions.json indexes all retained builds:
//
//   {
//     "current": "<sha of the live promoted build>",
//     "versions": [ { sha, shortsha, seq, build, targetRelease,
//                     pushedAt, promoted, promotedAt?, promotedBy? }, ... ]
//   }
//
// Retained builds are either *promoted* (shown prominently in About) or
// pending *candidates* (newer than the live build, shown less prominently).

// A build is served from /c/<full-sha>/ (40 hex chars).
const BUILD_PATH_RE = /^\/c\/([0-9a-f]{40})(?:\/|$)/;

/** Location-like object accepted for testability. */
interface LocationLike {
  pathname?: string;
}

/** A single entry in versions.json's `versions` array. */
interface VersionEntry {
  sha: string;
  shortsha?: string;
  seq: number;
  build: string;
  targetRelease?: string;
  pushedAt?: string;
  promoted: boolean;
  promotedAt?: string;
  promotedBy?: string;
  /**
   * True when the retention policy deleted this version's c/<sha>/ snapshot
   * (deploy-web-pages.py keeps the entry for the audit trail). Pruned
   * versions have no content to link to and cannot be promoted or rolled
   * back to.
   */
  pruned?: boolean;
}

/** The shape of a parsed versions.json file. */
interface VersionsIndex {
  current: string | null;
  versions: VersionEntry[];
}

/** A display-ready version item produced by buildVersionList. */
export interface VersionItem {
  sha: string;
  shortsha: string;
  seq: number;
  build: string;
  dateLabel: string;
  targetRelease: string;
  href: string;
  promoted: boolean;
  isCurrent: boolean;
  isViewed: boolean;
}

/**
 * Given a window.location-like object, return the full commit SHA if the path
 * is under /c/<sha>/; otherwise return null (root = current production).
 *
 * @param loc Optional location object. Defaults to window.location in a browser.
 */
export function getViewedSha(loc?: LocationLike | null): string | null {
  const location: LocationLike | null =
    loc ?? (typeof window !== 'undefined' ? window.location : null);
  if (!location) {
    return null;
  }
  const match = (location.pathname ?? '').match(BUILD_PATH_RE);
  return match ? match[1] : null;
}

/**
 * Strict shape check for a versions index object.
 */
export function isValidVersions(data: unknown): data is VersionsIndex {
  if (!data || typeof data !== 'object') {
    return false;
  }
  const d = data as Record<string, unknown>;
  if (!Array.isArray(d.versions)) {
    return false;
  }
  // `current` may be null (no production build yet) or a non-empty string.
  if (d.current != null && typeof d.current !== 'string') {
    return false;
  }
  return (d.versions as unknown[]).every(
    (entry) =>
      entry &&
      typeof entry === 'object' &&
      typeof (entry as Record<string, unknown>).sha === 'string' &&
      Number.isInteger((entry as Record<string, unknown>).seq) &&
      typeof (entry as Record<string, unknown>).build === 'string' &&
      typeof (entry as Record<string, unknown>).promoted === 'boolean',
  );
}

function toDateLabel(iso: string | undefined): string {
  if (!iso) {
    return '';
  }
  try {
    const d = new Date(iso);
    return isNaN(d.getTime()) ? '' : d.toLocaleDateString();
  } catch {
    return '';
  }
}

/**
 * Build a sorted (descending by seq) list of version items for display.
 *
 * Each item is tagged `promoted` (a manually-promoted version, shown
 * prominently) or not (a pending candidate). `isCurrent` marks the live build.
 *
 * @param data       Parsed versions.json object.
 * @param viewedSha  The SHA being viewed, or null for root.
 */
export function buildVersionList(data: unknown, viewedSha: string | null): VersionItem[] {
  if (!isValidVersions(data)) {
    return [];
  }
  // Pruned versions have no snapshot on disk: linking to them would 404 and
  // they are not valid rollback/promote targets, so they are not displayed.
  const sorted = [...data.versions]
    .filter((entry) => entry.pruned !== true)
    .sort((a, b) => b.seq - a.seq);
  return sorted.map((entry) => ({
    sha: entry.sha,
    shortsha: entry.shortsha ?? entry.sha.slice(0, 7),
    seq: entry.seq,
    build: entry.build,
    dateLabel: toDateLabel(entry.promoted ? entry.promotedAt : entry.pushedAt),
    targetRelease: entry.targetRelease ?? '',
    href: '/c/' + entry.sha + '/',
    promoted: entry.promoted === true,
    isCurrent: entry.sha === data.current,
    isViewed: viewedSha != null && entry.sha === viewedSha,
  }));
}

/**
 * Fetch and validate the root versions.json.
 *
 * @returns Parsed index if valid, null otherwise.
 */
export async function fetchVersions(): Promise<VersionsIndex | null> {
  try {
    const resp = await fetch('/versions.json', { cache: 'no-cache' });
    if (!resp.ok) {
      return null;
    }
    const data: unknown = await resp.json();
    return isValidVersions(data) ? data : null;
  } catch {
    return null;
  }
}
