#!/usr/bin/env python3
"""deploy-web-pages.py — Pages-repo layout manager for web.edumips.org.

Unified, commit-addressed web versioning. See
docs/design/unified-web-versioning.md for the full design.

Subcommands:
  push <artifact_dir> <sha> <seq> <build> <target_release>
      Publish a master build as a candidate at /c/<sha>/. Never touches root.
  promote <sha> <actor>
      Promote an already-pushed candidate to production. Copies /c/<sha>/ into
      the root, marks it promoted, prunes non-promoted candidates older than the
      new current. NEVER builds.
  rollback <actor>
      Revert production to the newest promoted version older than the current
      one. Copies its /c/<sha>/ into root. Never prunes.
  migrate [--repo PATH] [--dry-run]
      One-shot conversion from the legacy manifest.json + candidates.json model
      to versions.json + /c/<sha>/.

Must be run with cwd = checkout of the Pages repo (EduMIPS64/web.edumips.org).

KEEP-INVARIANT: a build V is retained iff
    V.promoted == True  OR  V.seq > current.seq
Every operation re-establishes this invariant.
"""

import argparse
import json
import os
import re
import shutil
import sys
from datetime import datetime, timezone
from pathlib import Path

# ---------------------------------------------------------------------------
# Constants
# ---------------------------------------------------------------------------

# Top-level names that a root replacement must never delete.
RESERVED_NAMES = ["c", "versions.json", "CNAME", ".nojekyll", ".git"]
# Name of the per-build directory tree (immutable snapshots live at c/<sha>/).
BUILDS_DIR = "c"
INDEX_FILE = "versions.json"

# Number of most-recent promoted versions whose c/<sha>/ snapshot is retained.
# The current production version is always retained regardless of this cap.
# Promoted versions beyond this window keep their versions.json entry (with
# "pruned": true) so the audit trail survives, but their snapshot is deleted.
KEEP_PROMOTED = 10

_FULL_SHA_RE = re.compile(r"^[0-9a-f]{40}$")

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------


def die(msg: str) -> None:
    """Print an error message to stderr and exit 1."""
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


def root_prod_entries() -> list[str]:
    """Return all top-level cwd entries (including dotfiles) not reserved.

    Hidden files ARE included (mirrors bash `dotglob`). The builds dir (c/),
    versions.json, CNAME, .nojekyll and .git are preserved.
    """
    return [e.name for e in Path(".").iterdir() if e.name not in RESERVED_NAMES]


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


def read_index() -> dict:
    """Return the parsed versions.json, or a fresh empty index if absent."""
    p = Path(INDEX_FILE)
    if p.is_file():
        data = json.loads(p.read_text())
        if not isinstance(data, dict) or not isinstance(data.get("versions"), list):
            die(f"{INDEX_FILE} is malformed.")
        data.setdefault("current", None)
        return data
    return {"current": None, "versions": []}


def write_index(data: dict) -> None:
    """Write versions.json with 2-space indent and a trailing newline.

    `versions` is always sorted newest-first by seq before writing.
    """
    data["versions"].sort(key=lambda e: e["seq"], reverse=True)
    Path(INDEX_FILE).write_text(json.dumps(data, indent=2) + "\n")


def find_version(index: dict, sha: str) -> dict | None:
    """Return the version entry for sha (or a unique short-sha prefix), else None."""
    for entry in index["versions"]:
        if entry["sha"] == sha:
            return entry
    # Allow short-sha lookups (unambiguous prefix match).
    matches = [e for e in index["versions"] if e["sha"].startswith(sha)]
    if len(matches) == 1:
        return matches[0]
    if len(matches) > 1:
        die(f"Ambiguous sha prefix '{sha}' matches {len(matches)} versions.")
    return None


def current_entry(index: dict) -> dict | None:
    """Return the version entry referenced by index['current'], or None."""
    cur = index.get("current")
    if not cur:
        return None
    return find_version(index, cur)


def build_dir(sha: str) -> Path:
    """Return the Path to a build's immutable snapshot dir (c/<sha>/)."""
    return Path(BUILDS_DIR) / sha


