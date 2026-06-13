#!/usr/bin/env python3
"""deploy-web-pages.py — Pages-repo layout manager for web.edumips.org.

Subcommands: promote | rollback | candidate
Must be run with cwd = checkout of the Pages repo (EduMIPS64/web.edumips.org).
"""

import argparse
import json
import os
import re
import shutil
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

RESERVED_NAMES = ["v", "prev", "candidates.json", "manifest.json", "CNAME", ".nojekyll", ".git"]
MAX_VERSIONS = 50
DEFAULT_RETENTION_DAYS = 14

_DATE_DIR_RE = re.compile(r"^\d{4}-\d{2}-\d{2}$")

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------


def die(msg: str) -> None:
    """Print an error message to stderr and exit 1 (mirrors bash `die`)."""
    print(f"ERROR: {msg}", file=sys.stderr)
    sys.exit(1)


def utc_now() -> str:
    """Return the current UTC time in ISO-8601 format (YYYY-MM-DDTHH:MM:SSZ)."""
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def ensure_static_files() -> None:
    """Create CNAME and .nojekyll if they are missing."""
    if not Path("CNAME").is_file():
        Path("CNAME").write_text("web.edumips.org\n")
        print("Created missing CNAME")
    if not Path(".nojekyll").is_file():
        Path(".nojekyll").touch()
        print("Created missing .nojekyll")


def is_candidate_date_dir(name: str) -> bool:
    """Return True if name matches YYYY-MM-DD and is a directory."""
    return bool(_DATE_DIR_RE.match(name)) and Path(name).is_dir()


def root_prod_entries() -> list[str]:
    """Return all top-level cwd entries (including dotfiles) not in RESERVED_NAMES.

    Mirrors bash `dotglob` + extglob negation — hidden files ARE included.
    Candidate date-dirs (YYYY-MM-DD) are also excluded to prevent promote/rollback
    from clobbering them.
    """
    entries = []
    for entry in Path(".").iterdir():
        if entry.name in RESERVED_NAMES:
            continue
        if is_candidate_date_dir(entry.name):
            continue
        entries.append(entry.name)
    return entries


def delete_root_prod_files() -> None:
    """Remove every root prod entry (files, dirs, symlinks), leaving reserved names."""
    for name in root_prod_entries():
        p = Path(name)
        if p.is_symlink() or p.is_file():
            p.unlink()
        elif p.is_dir():
            shutil.rmtree(p)


def copy_contents_into(src: str | Path, dest: str | Path) -> None:
    """Copy contents of src into dest (mirrors `cp -a "$src/." "$dest/"`).

    Each child of src is copied into dest, preserving metadata.
    Directories are copied recursively.
    """
    src = Path(src)
    dest = Path(dest)
    dest.mkdir(parents=True, exist_ok=True)
    for child in src.iterdir():
        dest_child = dest / child.name
        if child.is_symlink():
            # Preserve symlinks as-is.
            link_target = os.readlink(child)
            if dest_child.exists() or dest_child.is_symlink():
                dest_child.unlink()
            os.symlink(link_target, dest_child)
        elif child.is_dir():
            shutil.copytree(child, dest_child, symlinks=True,
                            dirs_exist_ok=True, copy_function=shutil.copy2)
            shutil.copystat(child, dest_child)
        else:
            shutil.copy2(child, dest_child)



def prune_versions() -> None:
    """Keep only the highest MAX_VERSIONS numeric-named subdirs under v/."""
    v_dir = Path("v")
    if not v_dir.is_dir():
        return

    numeric_dirs = sorted(
        (int(d.name) for d in v_dir.iterdir() if d.is_dir() and d.name.isdigit()),
        reverse=True,
    )
    if len(numeric_dirs) > MAX_VERSIONS:
        for n in numeric_dirs[MAX_VERSIONS:]:
            target = v_dir / str(n)
            print(f"Pruning v/{n}")
            shutil.rmtree(target)


def read_manifest() -> dict | None:
    """Return the parsed manifest.json dict, or None if the file is absent."""
    p = Path("manifest.json")
    if p.is_file():
        return json.loads(p.read_text())
    return None


def read_current_from_manifest() -> int:
    """Return manifest.json's `current` value, or 0 if the file is absent."""
    data = read_manifest()
    if data is not None:
        return int(data.get("current", 0))
    return 0


