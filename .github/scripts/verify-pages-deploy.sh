#!/usr/bin/env bash
# verify-pages-deploy.sh — Verify that a GitHub Pages deploy of web.edumips.org
# completed successfully and that the CDN is serving the expected build.
#
# Usage:
#   verify-pages-deploy.sh <expected-build-string> <pages-repo-path>
#
# Arguments:
#   expected-build-string  git-describe style string (e.g. "1.4.0-173-g5e4edfa1")
#                          that must appear in https://web.edumips.org/ui.js
#   pages-repo-path        path to the local Pages repo checkout (used to
#                          determine the commit SHA that was just pushed)
#
# Required environment variable:
#   PAT_WEBUI              GitHub personal access token with read access to
#                          EduMIPS64/web.edumips.org (the default GITHUB_TOKEN
#                          cannot see the Pages build status of another repo)
#
# Exit codes:
#   0  —  build verified: Pages built the right commit AND CDN serves expected version
#   1  —  timeout or unrecoverable error (loud failure with recovery instructions)
#
# Design:
#   Phase 1 — Poll the GitHub Pages build API for web.edumips.org until the
#   latest build has status "built" for the commit we just pushed.  If the
#   build reports "errored", immediately re-request a build via POST (this
#   pattern fixed a real 2026-07-02 incident where a transient queue glitch
#   caused an instant "errored" 0 ms build, silently leaving production stale).
#   Phase 2 — Once the Pages build is confirmed, poll
#   https://web.edumips.org/ui.js?cb=<random> (cache-busting) until it
#   contains the expected build string.  GitHub Pages CDN max-age is 600 s,
#   so we allow up to 12 minutes here.

set -euo pipefail

EXPECTED_BUILD="${1:?Usage: $0 <expected-build-string> <pages-repo-path>}"
PAGES_REPO="${2:?Usage: $0 <expected-build-string> <pages-repo-path>}"

# Validate required env
if [[ -z "${PAT_WEBUI:-}" ]]; then
    echo "ERROR: PAT_WEBUI environment variable is required." >&2
    exit 1
fi

PAGES_API_BASE="https://api.github.com/repos/EduMIPS64/web.edumips.org/pages/builds"
CDN_UI_JS="https://web.edumips.org/ui.js"

# Phase 1: up to 15 minutes for Pages build to complete.
BUILD_TIMEOUT=900
# Phase 2: up to 12 minutes for CDN propagation (max-age is 600 s).
CDN_TIMEOUT=720
# Seconds between poll attempts.
POLL_INTERVAL=15

# ---------------------------------------------------------------------------
# Determine the expected Pages repo HEAD (the commit we just pushed).
# ---------------------------------------------------------------------------
EXPECTED_SHA=$(git -C "${PAGES_REPO}" rev-parse HEAD)
echo "=== Verify Pages Deploy ==="
echo "Expected build string : ${EXPECTED_BUILD}"
echo "Expected Pages HEAD   : ${EXPECTED_SHA}"
echo ""

# ---------------------------------------------------------------------------
# Helper: authenticated GitHub API request.
# ---------------------------------------------------------------------------
gh_api() {
    curl -fsSL \
        -H "Authorization: Bearer ${PAT_WEBUI}" \
        -H "Accept: application/vnd.github+json" \
        -H "X-GitHub-Api-Version: 2022-11-28" \
        "$@"
}

# ---------------------------------------------------------------------------
# Phase 1: Wait for the correct Pages build to finish.
# ---------------------------------------------------------------------------
echo "=== Phase 1: Waiting for GitHub Pages build (up to $((BUILD_TIMEOUT / 60)) min) ==="

BUILD_DEADLINE=$(( $(date +%s) + BUILD_TIMEOUT ))
TRIGGERED_REBUILD=false

