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
#   Phase 1 — Poll the "Deploy to GitHub Pages" workflow runs in the
#   web.edumips.org repo (Actions-based Pages deployment since
#   EduMIPS64/edumips64#1913; the legacy /pages/builds API no longer
#   reflects deploys) until a run for the commit we just pushed completes
#   successfully.  If no run appears, or the run fails, re-dispatch the
#   workflow once via workflow_dispatch — the modern equivalent of the old
#   "re-request a Pages build" recovery that fixed the 2026-07-02 incident.
#   Phase 2 — Once the deploy run succeeded, poll
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

ACTIONS_API_BASE="https://api.github.com/repos/EduMIPS64/web.edumips.org/actions"
DEPLOY_WORKFLOW="deploy-pages.yml"
CDN_UI_JS="https://web.edumips.org/ui.js"

# Phase 1: up to 10 minutes for the deploy workflow to complete. Actions
# deploys of this site take ~1-2 minutes (vs >15 min for the old legacy
# Jekyll builds); 10 minutes leaves room for runner queueing.
BUILD_TIMEOUT=600
# Re-dispatch the deploy workflow if no run has appeared after this long.
REDISPATCH_AFTER=180
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
# Phase 1: Wait for the deploy workflow run for our commit to succeed.
# ---------------------------------------------------------------------------
echo "=== Phase 1: Waiting for the Pages deploy workflow (up to $((BUILD_TIMEOUT / 60)) min) ==="

BUILD_DEADLINE=$(( $(date +%s) + BUILD_TIMEOUT ))
REDISPATCH_AT=$(( $(date +%s) + REDISPATCH_AFTER ))
TRIGGERED_REDISPATCH=false

redispatch() {
    if [[ "${TRIGGERED_REDISPATCH}" == "false" ]]; then
        echo "  -> Re-dispatching the deploy workflow ..."
        gh_api -X POST \
            -H "Content-Type: application/json" \
            -d '{"ref":"master"}' \
            "${ACTIONS_API_BASE}/workflows/${DEPLOY_WORKFLOW}/dispatches" > /dev/null \
            && TRIGGERED_REDISPATCH=true \
            || echo "  -> WARNING: re-dispatch failed (missing actions scope?); continuing to poll."
    fi
}

while true; do
    NOW=$(date +%s)
    if (( NOW >= BUILD_DEADLINE )); then
        echo ""
        echo "ERROR: Timed out waiting for the Pages deploy workflow after $((BUILD_TIMEOUT / 60)) minutes."
        echo ""
        echo "Manual recovery — re-dispatch the deploy workflow:"
        echo "  gh workflow run ${DEPLOY_WORKFLOW} --repo EduMIPS64/web.edumips.org --ref master"
        exit 1
    fi

    # All runs of the deploy workflow for the commit we just pushed
    # (push-triggered and manually dispatched runs both carry head_sha).
    RUNS_JSON=$(gh_api "${ACTIONS_API_BASE}/workflows/${DEPLOY_WORKFLOW}/runs?head_sha=${EXPECTED_SHA}&per_page=10" 2>/dev/null || echo '{}')
    read -r RUN_STATUS RUN_CONCLUSION <<< "$(echo "${RUNS_JSON}" | python3 -c "
import json, sys
try:
    d = json.load(sys.stdin)
    runs = d.get('workflow_runs') or []
except Exception:
    runs = []
if not runs:
    print('absent none')
else:
    # Most recent run for this sha wins.
    r = runs[0]
    print(r.get('status') or 'unknown', r.get('conclusion') or 'none')
" 2>/dev/null || echo "unknown none")"

    echo "[$(date -u '+%H:%M:%S')] deploy run status=${RUN_STATUS} conclusion=${RUN_CONCLUSION} sha=${EXPECTED_SHA:0:8}"

    if [[ "${RUN_STATUS}" == "completed" && "${RUN_CONCLUSION}" == "success" ]]; then
        echo ""
        echo "Deploy workflow succeeded for commit ${EXPECTED_SHA:0:8}."
        break
    elif [[ "${RUN_STATUS}" == "completed" ]]; then
        # failure / cancelled / timed_out: try one re-dispatch, keep polling.
        echo "  -> Deploy run concluded '${RUN_CONCLUSION}'."
        redispatch
    elif [[ "${RUN_STATUS}" == "absent" ]] && (( NOW >= REDISPATCH_AT )); then
        # Push-triggered run never appeared (e.g. workflow file raced the
        # build_type flip): kick one off manually.
        echo "  -> No deploy run found for this commit yet."
        redispatch
    fi
    # queued / in_progress / transient API errors: just keep waiting.

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