def write_manifest(data: dict) -> None:
    """Write manifest.json with 2-space indent and a trailing newline."""
    Path("manifest.json").write_text(json.dumps(data, indent=2) + "\n")


def _max_v_subdir() -> int:
    """Return the highest numeric subdir name under v/, or 0 if none exist."""
    v_dir = Path("v")
    if not v_dir.is_dir():
        return 0
    nums = [int(d.name) for d in v_dir.iterdir() if d.is_dir() and d.name.isdigit()]
    return max(nums) if nums else 0


def filter_history_to_existing(history: list[dict]) -> list[dict]:
    """Remove history entries whose v/<n>/ directory no longer exists."""
    return [e for e in history if (Path("v") / str(e["n"])).is_dir()]


# ---------------------------------------------------------------------------
# Subcommand: promote
# ---------------------------------------------------------------------------


def cmd_promote(artifact_dir: str, build_string: str, sha: str,
                target_release: str, actor: str) -> None:
    """Promote an artifact to production with versioned snapshot and rollback support."""
    artifact = Path(artifact_dir)
    if not artifact.is_dir():
        die(f"Artifact dir not found: {artifact_dir}")

    # Guard: die if artifact's top-level entries (including dotfiles) contain a reserved name.
    print("Checking artifact for reserved top-level names...")
    for child in artifact.iterdir():
        if child.name in RESERVED_NAMES:
            die(f"Artifact contains reserved top-level name '{child.name}' — cannot promote.")

    # Read full manifest (may be None on first run).
    existing_manifest = read_manifest()
    is_first_run = existing_manifest is None

    if is_first_run:
        current_n = 0
        prev_n = 0
        prior_history: list[dict] = []
        print("First run detected: manifest.json absent.")
        print("Seeding prev/ from current root prod files (first-run bootstrap)...")
        existing_root = root_prod_entries()
        if existing_root:
            if Path("prev").exists():
                shutil.rmtree("prev")
            Path("prev").mkdir()
            for name in existing_root:
                src = Path(name)
                dest = Path("prev") / name
                if src.is_symlink():
                    os.symlink(os.readlink(src), dest)
                elif src.is_dir():
                    shutil.copytree(src, dest, symlinks=True, copy_function=shutil.copy2)
                else:
                    shutil.copy2(src, dest)
            print(f"Seeded prev/ with {len(existing_root)} root item(s).")
        else:
            print("Root has no prod files yet; prev/ will be empty on first promotion.")
            Path("prev").mkdir(exist_ok=True)
    else:
        current_n = int(existing_manifest.get("current", 0))
        prev_n = int(existing_manifest.get("prev", 0))

        # Build prior history: use existing list, or backfill from top-level manifest fields.
        if "history" in existing_manifest and isinstance(existing_manifest["history"], list):
            prior_history = existing_manifest["history"]
        elif current_n >= 1:
            # History-less manifest: synthesize a single backfill entry.
            backfill: dict = {
                "n": current_n,
                "build": existing_manifest.get("build", ""),
                "sha": existing_manifest.get("sha", ""),
                "targetRelease": existing_manifest.get("targetRelease", ""),
                "promotedAt": existing_manifest.get("promotedAt", ""),
                "promotedBy": existing_manifest.get("promotedBy", ""),
            }
            prior_history = [backfill]
        else:
            prior_history = []

    # Monotonic version number: strictly greater than any number ever used.
    max_history_n = max((e["n"] for e in prior_history), default=0)
    new_n = max(current_n, prev_n, max_history_n, _max_v_subdir()) + 1

    # Defensive guard: the target snapshot must not already exist.
    target_snapshot = Path("v") / str(new_n)
    if target_snapshot.exists():
        die(f"v/{new_n}/ already exists — aborting to protect immutable snapshot.")

    print(f"Promoting: current={current_n} -> new_n={new_n}, build={build_string}, sha={sha}")

    if not is_first_run:
        # Archive current root prod files into prev/.
        print("Archiving current root into prev/...")
        root_files = root_prod_entries()
        if Path("prev").exists():
            shutil.rmtree("prev")
        Path("prev").mkdir()
        for name in root_files:
            src = Path(name)
            dest = Path("prev") / name
            if src.is_symlink():
                os.symlink(os.readlink(src), dest)
            elif src.is_dir():
                shutil.copytree(src, dest, symlinks=True, copy_function=shutil.copy2)
            else:
                shutil.copy2(src, dest)
        print("prev/ updated.")

    # Copy artifact into v/<new_n>/ (immutable snapshot).
    print(f"Creating immutable snapshot at v/{new_n}/...")
    target_snapshot.mkdir(parents=True, exist_ok=True)
    copy_contents_into(artifact, target_snapshot)

    # Replace root production files with artifact contents.
    print("Replacing root prod files...")
    delete_root_prod_files()
    copy_contents_into(artifact, Path("."))

    # Prune old snapshots BEFORE writing the manifest.
    prune_versions()

    # Build new history entry and filter to dirs that still exist after pruning.
    promoted_at = utc_now()
    new_entry: dict = {
        "n": new_n,
        "build": build_string,
        "sha": sha,
        "targetRelease": target_release,
        "promotedAt": promoted_at,
        "promotedBy": actor,
    }
    combined = [new_entry] + [e for e in prior_history if e["n"] != new_n]
    history = filter_history_to_existing(combined)

    # Write manifest.json LAST (snapshot + prune already done).
    manifest = {
        "current": new_n,
        "prev": current_n,
        "sha": sha,
        "build": build_string,
        "targetRelease": target_release,
        "promotedAt": promoted_at,
        "promotedBy": actor,
        "history": history,
    }
    write_manifest(manifest)
    print("manifest.json written.")

    ensure_static_files()

    print(f"Promote complete: v{new_n} @ {sha} by {actor}")


