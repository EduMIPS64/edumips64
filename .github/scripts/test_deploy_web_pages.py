"""Tests for deploy-web-pages.py.

Run with:  cd .github/scripts && python -m pytest test_deploy_web_pages.py -q
"""

import importlib.util
import json
import os
import pytest
from datetime import datetime, timedelta, timezone
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

# ---------------------------------------------------------------------------
# Candidate helper
# ---------------------------------------------------------------------------


def candidate(pages: Path, artifact: Path, sha: str = "abc1234def5678901234567890123456789012ab",
              build_string: str = "v1.0.0-1-gabc1234", target_release: str = "1.0.1") -> None:
    """Run cmd_candidate inside pages dir."""
    old = os.getcwd()
    os.chdir(pages)
    try:
        dwp.cmd_candidate(str(artifact), sha, build_string, target_release)
    finally:
        os.chdir(old)


def read_candidates(pages: Path) -> dict:
    return json.loads((pages / "candidates.json").read_text())


# ---------------------------------------------------------------------------
# §6.1 Test 1: First candidate run
# ---------------------------------------------------------------------------


def test_candidate_first_run(tmp_path, monkeypatch):
    today = "2026-06-13"
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 13, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())

    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)
    sha = "abc1234def5678901234567890123456789012ab"
    shortsha = sha[:7]

    candidate(pages, art, sha=sha)

    # candidates.json must exist
    assert (pages / "candidates.json").is_file()
    data = read_candidates(pages)
    assert "candidates" in data
    assert "retentionDays" in data
    assert len(data["candidates"]) == 1

    entry = data["candidates"][0]
    assert entry["date"] == today
    assert entry["n"] == 1
    assert entry["sha"] == sha
    assert entry["shortsha"] == shortsha
    assert entry["path"] == f"/{today}/1-{shortsha}/"
    assert "build" in entry
    assert "targetRelease" in entry
    assert "deployedAt" in entry

    # Candidate dir must exist with artifact contents
    cand_dir = pages / today / f"1-{shortsha}"
    assert cand_dir.is_dir()
    assert (cand_dir / "index.html").is_file()


# ---------------------------------------------------------------------------
# §6.1 Test 2: Second run same day — n increments, both entries present, sorted
# ---------------------------------------------------------------------------


def test_candidate_second_run_same_day(tmp_path, monkeypatch):
    today = "2026-06-13"
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 13, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())

    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)
    sha1 = "aaa1111def5678901234567890123456789012ab"
    sha2 = "bbb2222def5678901234567890123456789012ab"

    candidate(pages, art, sha=sha1)
    candidate(pages, art, sha=sha2)

    data = read_candidates(pages)
    assert len(data["candidates"]) == 2

    # Sorted newest-first by (date, n)
    assert data["candidates"][0]["n"] == 2
    assert data["candidates"][1]["n"] == 1

    assert data["candidates"][0]["shortsha"] == sha2[:7]
    assert data["candidates"][1]["shortsha"] == sha1[:7]

    # Both dirs must exist
    assert (pages / today / f"2-{sha2[:7]}").is_dir()
    assert (pages / today / f"1-{sha1[:7]}").is_dir()


# ---------------------------------------------------------------------------
# §6.1 Test 3: Different day — n resets to 1 for new date
# ---------------------------------------------------------------------------


def test_candidate_different_day_resets_n(tmp_path, monkeypatch):
    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)

    # First run on day 1
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 13, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())
    sha1 = "aaa1111def5678901234567890123456789012ab"
    candidate(pages, art, sha=sha1)

    # Simulate a second run the next day by injecting an older entry first via
    # direct manipulation, then running cmd_candidate with a "new" day mock.
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 14, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())
    sha2 = "bbb2222def5678901234567890123456789012ab"
    candidate(pages, art, sha=sha2)

    data = read_candidates(pages)
    # Newest entry is from day 2 with n=1
    assert data["candidates"][0]["date"] == "2026-06-14"
    assert data["candidates"][0]["n"] == 1
    # Older entry is from day 1 with n=1
    assert data["candidates"][1]["date"] == "2026-06-13"
    assert data["candidates"][1]["n"] == 1


# ---------------------------------------------------------------------------
# §6.1 Test 4: Pruning removes old entries and dirs
# ---------------------------------------------------------------------------


def test_candidate_pruning(tmp_path, monkeypatch):
    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)

    # Deploy a candidate "30 days ago"
    old_date = (datetime.now(timezone.utc) - timedelta(days=30)).strftime("%Y-%m-%d")
    old_sha = "old1234def5678901234567890123456789012ab"
    old_shortsha = old_sha[:7]
    old_dir = pages / old_date / f"1-{old_shortsha}"
    old_dir.mkdir(parents=True)
    (old_dir / "index.html").write_text("old")

    stale_entry = {
        "date": old_date,
        "n": 1,
        "sha": old_sha,
        "shortsha": old_shortsha,
        "path": f"/{old_date}/1-{old_shortsha}/",
        "build": "v0.9.0",
        "targetRelease": "1.0.0",
        "deployedAt": "2026-01-01T00:00:00Z",
    }
    candidates_file = pages / "candidates.json"
    candidates_file.write_text(json.dumps({"candidates": [stale_entry], "retentionDays": 14}) + "\n")

    # Deploy a fresh candidate today — this triggers pruning inside cmd_candidate
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime.now(timezone.utc)),
                        "strptime": datetime.strptime})())
    sha_new = "new1234def5678901234567890123456789012ab"
    candidate(pages, art, sha=sha_new)

    data = read_candidates(pages)
    dates = {e["date"] for e in data["candidates"]}
    # Old entry should be pruned
    assert old_date not in dates
    # Old dir should be deleted
    assert not old_dir.exists()
    # Empty date dir should be removed
    assert not (pages / old_date).exists()
    # Fresh entry must survive
    today = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    assert any(e["date"] == today for e in data["candidates"])