def replace_root_with(sha: str) -> None:
    """Clean-replace the root prod files with a copy of c/<sha>/."""
    snapshot = build_dir(sha)
    if not snapshot.is_dir():
        die(f"{snapshot}/ does not exist — cannot copy into root.")
    print(f"Replacing root prod files with {snapshot}/ ...")
    delete_root_prod_files()
    copy_contents_into(snapshot, Path("."))


def prune_to_invariant(index: dict) -> None:
    """Apply the retention policy after a promote operation.

    A version's c/<sha>/ snapshot is retained iff:
      (1) It is the current production version, OR
      (2) It is among the KEEP_PROMOTED most-recent promoted versions by seq, OR
      (3) Its seq > current.seq (candidate newer than current).

    For promoted versions outside the retention window: the versions.json entry
    is kept with "pruned": true (audit trail / seq history preserved) but the
    c/<sha>/ snapshot directory is deleted to reclaim Pages storage.

    Non-promoted versions with seq < current.seq are removed entirely (both the
    entry and the snapshot) — these are candidates that were skipped over and will
    never be promoted.

    promote and rollback refuse to target a pruned version.
    """
    cur = current_entry(index)
    if cur is None:
        return
    cur_seq = cur["seq"]

    # Determine which promoted snapshots to keep on disk.
    all_promoted_by_seq = sorted(
        (e for e in index["versions"] if e["promoted"]),
        key=lambda e: e["seq"],
        reverse=True,
    )
    keep_snapshot_shas: set[str] = {cur["sha"]}  # current is always kept (rule 1)
    for e in all_promoted_by_seq[:KEEP_PROMOTED]:  # top KEEP_PROMOTED (rule 2)
        keep_snapshot_shas.add(e["sha"])

    kept = []
    for entry in index["versions"]:
        if entry["seq"] > cur_seq:
            # Future candidate: always keep entirely (rule 3).
            entry.pop("pruned", None)
            kept.append(entry)
        elif entry["promoted"]:
            if entry["sha"] in keep_snapshot_shas:
                # Within retention window: keep snapshot, clear any stale pruned flag.
                entry.pop("pruned", None)
                kept.append(entry)
            else:
                # Promoted but outside KEEP_PROMOTED window: retain entry (audit
                # trail) but delete the on-disk snapshot to save space.
                entry["pruned"] = True
                snapshot = build_dir(entry["sha"])
                if snapshot.is_dir():
                    print(f"Pruning old promoted snapshot {BUILDS_DIR}/{entry['sha']} "
                          f"(seq {entry['seq']}, beyond KEEP_PROMOTED={KEEP_PROMOTED})")
                    shutil.rmtree(snapshot)
                kept.append(entry)
        else:
            # Non-promoted and in the past: remove entry and snapshot entirely.
            snapshot = build_dir(entry["sha"])
            if snapshot.is_dir():
                print(f"Pruning candidate {BUILDS_DIR}/{entry['sha']} (seq {entry['seq']})")
                shutil.rmtree(snapshot)
            # Entry intentionally omitted from kept.

    index["versions"] = kept


# ---------------------------------------------------------------------------
# Subcommand: push
# ---------------------------------------------------------------------------