# ---------------------------------------------------------------------------
# Subcommand: rollback
# ---------------------------------------------------------------------------


def cmd_rollback(actor: str) -> None:
    """Swap root and prev/ to revert to the last known good build."""
    if not Path("manifest.json").is_file():
        die("manifest.json not found — nothing to roll back to.")
    if not Path("prev").is_dir():
        die("prev/ directory not found — no previous build to roll back to.")

    # Verify prev/ is non-empty before any destructive operation.
    prev_contents = list(Path("prev").iterdir())
    if not prev_contents:
        die("prev/ is empty — cannot roll back.")

    manifest = json.loads(Path("manifest.json").read_text())
    current_n = int(manifest["current"])
    prev_n = int(manifest.get("prev", 0))

    print(f"Rolling back: swapping root (v{current_n}) <-> prev (v{prev_n}), actor={actor}")

    # STEP 1: Collect current root prod files BEFORE creating any staging dir.
    root_files = root_prod_entries()

    # STEP 2: Stage root prod files in a temporary directory.
    swap_dir = Path(".rollback-swap")
    if swap_dir.exists():
        shutil.rmtree(swap_dir)
    swap_dir.mkdir()
    for name in root_files:
        shutil.move(name, swap_dir / name)

    # STEP 3: Move prev/ contents into root.
    print("Moving prev/ into root...")
    for child in list(Path("prev").iterdir()):
        shutil.move(str(child), child.name)

    # STEP 4: Move former root (from swap_dir) into prev/, replacing it.
    print("Moving former root into prev/...")
    shutil.rmtree("prev")
    Path("prev").mkdir()
    for child in list(swap_dir.iterdir()):
        shutil.move(str(child), Path("prev") / child.name)
    shutil.rmtree(swap_dir)

    # STEP 5: Update manifest — swap current <-> prev.
    manifest["current"] = prev_n
    manifest["prev"] = current_n
    manifest["promotedAt"] = utc_now()
    manifest["promotedBy"] = actor
    manifest["rolledBackFrom"] = current_n
    manifest["note"] = "rollback"
    write_manifest(manifest)
    print(f"manifest.json updated (swap: current=v{prev_n}, prev=v{current_n}).")

    ensure_static_files()

    print(f"Rollback complete: root ↔ prev swap (v{current_n} ↔ v{prev_n}) by {actor}")


def prune_candidates(candidates_data: dict) -> dict:
    """Remove candidate entries older than retentionDays and their directories."""
    retention = candidates_data.get("retentionDays", DEFAULT_RETENTION_DAYS)
    cutoff = (datetime.now(timezone.utc) - timedelta(days=retention)).strftime("%Y-%m-%d")

    kept = []
    removed_dates: set[str] = set()
    for entry in candidates_data["candidates"]:
        if entry["date"] < cutoff:
            candidate_dir = Path(entry["date"]) / f"{entry['n']}-{entry['shortsha']}"
            if candidate_dir.exists():
                shutil.rmtree(candidate_dir)
            removed_dates.add(entry["date"])
        else:
            kept.append(entry)

    candidates_data["candidates"] = kept

    for date_str in removed_dates:
        date_path = Path(date_str)
        if date_path.is_dir() and not any(date_path.iterdir()):
            date_path.rmdir()

    return candidates_data


