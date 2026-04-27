// Utilities to classify the running web build based on the URL it is served
// from. The web UI is deployed to three kinds of locations:
//
//   * Production:   https://web.edumips.org/
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

/**
 * Classify a `window.location`-like object.
 *
 * @param {{hostname?: string, pathname?: string}} [loc] Optional location
 *   object. Defaults to `window.location` when running in a browser.
 * @returns {{kind: 'production'|'pr'|'dev', prNumber: number|null, prUrl: string|null}}
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