def cmd_push(artifact_dir: str, sha: str, seq: int, build_string: str,
             target_release: str) -> None:
    """Publish a master build as a candidate at c/<sha>/. Never touches root."""
    artifact = Path(artifact_dir)
    if not artifact.is_dir():
        die(f"Artifact dir not found: {artifact_dir}")
    if not _FULL_SHA_RE.match(sha):
        die(f"push requires a full 40-char commit sha, got '{sha}'.")

    index = read_index()
    snapshot = build_dir(sha)
    existing = find_version(index, sha)

    if existing is not None and snapshot.is_dir():
        # Idempotent: a CI re-run of the same commit. Leave files and entry
        # untouched (the snapshot is immutable).
        print(f"Build {sha} already pushed (idempotent no-op).")
        ensure_static_files()
        write_index(index)
        return

    print(f"Pushing candidate {sha} (seq {seq}) to {snapshot}/ ...")
    if snapshot.is_dir():
        # Directory present but no index entry (partial state) — rewrite it.
        shutil.rmtree(snapshot)
    snapshot.mkdir(parents=True, exist_ok=True)
    copy_contents_into(artifact, snapshot)

    new_entry = {
        "sha": sha,
        "shortsha": sha[:7],
        "seq": int(seq),
        "build": build_string,
        "targetRelease": target_release,
        "pushedAt": utc_now(),
        "promoted": False,
    }
    # Upsert (replace any stale entry for this sha).
    index["versions"] = [e for e in index["versions"] if e["sha"] != sha]
    index["versions"].append(new_entry)

    write_index(index)
    ensure_static_files()
    print(f"Push complete: {BUILDS_DIR}/{sha}/ (candidate, seq {seq})")


# ---------------------------------------------------------------------------
# Subcommand: promote (NEVER builds)
# ---------------------------------------------------------------------------


def cmd_promote(sha: str, actor: str) -> None:
    """Promote an already-pushed candidate to production. Never builds."""
    index = read_index()
    entry = find_version(index, sha)
    if entry is None:
        die(f"Refusing to promote '{sha}': no such version in {INDEX_FILE} "
            f"(promotion never builds — the commit must be pushed first).")
    full_sha = entry["sha"]
    if entry.get("pruned"):
        die(f"Refusing to promote '{full_sha}': this version has been pruned "
            f"(its c/<sha>/ snapshot was deleted to save Pages storage). "
            f"Pruned versions cannot be promoted. "
            f"To restore it you would need to re-push the artifact with `push`.")
    if not build_dir(full_sha).is_dir():
        die(f"Refusing to promote '{full_sha}': {BUILDS_DIR}/{full_sha}/ "
            f"is missing (promotion never builds).")

    cur = current_entry(index)
    if cur is not None and full_sha == cur["sha"]:
        print(f"{full_sha} is already current — refreshing root and metadata.")

    print(f"Promoting {full_sha} (seq {entry['seq']}) by {actor} ...")

    # 1. Mark promoted (idempotent — keep original promotion audit if re-promoting).
    entry["promoted"] = True
    entry.setdefault("promotedAt", utc_now())
    entry.setdefault("promotedBy", actor)

    # 2. Point current at it and copy into root (clean replace, no build).
    index["current"] = full_sha
    replace_root_with(full_sha)

    # 3. Prune non-promoted candidates now in the past.
    prune_to_invariant(index)

    write_index(index)
    ensure_static_files()
    print(f"Promote complete: current={full_sha} (seq {entry['seq']}) by {actor}")


# ---------------------------------------------------------------------------
# Subcommand: rollback
# ---------------------------------------------------------------------------


def cmd_rollback(actor: str) -> None:
    """Revert to the newest promoted version older than the current one."""
    index = read_index()
    cur = current_entry(index)
    if cur is None:
        die("No current version — nothing to roll back from.")

    older_promoted = [
        e for e in index["versions"]
        if e["promoted"] and e["seq"] < cur["seq"] and not e.get("pruned")
    ]
    if not older_promoted:
        die("No older promoted version to roll back to (all older promoted versions "
            "have been pruned or no older promoted version exists). "
            "With KEEP_PROMOTED=10 this should not happen in normal operation.")

    target = max(older_promoted, key=lambda e: e["seq"])
    if not build_dir(target["sha"]).is_dir():
        die(f"Rollback target {BUILDS_DIR}/{target['sha']}/ is missing.")

    print(f"Rolling back: current {cur['sha']} (seq {cur['seq']}) "
          f"-> {target['sha']} (seq {target['seq']}) by {actor}")

    index["current"] = target["sha"]
    replace_root_with(target["sha"])
    # Lowering current only GROWS the kept set, so the invariant still holds:
    # no pruning needed. The candidates between target and the former current
    # become "future" again and are preserved.

    write_index(index)
    ensure_static_files()
    print(f"Rollback complete: current={target['sha']} (seq {target['seq']}) by {actor}")