while true; do
    NOW=$(date +%s)
    if (( NOW >= BUILD_DEADLINE )); then
        echo ""
        echo "ERROR: Timed out waiting for GitHub Pages build after $((BUILD_TIMEOUT / 60)) minutes."
        echo ""
        echo "Manual recovery — re-request a build:"
        echo "  curl -s -X POST \\"
        echo "    -H 'Authorization: Bearer \$PAT_WEBUI' \\"
        echo "    -H 'Accept: application/vnd.github+json' \\"
        echo "    https://api.github.com/repos/EduMIPS64/web.edumips.org/pages/builds"
        exit 1
    fi

    BUILD_JSON=$(gh_api "${PAGES_API_BASE}/latest" 2>/dev/null || echo '{}')
    STATUS=$(echo "${BUILD_JSON}" | python3 -c \
        "import json,sys; d=json.load(sys.stdin); print(d.get('status','unknown'))" \
        2>/dev/null || echo "unknown")
    COMMIT=$(echo "${BUILD_JSON}" | python3 -c \
        "import json,sys; d=json.load(sys.stdin); print(d.get('commit',''))" \
        2>/dev/null || echo "")

    echo "[$(date -u '+%H:%M:%S')] Pages build status=${STATUS}  commit=${COMMIT:0:8}  want=${EXPECTED_SHA:0:8}"

    if [[ "${STATUS}" == "errored" ]]; then
        # Re-request a build once — this is the fix for the 2026-07-02 incident
        # where a transient GitHub queue glitch caused an instant zero-duration error.
        if [[ "${TRIGGERED_REBUILD}" == "false" ]]; then
            echo "  -> Build errored! Requesting a fresh build via POST ..."
            gh_api -X POST "${PAGES_API_BASE}" > /dev/null
            TRIGGERED_REBUILD=true
            echo "  -> Rebuild requested. Continuing to poll..."
        else
            echo "  -> Build errored again after rebuild; continuing to poll..."
        fi
    elif [[ "${STATUS}" == "built" && "${COMMIT}" == "${EXPECTED_SHA}" ]]; then
        echo ""
        echo "Pages build complete for commit ${EXPECTED_SHA:0:8}."
        break
    elif [[ "${STATUS}" == "built" && "${COMMIT}" != "${EXPECTED_SHA}" ]]; then
        echo "  -> Built, but for wrong commit (${COMMIT:0:8}). Waiting for our build..."
    fi
    # For status "queued" or "building": just keep waiting.

    sleep "${POLL_INTERVAL}"
done

# ---------------------------------------------------------------------------
# Phase 2: Wait for CDN to serve the expected build string in ui.js.
# ---------------------------------------------------------------------------
echo ""
echo "=== Phase 2: Waiting for CDN to serve '${EXPECTED_BUILD}' (up to $((CDN_TIMEOUT / 60)) min) ==="

CDN_DEADLINE=$(( $(date +%s) + CDN_TIMEOUT ))

while true; do
    NOW=$(date +%s)
    if (( NOW >= CDN_DEADLINE )); then
        echo ""
        echo "ERROR: CDN did not serve the expected build '${EXPECTED_BUILD}'"
        echo "within $((CDN_TIMEOUT / 60)) minutes."
        echo ""
        echo "GitHub Pages CDN max-age is 600 s; this suggests a longer propagation"
        echo "delay or the wrong build was deployed."
        echo ""
        echo "Current content of ui.js (first 5 lines):"
        curl -fsSL "${CDN_UI_JS}?cb=${RANDOM}" 2>/dev/null | head -5 || true
        echo ""
        echo "Manual check:"
        echo "  curl -s '${CDN_UI_JS}?cb=\${RANDOM}' | grep -o '${EXPECTED_BUILD}'"
        exit 1
    fi

    CACHE_BUST="${RANDOM}${RANDOM}"
    CONTENT=$(curl -fsSL "${CDN_UI_JS}?cb=${CACHE_BUST}" 2>/dev/null || echo "")

    if echo "${CONTENT}" | grep -qF "${EXPECTED_BUILD}"; then
        echo "[$(date -u '+%H:%M:%S')] CDN is serving expected build: ${EXPECTED_BUILD}"
        echo ""
        echo "=== Deploy verification complete! ==="
        exit 0
    fi

    REMAINING=$(( BUILD_DEADLINE > CDN_DEADLINE ? CDN_DEADLINE : CDN_DEADLINE ))
    REMAINING=$(( CDN_DEADLINE - $(date +%s) ))
    echo "[$(date -u '+%H:%M:%S')] CDN not yet serving '${EXPECTED_BUILD}' (${REMAINING}s remaining). Retrying..."
    sleep "${POLL_INTERVAL}"
done