# ---------------------------------------------------------------------------
# Subcommand: candidate
# ---------------------------------------------------------------------------


def cmd_candidate(artifact_dir: str, sha: str, build_string: str,
                  target_release: str) -> None:
    """Deploy a candidate build to /<date>/<n>-<shortsha>/ and update candidates.json."""
    artifact = Path(artifact_dir)
    if not artifact.is_dir():
        die(f"Artifact dir not found: {artifact_dir}")

    date = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    shortsha = sha[:7]

    candidates_file = Path("candidates.json")
    if candidates_file.is_file():
        candidates_data = json.loads(candidates_file.read_text())
    else:
        candidates_data = {"candidates": [], "retentionDays": DEFAULT_RETENTION_DAYS}

    today_ns = [e["n"] for e in candidates_data["candidates"] if e["date"] == date]
    new_n = max(today_ns) + 1 if today_ns else 1

    candidate_dir = Path(date) / f"{new_n}-{shortsha}"
    print(f"Creating candidate at {candidate_dir}...")
    candidate_dir.mkdir(parents=True, exist_ok=True)
    copy_contents_into(artifact, candidate_dir)

    new_entry: dict = {
        "date": date,
        "n": new_n,
        "sha": sha,
        "shortsha": shortsha,
        "path": f"/{date}/{new_n}-{shortsha}/",
        "build": build_string,
        "targetRelease": target_release,
        "deployedAt": utc_now(),
    }
    candidates_data["candidates"] = [new_entry] + candidates_data["candidates"]
    candidates_data["candidates"].sort(key=lambda e: (e["date"], e["n"]), reverse=True)

    candidates_data = prune_candidates(candidates_data)
    candidates_file.write_text(json.dumps(candidates_data, indent=2) + "\n")
    print("candidates.json written.")

    ensure_static_files()

    if Path("nightly").is_dir():
        shutil.rmtree("nightly")
        print("Removed legacy /nightly/ directory.")

    print(f"Candidate deploy complete: {candidate_dir} @ {sha}")


# ---------------------------------------------------------------------------
# CLI dispatch
# ---------------------------------------------------------------------------


class _ArgumentParser(argparse.ArgumentParser):
    """ArgumentParser that exits with status 1 on usage errors.

    Mirrors the bash script's `die` contract (any error exits 1) instead of
    argparse's default exit status 2.
    """

    def error(self, message: str):
        self.print_usage(sys.stderr)
        sys.stderr.write(f"ERROR: {message}\n")
        sys.exit(1)


def build_parser() -> argparse.ArgumentParser:
    parser = _ArgumentParser(
        prog="deploy-web-pages.py",
        description="Pages-repo layout manager for web.edumips.org",
    )
    sub = parser.add_subparsers(dest="subcmd")

    p_promote = sub.add_parser("promote", help="Promote a web artifact to production")
    p_promote.add_argument("artifact_dir")
    p_promote.add_argument("build_string")
    p_promote.add_argument("sha")
    p_promote.add_argument("target_release")
    p_promote.add_argument("actor")

    p_rollback = sub.add_parser("rollback", help="Roll back to the previous production build")
    p_rollback.add_argument("actor")

    p_candidate = sub.add_parser("candidate", help="Deploy artifact as a candidate build")
    p_candidate.add_argument("artifact_dir")
    p_candidate.add_argument("sha")
    p_candidate.add_argument("build_string")
    p_candidate.add_argument("target_release")

    return parser


def main() -> None:
    parser = build_parser()
    if len(sys.argv) < 2:
        parser.print_usage()
        sys.exit(1)

    args = parser.parse_args()

    if args.subcmd == "promote":
        cmd_promote(args.artifact_dir, args.build_string, args.sha,
                    args.target_release, args.actor)
    elif args.subcmd == "rollback":
        cmd_rollback(args.actor)
    elif args.subcmd == "candidate":
        cmd_candidate(args.artifact_dir, args.sha, args.build_string, args.target_release)
    else:
        parser.print_usage()
        sys.exit(1)


if __name__ == "__main__":
    main()