# ---------------------------------------------------------------------------
# Subcommand: migrate (one-shot legacy -> unified)
# ---------------------------------------------------------------------------


def _seq_for(sha: str, repo: str | None) -> int:
    """Compute seq = git rev-list --count <sha> from a local edumips64 clone."""
    import subprocess
    if not repo:
        die("migrate requires --repo <path-to-edumips64-clone> to compute seq.")
    try:
        out = subprocess.check_output(
            ["git", "-C", repo, "rev-list", "--count", sha],
            text=True, stderr=subprocess.STDOUT,
        )
    except subprocess.CalledProcessError as exc:
        die(f"git rev-list failed for {sha}: {exc.output}")
    return int(out.strip())


def _redirect_stub(target_url: str) -> str:
    """Return a minimal HTML redirect page forwarding to target_url."""
    return (
        "<!doctype html><html><head><meta charset=\"utf-8\">"
        f"<meta http-equiv=\"refresh\" content=\"0; url={target_url}\">"
        f"<link rel=\"canonical\" href=\"{target_url}\">"
        f"<title>Moved</title></head><body>"
        f"<p>This build has moved to <a href=\"{target_url}\">{target_url}</a>.</p>"
        "</body></html>\n"
    )


def cmd_migrate(repo: str | None, dry_run: bool) -> None:
    """Convert legacy manifest.json + candidates.json to versions.json + c/<sha>/."""
    manifest_path = Path("manifest.json")
    candidates_path = Path("candidates.json")
    if not manifest_path.is_file() and not candidates_path.is_file():
        die("Neither manifest.json nor candidates.json found — nothing to migrate.")

    versions: dict[str, dict] = {}  # sha -> entry
    moves: list[tuple[Path, str]] = []  # (old_dir, sha)
    redirect_stubs: list[tuple[Path, str]] = []  # (old_v_dir, sha)

    current_sha = None

    # --- promoted versions from manifest.json ---
    if manifest_path.is_file():
        manifest = json.loads(manifest_path.read_text())
        current_n = int(manifest.get("current", 0))
        for e in manifest.get("history", []):
            sha = e.get("sha", "")
            if not _FULL_SHA_RE.match(sha):
                print(f"WARNING: skipping manifest entry n={e.get('n')} "
                      f"with non-full sha '{sha}'")
                continue
            seq = _seq_for(sha, repo)
            entry = {
                "sha": sha,
                "shortsha": sha[:7],
                "seq": seq,
                "build": e.get("build", ""),
                "targetRelease": e.get("targetRelease", ""),
                "pushedAt": e.get("promotedAt", ""),
                "promoted": True,
                "promotedAt": e.get("promotedAt", ""),
                "promotedBy": e.get("promotedBy", ""),
            }
            versions[sha] = entry
            old_dir = Path("v") / str(e["n"])
            if old_dir.is_dir():
                moves.append((old_dir, sha))
                redirect_stubs.append((old_dir, sha))
            if int(e.get("n", -1)) == current_n:
                current_sha = sha

    # --- candidate versions from candidates.json ---
    if candidates_path.is_file():
        candidates = json.loads(candidates_path.read_text())
        for e in candidates.get("candidates", []):
            sha = e.get("sha", "")
            if not _FULL_SHA_RE.match(sha):
                print(f"WARNING: skipping candidate {e.get('path')} "
                      f"with non-full sha '{sha}'")
                continue
            if sha in versions:
                continue  # already promoted; promoted entry wins
            seq = _seq_for(sha, repo)
            versions[sha] = {
                "sha": sha,
                "shortsha": sha[:7],
                "seq": seq,
                "build": e.get("build", ""),
                "targetRelease": e.get("targetRelease", ""),
                "pushedAt": e.get("deployedAt", ""),
                "promoted": False,
            }
            old_dir = Path(e["date"]) / f"{e['n']}-{e['shortsha']}"
            if old_dir.is_dir():
                moves.append((old_dir, sha))

    index = {
        "current": current_sha,
        "versions": sorted(versions.values(), key=lambda x: x["seq"], reverse=True),
    }

    print(f"Migration plan: {len(index['versions'])} versions, "
          f"current={current_sha}, {len(moves)} dir moves, "
          f"{len(redirect_stubs)} redirect stubs.")
    if dry_run:
        print("--- DRY RUN: versions.json that WOULD be written ---")
        print(json.dumps(index, indent=2))
        print("--- moves ---")
        for old, sha in moves:
            print(f"  {old}  ->  {BUILDS_DIR}/{sha}/")
        return

    # Move directories into c/<sha>/.
    Path(BUILDS_DIR).mkdir(exist_ok=True)
    for old, sha in moves:
        dest = build_dir(sha)
        if dest.exists():
            shutil.rmtree(old)
            continue
        shutil.move(str(old), str(dest))

    # Replace promoted /v/<n>/ dirs with redirect stubs (one cycle).
    Path("v").mkdir(exist_ok=True)
    for old_v_dir, sha in redirect_stubs:
        old_v_dir.mkdir(parents=True, exist_ok=True)
        (old_v_dir / "index.html").write_text(
            _redirect_stub(f"/{BUILDS_DIR}/{sha}/"))

    # Remove legacy index files and prev/ (root copy already equals current).
    for legacy in ("manifest.json", "candidates.json"):
        if Path(legacy).is_file():
            Path(legacy).unlink()
    if Path("prev").is_dir():
        shutil.rmtree("prev")
    # Remove stale candidate date dirs that no longer have an index entry.
    for entry in Path(".").iterdir():
        if re.match(r"^\d{4}-\d{2}-\d{2}$", entry.name) and entry.is_dir():
            shutil.rmtree(entry)

    write_index(index)
    ensure_static_files()
    print(f"Migration complete: {INDEX_FILE} written with {len(index['versions'])} versions.")


