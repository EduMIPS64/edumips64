"""Tests for deploy-web-pages.py.

Run with:  cd .github/scripts && python -m pytest test_deploy_web_pages.py -q
"""

import importlib.util
import json
from pathlib import Path

# ---------------------------------------------------------------------------
# Load the hyphenated module
# ---------------------------------------------------------------------------

_SCRIPT = Path(__file__).parent / "deploy-web-pages.py"
spec = importlib.util.spec_from_file_location("deploy_web_pages", _SCRIPT)
dwp = importlib.util.module_from_spec(spec)
spec.loader.exec_module(dwp)

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------


def make_artifact(tmp_path: Path, name: str = "art") -> Path:
    """Create a minimal fake artifact directory with one file."""
    art = tmp_path / name
    art.mkdir()
    (art / "index.html").write_text(f"<h1>{name}</h1>")
    return art


def promote(pages: Path, artifact: Path, n: int, actor: str = "ci") -> None:
    """Run cmd_promote inside pages dir."""
    import os
    old = os.getcwd()
    os.chdir(pages)
    try:
        dwp.cmd_promote(
            str(artifact),
            f"build-{n}",
            f"sha{n:040x}",
            f"1.{n}.0",
            actor,
        )
    finally:
        os.chdir(old)


def rollback(pages: Path, actor: str = "ci") -> None:
    """Run cmd_rollback inside pages dir."""
    import os
    old = os.getcwd()
    os.chdir(pages)
    try:
        dwp.cmd_rollback(actor)
    finally:
        os.chdir(old)


def read_manifest(pages: Path) -> dict:
    return json.loads((pages / "manifest.json").read_text())


# ---------------------------------------------------------------------------
# (a) First-ever promote — no manifest
# ---------------------------------------------------------------------------


def test_first_promote_creates_history(tmp_path):
    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)

    promote(pages, art, 1)

    m = read_manifest(pages)
    assert m["current"] == 1
    assert m["prev"] == 0
    assert isinstance(m["history"], list)
    assert len(m["history"]) == 1
    entry = m["history"][0]
    assert entry["n"] == 1
    assert entry["build"] == "build-1"
    assert entry["sha"] == f"sha{1:040x}"
    assert entry["targetRelease"] == "1.1.0"
    assert "promotedAt" in entry
    assert entry["promotedBy"] == "ci"
    assert (pages / "v" / "1").is_dir()


# ---------------------------------------------------------------------------
# (b) Promote against a history-LESS manifest (backfill)
# ---------------------------------------------------------------------------


def test_promote_against_history_less_manifest(tmp_path):
    pages = tmp_path / "pages"
    pages.mkdir()

    # Write a legacy manifest without 'history'.
    legacy = {
        "current": 1,
        "prev": 0,
        "sha": "aaa",
        "build": "old-build",
        "targetRelease": "1.0.0",
        "promotedAt": "2026-01-01T00:00:00Z",
        "promotedBy": "human",
    }
    (pages / "manifest.json").write_text(json.dumps(legacy))
    # Simulate that v/1 already exists (immutable snapshot).
    (pages / "v" / "1").mkdir(parents=True)
    (pages / "prev").mkdir()
    # Root needs at least one prod file for the archiving step.
    (pages / "index.html").write_text("v1")

    art = make_artifact(tmp_path)
    promote(pages, art, 2)

    m = read_manifest(pages)
    assert m["current"] == 2
    assert m["prev"] == 1
    history = m["history"]
    assert len(history) == 2
    assert history[0]["n"] == 2  # newest first
    assert history[1]["n"] == 1
    # Backfilled v1 entry should carry legacy fields.
    assert history[1]["build"] == "old-build"
    assert history[1]["sha"] == "aaa"
    assert history[1]["promotedBy"] == "human"


# ---------------------------------------------------------------------------
# (c) Third promote — history ordered desc, all n unique
# ---------------------------------------------------------------------------


def test_three_promotes_ordered_and_unique(tmp_path):
    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)

    promote(pages, art, 1)
    promote(pages, art, 2)
    promote(pages, art, 3)

    m = read_manifest(pages)
    assert m["current"] == 3
    history = m["history"]
    assert len(history) == 3
    ns = [e["n"] for e in history]
    assert ns == sorted(ns, reverse=True), "history must be newest-first"
    assert len(set(ns)) == len(ns), "n values must be unique"
    assert ns == [3, 2, 1]


# ---------------------------------------------------------------------------
# (d) Numbering after rollback: no collision with v/2
# ---------------------------------------------------------------------------


def test_numbering_after_rollback_no_collision(tmp_path):
    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)

    promote(pages, art, 1)   # current=1, prev=0
    promote(pages, art, 2)   # current=2, prev=1
    rollback(pages)           # current=1, prev=2  (swap)

    m_after_rb = read_manifest(pages)
    assert m_after_rb["current"] == 1
    assert m_after_rb["prev"] == 2

    promote(pages, art, 3)   # must be v/3, NOT v/2

    m = read_manifest(pages)
    assert m["current"] == 3, f"expected new_n=3, got {m['current']}"
    assert (pages / "v" / "3").is_dir(), "v/3 must have been created"
    # v/2 must be intact (immutable snapshot untouched).
    assert (pages / "v" / "2").is_dir(), "v/2 must still exist"
    # History must not contain duplicate n values.
    ns = [e["n"] for e in m["history"]]
    assert len(set(ns)) == len(ns)


# ---------------------------------------------------------------------------
# (e) Prune lockstep: history entries for pruned dirs are removed
# ---------------------------------------------------------------------------


def test_prune_lockstep_removes_history_entries(tmp_path, monkeypatch):
    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)

    # Temporarily reduce MAX_VERSIONS to 2 so pruning triggers quickly.
    monkeypatch.setattr(dwp, "MAX_VERSIONS", 2)

    promote(pages, art, 1)   # history=[1]         v/: 1
    promote(pages, art, 2)   # history=[2,1]        v/: 2,1
    promote(pages, art, 3)   # prunes v/1 → history=[3,2]

    m = read_manifest(pages)
    assert m["current"] == 3
    history_ns = {e["n"] for e in m["history"]}
    # v/1 pruned → its entry removed from history.
    assert 1 not in history_ns
    assert 2 in history_ns
    assert 3 in history_ns
    # Physical check.
    assert not (pages / "v" / "1").exists(), "v/1 should have been pruned"
    assert (pages / "v" / "2").is_dir()
    assert (pages / "v" / "3").is_dir()