# ---------------------------------------------------------------------------
# §6.1 Test 5: root_prod_entries excludes date-pattern dirs and candidates.json
# ---------------------------------------------------------------------------


def test_root_prod_entries_excludes_candidate_dirs(tmp_path):
    pages = tmp_path / "pages"
    pages.mkdir()

    # Create a date dir, candidates.json, and a normal file
    (pages / "2026-06-13").mkdir()
    (pages / "candidates.json").write_text("{}")
    (pages / "index.html").write_text("prod")

    old = os.getcwd()
    os.chdir(pages)
    try:
        entries = dwp.root_prod_entries()
    finally:
        os.chdir(old)

    assert "2026-06-13" not in entries
    assert "candidates.json" not in entries
    assert "index.html" in entries


# ---------------------------------------------------------------------------
# §6.1 Test 6: cmd_promote does NOT delete candidate date-dirs or candidates.json
# ---------------------------------------------------------------------------


def test_promote_preserves_candidate_dirs(tmp_path, monkeypatch):
    today = "2026-06-13"
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 13, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())

    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)
    sha = "abc1234def5678901234567890123456789012ab"

    # First: deploy a candidate
    candidate(pages, art, sha=sha)

    cand_dir = pages / today / f"1-{sha[:7]}"
    assert cand_dir.is_dir(), "candidate dir must exist before promote"
    assert (pages / "candidates.json").is_file(), "candidates.json must exist before promote"

    # Now promote a new artifact
    art2 = make_artifact(tmp_path, "art2")
    promote(pages, art2, 1)

    # Candidate dir and candidates.json must survive promote
    assert cand_dir.is_dir(), "candidate dir must survive promote"
    assert (pages / "candidates.json").is_file(), "candidates.json must survive promote"


# ---------------------------------------------------------------------------
# §6.1 Test 7: cmd_rollback does NOT disturb candidate date-dirs / candidates.json
# ---------------------------------------------------------------------------


def test_rollback_preserves_candidate_dirs(tmp_path, monkeypatch):
    today = "2026-06-13"
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 13, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())

    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)
    sha = "abc1234def5678901234567890123456789012ab"

    # Deploy two production versions and then deploy a candidate
    promote(pages, art, 1)
    art2 = make_artifact(tmp_path, "art2")
    promote(pages, art2, 2)

    candidate(pages, art, sha=sha)

    cand_dir = pages / today / f"1-{sha[:7]}"
    assert cand_dir.is_dir()
    assert (pages / "candidates.json").is_file()

    rollback(pages)

    # Candidate dir and candidates.json must survive rollback
    assert cand_dir.is_dir(), "candidate dir must survive rollback"
    assert (pages / "candidates.json").is_file(), "candidates.json must survive rollback"


# ---------------------------------------------------------------------------
# §6.1 Test 8: Legacy /nightly/ dir removed on first candidate deploy
# ---------------------------------------------------------------------------


def test_candidate_removes_nightly_dir(tmp_path, monkeypatch):
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 13, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())

    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)
    # Create a legacy /nightly/ dir
    nightly = pages / "nightly"
    nightly.mkdir()
    (nightly / "index.html").write_text("old nightly")

    candidate(pages, art)

    assert not nightly.exists(), "Legacy /nightly/ dir must be removed on first candidate deploy"


# ---------------------------------------------------------------------------
# §6.1 Test 9: candidates.json entries sorted descending by (date, n)
# ---------------------------------------------------------------------------


def test_candidates_sorted_newest_first(tmp_path, monkeypatch):
    pages = tmp_path / "pages"
    pages.mkdir()
    art = make_artifact(tmp_path)

    # Three candidates: two same day, one different day
    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 12, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())
    candidate(pages, art, sha="aaa0000" + "0" * 33)

    monkeypatch.setattr(dwp, "datetime",
        type("dt", (), {"now": staticmethod(lambda tz=None: datetime(2026, 6, 13, 12, 0, 0, tzinfo=timezone.utc)),
                        "strptime": datetime.strptime})())
    candidate(pages, art, sha="bbb1111" + "0" * 33)
    candidate(pages, art, sha="ccc2222" + "0" * 33)

    data = read_candidates(pages)
    entries = data["candidates"]
    assert len(entries) == 3
    # Should be descending by (date, n)
    keys = [(e["date"], e["n"]) for e in entries]
    assert keys == sorted(keys, reverse=True), f"entries not sorted: {keys}"
    assert entries[0]["date"] == "2026-06-13"
    assert entries[0]["n"] == 2
    assert entries[1]["date"] == "2026-06-13"
    assert entries[1]["n"] == 1
    assert entries[2]["date"] == "2026-06-12"
    assert entries[2]["n"] == 1


# ---------------------------------------------------------------------------
# §6.1 Test 10: die() on missing artifact dir
# ---------------------------------------------------------------------------


def test_candidate_die_on_missing_artifact(tmp_path):
    pages = tmp_path / "pages"
    pages.mkdir()
    missing = str(tmp_path / "does_not_exist")

    old = os.getcwd()
    os.chdir(pages)
    try:
        with pytest.raises(SystemExit) as exc_info:
            dwp.cmd_candidate(missing, "abc1234def5678901234567890123456789012ab",
                              "v1.0.0", "1.0.1")
        assert exc_info.value.code == 1
    finally:
        os.chdir(old)