# ---------------------------------------------------------------------------
# CLI dispatch
# ---------------------------------------------------------------------------


class _ArgumentParser(argparse.ArgumentParser):
    """ArgumentParser that exits with status 1 on usage errors."""

    def error(self, message: str):
        self.print_usage(sys.stderr)
        sys.stderr.write(f"ERROR: {message}\n")
        sys.exit(1)


def build_parser() -> argparse.ArgumentParser:
    parser = _ArgumentParser(
        prog="deploy-web-pages.py",
        description="Unified commit-addressed layout manager for web.edumips.org",
    )
    sub = parser.add_subparsers(dest="subcmd")

    p_push = sub.add_parser("push", help="Publish a master build as a candidate")
    p_push.add_argument("artifact_dir")
    p_push.add_argument("sha")
    p_push.add_argument("seq", type=int)
    p_push.add_argument("build_string")
    p_push.add_argument("target_release")

    p_promote = sub.add_parser("promote", help="Promote an already-pushed candidate")
    p_promote.add_argument("sha")
    p_promote.add_argument("actor")

    p_rollback = sub.add_parser("rollback", help="Roll back to previous promoted build")
    p_rollback.add_argument("actor")

    p_migrate = sub.add_parser("migrate", help="One-shot legacy -> unified migration")
    p_migrate.add_argument("--repo", help="Path to an edumips64 clone (for seq computation)")
    p_migrate.add_argument("--dry-run", action="store_true")

    return parser


def main() -> None:
    parser = build_parser()
    if len(sys.argv) < 2:
        parser.print_usage()
        sys.exit(1)

    args = parser.parse_args()

    if args.subcmd == "push":
        cmd_push(args.artifact_dir, args.sha, args.seq, args.build_string,
                 args.target_release)
    elif args.subcmd == "promote":
        cmd_promote(args.sha, args.actor)
    elif args.subcmd == "rollback":
        cmd_rollback(args.actor)
    elif args.subcmd == "migrate":
        cmd_migrate(args.repo, args.dry_run)
    else:
        parser.print_usage()
        sys.exit(1)


if __name__ == "__main__":
    main()
