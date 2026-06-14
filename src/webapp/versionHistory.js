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

/**
 * Given a window.location-like object, return the full commit SHA if the path
 * is under /c/<sha>/; otherwise return null (root = current production).
 *
 * @param {{pathname?: string}} [loc] - Optional location object.
 *   Defaults to window.location when running in a browser.
 * @returns {string|null}
 */
export function getViewedSha(loc) {
  const location =
    loc || (typeof window !== 'undefined' ? window.location : null);
  if (!location) {
    return null;
  }
  const match = (location.pathname || '').match(BUILD_PATH_RE);
  return match ? match[1] : null;
}

/**
 * Strict shape check for a versions index object.
 *
 * @param {unknown} data
 * @returns {boolean}
 */
export function isValidVersions(data) {
  if (!data || typeof data !== 'object') {
    return false;
  }
  if (!Array.isArray(data.versions)) {
    return false;
  }
  // `current` may be null (no production build yet) or a non-empty string.
  if (data.current != null && typeof data.current !== 'string') {
    return false;
  }
  return data.versions.every(
    (entry) =>
      entry &&
      typeof entry === 'object' &&
      typeof entry.sha === 'string' &&
      Number.isInteger(entry.seq) &&
      typeof entry.build === 'string' &&
      typeof entry.promoted === 'boolean',
  );
}

function toDateLabel(iso) {
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
 * @param {object} data - Parsed versions.json object.
 * @param {string|null} viewedSha - The SHA being viewed, or null for root.
 * @returns {Array<{sha: string, shortsha: string, seq: number, build: string, dateLabel: string, targetRelease: string, href: string, promoted: boolean, isCurrent: boolean, isViewed: boolean}>}
 */
export function buildVersionList(data, viewedSha) {
  if (!isValidVersions(data)) {
    return [];
  }
  const sorted = [...data.versions].sort((a, b) => b.seq - a.seq);
  return sorted.map((entry) => ({
    sha: entry.sha,
    shortsha: entry.shortsha || entry.sha.slice(0, 7),
    seq: entry.seq,
    build: entry.build,
    dateLabel: toDateLabel(entry.promoted ? entry.promotedAt : entry.pushedAt),
    targetRelease: entry.targetRelease || '',
    href: '/c/' + entry.sha + '/',
    promoted: entry.promoted === true,
    isCurrent: entry.sha === data.current,
    isViewed: viewedSha != null && entry.sha === viewedSha,
  }));
}

/**
 * Fetch and validate the root versions.json.
 *
 * @returns {Promise<object|null>} Parsed index if valid, null otherwise.
 */
export async function fetchVersions() {
  try {
    const resp = await fetch('/versions.json', { cache: 'no-cache' });
    if (!resp.ok) {
      return null;
    }
    const data = await resp.json();
    return isValidVersions(data) ? data : null;
  } catch {
    return null;
  }
}
