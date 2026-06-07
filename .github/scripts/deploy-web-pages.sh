#!/usr/bin/env bash
# deploy-web-pages.sh — Pages-repo layout manager for web.edumips.org
# Subcommands: promote | rollback | nightly
# Must be run with cwd = checkout of the Pages repo (EduMIPS64/web.edumips.org).
set -euo pipefail

# ---------------------------------------------------------------------------
# Reserved top-level names that are NEVER production files.
# ---------------------------------------------------------------------------
RESERVED_NAMES=(v prev nightly manifest.json CNAME .nojekyll .git)

# Max number of /v/<N> snapshot dirs to keep.
MAX_VERSIONS=50

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

usage() {
  echo "Usage:"
  echo "  $0 promote <artifact_dir> <build_string> <sha> <target_release> <actor>"
  echo "  $0 rollback <actor>"
  echo "  $0 nightly <artifact_dir>"
  exit 1
}

die() {
  echo "ERROR: $*" >&2
  exit 1
}

# Build a bash pattern for reserved names used with extglob.
# Returns something like: !(v|prev|nightly|manifest.json|CNAME|.nojekyll|.git)
reserved_extglob() {
  local IFS='|'
  echo "!(${RESERVED_NAMES[*]})"
}

# Ensure CNAME and .nojekyll exist and are correct.
ensure_static_files() {
  if [[ ! -f CNAME ]]; then
    echo "web.edumips.org" > CNAME
    echo "Created missing CNAME"
  fi
  if [[ ! -f .nojekyll ]]; then
    touch .nojekyll
    echo "Created missing .nojekyll"
  fi
}

# Delete all root-level production files, leaving reserved names untouched.
delete_root_prod_files() {
  shopt -s extglob dotglob nullglob
  local pattern
  pattern=$(reserved_extglob)
  # shellcheck disable=SC2086,SC2206
  local files=( $pattern )
  for f in "${files[@]}"; do
    rm -rf -- "$f"
  done
  shopt -u extglob dotglob nullglob
}

# Copy src_dir contents into dest_dir, replacing dest_dir first.
# dest_dir is a subdirectory of the Pages repo (safe: not root).
replace_subdir() {
  local src="$1" dest="$2"
  rm -rf -- "${dest:?}"
  mkdir -p "$dest"
  cp -a "$src/." "$dest/"
}

# Prune /v/ keeping only the highest MAX_VERSIONS numbered dirs.
prune_versions() {
  local v_dir="v"
  [[ -d "$v_dir" ]] || return 0

  # Collect numeric dirs, sort descending.
  local dirs=()
  while IFS= read -r d; do
    dirs+=("$d")
  done < <(
    find "$v_dir" -mindepth 1 -maxdepth 1 -type d \
      | grep -E '/[0-9]+$' \
      | sed "s|^$v_dir/||" \
      | sort -rn
  )

  local count="${#dirs[@]}"
  if (( count > MAX_VERSIONS )); then
    local i=0
    # Lowest-numbered dirs are at the end after descending sort.
    for d in "${dirs[@]}"; do
      if (( i >= MAX_VERSIONS )); then
        echo "Pruning v/$d"
        rm -rf -- "${v_dir:?}/${d:?}"
      fi
      (( i++ ))
    done
  fi
}

# Read current promotion counter from manifest.json; echo 0 if absent.
read_current_from_manifest() {
  if [[ -f manifest.json ]]; then
    python3 -c "import json,sys; d=json.load(open('manifest.json')); print(d.get('current',0))"
  else
    echo 0
  fi
}

utc_now() {
  date -u +"%Y-%m-%dT%H:%M:%SZ"
}

