// Utilities to classify the running web build based on the URL it is served
// from. The web UI is deployed to these kinds of locations:
//
//   * Production:   https://web.edumips.org/
//   * Archived/candidate builds:
//                   https://web.edumips.org/c/<full-sha>/
//                   (every master build is pushed here; whether it is a
//                    promoted archive or an unpromoted candidate is resolved
//                    against /versions.json, not from the URL)
//   * PR previews:  https://edumips64ci.z16.web.core.windows.net/<PR_NUMBER>/
//                   (see .github/workflows/ci.yml, deploy-staging job)
//   * Local dev:    http://localhost:8080/, file://, etc.
//
// `getBuildInfo` returns a structured description of the current environment
// so that the UI can clearly tell users which version they are looking at
// and, for PR builds, link back to the originating pull request on GitHub.

const PROD_HOSTNAME = 'web.edumips.org';
const CI_HOSTNAME = 'edumips64ci.z16.web.core.windows.net';
const PR_REPO_URL = 'https://github.com/EduMIPS64/edumips64/pull';

// A build served from /c/<full-sha>/ (40 hex chars).
const BUILD_PATH_RE = /^\/c\/([0-9a-f]{40})\//;

/**
 * Classify a `window.location`-like object.
 *
 * The `kind` is derived purely from the URL:
 *   - 'production'   served from the Pages root (the current promoted build)
 *   - 'archive-build' served from /c/<sha>/ (a candidate OR a promoted archive;
 *                     the distinction is resolved later against versions.json)
 *   - 'pr'           a pull-request preview build
 *   - 'dev'          anything else (localhost, file://, etc.)
 *
 * @param {{hostname?: string, pathname?: string}} [loc] Optional location
 *   object. Defaults to `window.location` when running in a browser.
 * @returns {{kind: 'production'|'archive-build'|'pr'|'dev', prNumber: number|null, prUrl: string|null, sha?: string, buildUrl?: string}}
 */
export function getBuildInfo(loc) {
  const location =
    loc || (typeof window !== 'undefined' ? window.location : null);
  if (!location) {
    return { kind: 'dev', prNumber: null, prUrl: null };
  }

  const hostname = (location.hostname || '').toLowerCase();
  const pathname = location.pathname || '';

  if (hostname === PROD_HOSTNAME) {
    const buildMatch = pathname.match(BUILD_PATH_RE);
    if (buildMatch) {
      return {
        kind: 'archive-build',
        prNumber: null,
        prUrl: null,
        sha: buildMatch[1],
        buildUrl: `https://web.edumips.org${buildMatch[0]}`,
      };
    }
    return { kind: 'production', prNumber: null, prUrl: null };
  }

  if (hostname === CI_HOSTNAME) {
    // The PR number is the first path segment, e.g. "/123/index.html".
    const match = pathname.match(/^\/(\d+)(?:\/|$)/);
    if (match) {
      const prNumber = parseInt(match[1], 10);
      return {
        kind: 'pr',
        prNumber,
        prUrl: `${PR_REPO_URL}/${prNumber}`,
      };
    }
  }

  return { kind: 'dev', prNumber: null, prUrl: null };
}
