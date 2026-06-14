#!/usr/bin/env bash
#
# Self-healing, one-shot migration of the web.edumips.org Pages repo from the
# legacy layout (manifest.json + candidates.json + /v/<n>/ + date dirs + prev/)
# to the unified layout (versions.json + /c/<sha>/). See
# docs/design/unified-web-versioning.md.
#
# It is idempotent and safe to call from every Pages-writing workflow
# (push-web, promote-web, rollback-web):
#   - If versions.json already exists, the repo is migrated; do nothing.
#   - If there are no legacy index files either, it is a fresh repo; do nothing
#     (the caller's push/promote will create versions.json on its own).
#   - Otherwise run `deploy-web-pages.py migrate` once and push the result so
#     the very next push/promote/rollback operates on the unified layout.
#
# Usage: migrate-pages-if-needed.sh <pages-repo-dir> <edumips64-repo-dir>
#   <pages-repo-dir>     working clone of EduMIPS64/web.edumips.org (push remote
#                        already authenticated)
#   <edumips64-repo-dir> full-history checkout of this repo, used by `migrate`
#                        to compute seq = `git rev-list --count <sha>`
set -euo pipefail

PAGES_DIR="${1:?pages repo dir required}"
EDUMIPS_DIR="${2:?edumips64 repo dir required}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "${PAGES_DIR}/versions.json" ]]; then
  echo "versions.json present — Pages repo already on the unified layout."
  exit 0
fi

if [[ ! -f "${PAGES_DIR}/manifest.json" && ! -f "${PAGES_DIR}/candidates.json" ]]; then
  echo "No versions.json and no legacy index files — fresh repo, nothing to migrate."
  exit 0
fi

echo "Legacy layout detected (no versions.json) — running one-shot migration."
cp "${SCRIPT_DIR}/deploy-web-pages.py" "${PAGES_DIR}/"
( cd "${PAGES_DIR}" && python3 deploy-web-pages.py migrate --repo "${EDUMIPS_DIR}" )
rm -f "${PAGES_DIR}/deploy-web-pages.py"

cd "${PAGES_DIR}"
git add -A
if git diff --cached --quiet; then
  echo "Migration produced no changes."
  exit 0
fi
git commit -m "Migrate web.edumips.org to unified versioning layout"
git push origin master
echo "Pushed unified-versioning migration."
