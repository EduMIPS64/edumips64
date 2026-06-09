// Utilities for browsing the web UI's version history.
//
// The production deployment accumulates immutable snapshots at /v/<n>/.
// A root /manifest.json describes all retained versions.

/**
 * Given a window.location-like object, return the integer version number <n>
 * if the path is under /v/<n>/; otherwise return null (root = current).
 *
 * @param {{pathname?: string}} [loc] - Optional location object.
 *   Defaults to window.location when running in a browser.
 * @returns {number|null}
 */
export function getViewedVersion(loc) {
  const location =
    loc || (typeof window !== 'undefined' ? window.location : null);
  if (!location) {
    return null;
  }
  const pathname = location.pathname || '';
  const match = pathname.match(/^\/v\/(\d+)(?:\/|$)/);
  if (!match) {
    return null;
  }
  return parseInt(match[1], 10);
}

/**
 * Strict shape check for a manifest object.
 *
 * @param {unknown} manifest
 * @returns {boolean}
 */
export function isValidManifest(manifest) {
  if (!manifest || typeof manifest !== 'object') {
    return false;
  }
  if (!Number.isInteger(manifest.current) || manifest.current < 1) {
    return false;
  }
  if (!Array.isArray(manifest.history)) {
    return false;
  }
  return manifest.history.every(
    (entry) =>
      entry &&
      typeof entry === 'object' &&
      Number.isInteger(entry.n) &&
      typeof entry.build === 'string',
  );
}

/**
 * Build a sorted (descending by n) list of version items for display.
 *
 * @param {object} manifest - Parsed manifest object.
 * @param {number|null} viewedN - The version being viewed, or null for root.
 * @returns {Array<{n: number, build: string, dateLabel: string, targetRelease: string, href: string, isCurrent: boolean, isViewed: boolean}>}
 */
export function buildVersionList(manifest, viewedN) {
  if (!isValidManifest(manifest)) {
    return [];
  }
  const sorted = [...manifest.history].sort((a, b) => b.n - a.n);
  return sorted.map((entry) => {
    let dateLabel = '';
    if (entry.promotedAt) {
      try {
        const d = new Date(entry.promotedAt);
        dateLabel = isNaN(d.getTime()) ? '' : d.toLocaleDateString();
      } catch {
        dateLabel = '';
      }
    }
    return {
      n: entry.n,
      build: entry.build,
      dateLabel,
      targetRelease: entry.targetRelease || '',
      href: '/v/' + entry.n + '/',
      isCurrent: entry.n === manifest.current,
      isViewed: entry.n === viewedN,
    };
  });
}

/**
 * Fetch and validate the root manifest.json.
 *
 * @returns {Promise<object|null>} Parsed manifest if valid, null otherwise.
 */
export async function fetchManifest() {
  try {
    const resp = await fetch('/manifest.json', { cache: 'no-cache' });
    if (!resp.ok) {
      return null;
    }
    const data = await resp.json();
    return isValidManifest(data) ? data : null;
  } catch {
    return null;
  }
}