# ---------------------------------------------------------------------------
# Subcommand: promote
# ---------------------------------------------------------------------------
cmd_promote() {
  [[ $# -ge 5 ]] || die "promote requires: <artifact_dir> <build_string> <sha> <target_release> <actor>"
  local artifact_dir="$1" build_string="$2" sha="$3" target_release="$4" actor="$5"

  [[ -d "$artifact_dir" ]] || die "Artifact dir not found: $artifact_dir"

  # Guard: die if the artifact's top-level entries contain any reserved name.
  # This prevents an artifact from clobbering the Pages layout (v/, prev/, etc.).
  echo "Checking artifact for reserved top-level names..."
  shopt -s dotglob nullglob
  local entry_name reserved
  for entry in "$artifact_dir"/*; do
    entry_name="$(basename -- "$entry")"
    for reserved in "${RESERVED_NAMES[@]}"; do
      [[ "$entry_name" == "$reserved" ]] \
        && die "Artifact contains reserved top-level name '${entry_name}' — cannot promote."
    done
  done
  shopt -u dotglob nullglob

  local is_first_run=false
  local current_n
  if [[ ! -f manifest.json ]]; then
    is_first_run=true
    current_n=0
    echo "First run detected: manifest.json absent."
    # On first run, copy existing root prod files into prev/ so rollback works immediately.
    echo "Seeding prev/ from current root prod files (first-run bootstrap)..."
    shopt -s extglob dotglob nullglob
    local pattern
    pattern=$(reserved_extglob)
    # shellcheck disable=SC2086,SC2206
    local existing_root=( $pattern )
    if [[ ${#existing_root[@]} -gt 0 ]]; then
      rm -rf prev
      mkdir -p prev
      for f in "${existing_root[@]}"; do
        cp -a -- "$f" prev/
      done
      echo "Seeded prev/ with ${#existing_root[@]} root item(s)."
    else
      echo "Root has no prod files yet; prev/ will be empty on first promotion."
      mkdir -p prev
    fi
    shopt -u extglob dotglob nullglob
  else
    current_n=$(read_current_from_manifest)
  fi

  local new_n=$(( current_n + 1 ))
  echo "Promoting: N=${current_n} -> newN=${new_n}, build=${build_string}, sha=${sha}"

  if [[ "$is_first_run" == "false" ]]; then
    # Copy existing root prod files into prev/ (replace prev — rollback target).
    echo "Archiving current root into prev/..."
    shopt -s extglob dotglob nullglob
    local pattern
    pattern=$(reserved_extglob)
    # shellcheck disable=SC2086,SC2206
    local root_files=( $pattern )
    rm -rf prev
    mkdir -p prev
    for f in "${root_files[@]}"; do
      cp -a -- "$f" prev/
    done
    shopt -u extglob dotglob nullglob
    echo "prev/ updated."
  fi

  # Copy artifact into v/<newN>/ (immutable snapshot).
  echo "Creating immutable snapshot at v/${new_n}/..."
  mkdir -p "v/${new_n}"
  cp -a "$artifact_dir/." "v/${new_n}/"

  # Replace root production files with artifact contents.
  echo "Replacing root prod files..."
  delete_root_prod_files
  cp -a "$artifact_dir/." ./

  # Write manifest.json.
  local promoted_at
  promoted_at=$(utc_now)
  python3 - <<PYEOF
import json
manifest = {
    "current": ${new_n},
    "prev": ${current_n},
    "sha": "${sha}",
    "build": "${build_string}",
    "targetRelease": "${target_release}",
    "promotedAt": "${promoted_at}",
    "promotedBy": "${actor}"
}
with open("manifest.json", "w") as f:
    json.dump(manifest, f, indent=2)
    f.write("\n")
PYEOF
  echo "manifest.json written."

  # Prune old /v/ snapshots.
  prune_versions

  # Ensure CNAME and .nojekyll are present.
  ensure_static_files

  echo "Promote complete: v${new_n} @ ${sha} by ${actor}"
}

# ---------------------------------------------------------------------------
# Subcommand: rollback
# ---------------------------------------------------------------------------
cmd_rollback() {
  [[ $# -ge 1 ]] || die "rollback requires: <actor>"
  local actor="$1"

  [[ -f manifest.json ]] || die "manifest.json not found — nothing to roll back to."
  [[ -d prev ]] || die "prev/ directory not found — no previous build to roll back to."

  # Verify prev/ is non-empty before any destructive operation.
  shopt -s dotglob nullglob
  local prev_check=( prev/* )
  shopt -u dotglob nullglob
  [[ ${#prev_check[@]} -gt 0 ]] || die "prev/ is empty — cannot roll back."

  local current_n prev_n
  current_n=$(python3 -c "import json; d=json.load(open('manifest.json')); print(d['current'])")
  prev_n=$(python3 -c "import json; d=json.load(open('manifest.json')); print(d.get('prev', 0))")

  echo "Rolling back: swapping root (v${current_n}) <-> prev (v${prev_n}), actor=${actor}"

  # ---- STEP 1: Collect current root prod files BEFORE creating any staging dir.
  shopt -s extglob dotglob nullglob
  local pattern
  pattern=$(reserved_extglob)
  # shellcheck disable=SC2086,SC2206
  local root_files=( $pattern )
  shopt -u extglob dotglob nullglob

  # ---- STEP 2: Stage root prod files in a temporary directory.
  local swap_dir=".rollback-swap"
  rm -rf -- "$swap_dir"
  mkdir -p "$swap_dir"
  for f in "${root_files[@]}"; do
    mv -- "$f" "$swap_dir/"
  done

  # ---- STEP 3: Move prev/ contents into root.
  echo "Moving prev/ into root..."
  shopt -s dotglob nullglob
  local prev_files=( prev/* )
  shopt -u dotglob nullglob
  for f in "${prev_files[@]}"; do
    mv -- "$f" ./
  done

  # ---- STEP 4: Move former root (from swap_dir) into prev/, replacing it.
  echo "Moving former root into prev/..."
  rm -rf -- prev
  mkdir -p prev
  shopt -s dotglob nullglob
  local swap_files=( "$swap_dir"/* )
  shopt -u dotglob nullglob
  for f in "${swap_files[@]}"; do
    mv -- "$f" prev/
  done

  rm -rf -- "$swap_dir"

  # ---- STEP 5: Swap manifest current <-> prev so a second rollback re-applies this one.
  local rolled_back_at
  rolled_back_at=$(utc_now)
  python3 - <<PYEOF
import json
with open("manifest.json") as f:
    manifest = json.load(f)
old_current = manifest["current"]
old_prev = manifest.get("prev", 0)
manifest["current"] = old_prev
manifest["prev"] = old_current
manifest["promotedAt"] = "${rolled_back_at}"
manifest["promotedBy"] = "${actor}"
manifest["rolledBackFrom"] = old_current
manifest["note"] = "rollback"
with open("manifest.json", "w") as f:
    json.dump(manifest, f, indent=2)
    f.write("\n")
PYEOF
  echo "manifest.json updated (swap: current=v${prev_n}, prev=v${current_n})."

  ensure_static_files

  echo "Rollback complete: root ↔ prev swap (v${current_n} ↔ v${prev_n}) by ${actor}"
}

# ---------------------------------------------------------------------------
# Subcommand: nightly
# ---------------------------------------------------------------------------
cmd_nightly() {
  [[ $# -ge 1 ]] || die "nightly requires: <artifact_dir>"
  local artifact_dir="$1"

  [[ -d "$artifact_dir" ]] || die "Artifact dir not found: $artifact_dir"

  echo "Deploying nightly from ${artifact_dir}..."
  replace_subdir "$artifact_dir" "nightly"

  ensure_static_files

  echo "nightly/ updated."
}

# ---------------------------------------------------------------------------
# Dispatch
# ---------------------------------------------------------------------------
[[ $# -ge 1 ]] || usage
subcmd="$1"
shift

case "$subcmd" in
  promote)  cmd_promote  "$@" ;;
  rollback) cmd_rollback "$@" ;;
  nightly)  cmd_nightly  "$@" ;;
  *) die "Unknown subcommand: $subcmd. Valid: promote | rollback | nightly" ;;
esac
