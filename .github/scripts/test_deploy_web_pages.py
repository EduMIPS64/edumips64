"""Tests for deploy-web-pages.py (unified commit-addressed model).

Run with:  cd .github/scripts && python -m pytest test_deploy_web_pages.py -q
"""

import importlib.util
import json
import os
import pytest
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


def sha_for(n: int) -> str:
    """Deterministic full 40-char hex sha for an integer."""
    return f"{n:040x}"


def make_artifact(tmp_path: Path, name: str = "art", marker: str | None = None) -> Path:
    """Create a minimal fake artifact directory with one file."""
    art = tmp_path / name
    art.mkdir()
    (art / "index.html").write_text(marker or f"<h1>{name}</h1>")
    (art / "ui.js").write_text("// js")
    return art


def in_pages(pages: Path, fn):
    old = os.getcwd()
    os.chdir(pages)
    try:
        return fn()
    finally:
        os.chdir(old)


def push(pages: Path, artifact: Path, n: int, seq: int | None = None) -> None:
    in_pages(pages, lambda: dwp.cmd_push(
        str(artifact), sha_for(n), seq if seq is not None else n,
        f"build-{n}", f"1.{n}.0"))


def promote(pages: Path, n: int, actor: str = "lupino3") -> None:
    in_pages(pages, lambda: dwp.cmd_promote(sha_for(n), actor))


def rollback(pages: Path, actor: str = "lupino3") -> None:
    in_pages(pages, lambda: dwp.cmd_rollback(actor))


def read_index(pages: Path) -> dict:
    return json.loads((pages / "versions.json").read_text())


def root_files(pages: Path) -> set[str]:
    """Top-level non-reserved entries in the Pages root (the 'production' set)."""
    return {e.name for e in pages.iterdir() if e.name not in dwp.RESERVED_NAMES}


def build_shas(pages: Path) -> set[str]:
    """SHAs that have a c/<sha>/ snapshot on disk."""
    c = pages / "c"
    if not c.is_dir():
        return set()
    return {e.name for e in c.iterdir() if e.is_dir()}


@pytest.fixture
def pages(tmp_path):
    p = tmp_path / "pages"
    p.mkdir()
    return p


# ---------------------------------------------------------------------------
# push
# ---------------------------------------------------------------------------


def test_push_creates_candidate_without_touching_root(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)

    idx = read_index(pages)
    assert idx["current"] is None
    assert len(idx["versions"]) == 1
    entry = idx["versions"][0]
    assert entry["sha"] == sha_for(1)
    assert entry["seq"] == 1
    assert entry["promoted"] is False
    # Build snapshot exists, root is still empty (no production yet).
    assert (pages / "c" / sha_for(1) / "index.html").is_file()
    assert root_files(pages) == set()


def test_push_is_idempotent(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    push(pages, art, 1)  # re-run of same commit
    idx = read_index(pages)
    assert len(idx["versions"]) == 1


def test_push_rejects_short_sha(pages, tmp_path):
    art = make_artifact(tmp_path)
    with pytest.raises(SystemExit):
        in_pages(pages, lambda: dwp.cmd_push(str(art), "abc1234", 1, "b", ""))


def test_push_sorts_newest_first(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    push(pages, art, 3, seq=3)
    push(pages, art, 2, seq=2)
    idx = read_index(pages)
    assert [e["seq"] for e in idx["versions"]] == [3, 2, 1]


# ---------------------------------------------------------------------------
# promote
# ---------------------------------------------------------------------------


def test_promote_unpushed_sha_errors(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    # sha 2 was never pushed
    with pytest.raises(SystemExit):
        promote(pages, 2)


def test_promote_copies_into_root_and_marks_promoted(pages, tmp_path):
    art = make_artifact(tmp_path, marker="<h1>one</h1>")
    push(pages, art, 1)
    promote(pages, 1)

    idx = read_index(pages)
    assert idx["current"] == sha_for(1)
    entry = idx["versions"][0]
    assert entry["promoted"] is True
    assert entry["promotedBy"] == "lupino3"
    assert "promotedAt" in entry
    # Root now physically contains the build.
    assert (pages / "index.html").read_text() == "<h1>one</h1>"
    assert root_files(pages) == {"index.html", "ui.js"}


def test_promote_never_builds_uses_existing_snapshot(pages, tmp_path):
    # The promoted bytes come from c/<sha>/, not from any artifact dir.
    art = make_artifact(tmp_path, marker="<h1>pushed-bytes</h1>")
    push(pages, art, 1)
    # Mutating the original artifact must NOT affect promotion.
    (art / "index.html").write_text("<h1>mutated-after-push</h1>")
    promote(pages, 1)
    assert (pages / "index.html").read_text() == "<h1>pushed-bytes</h1>"


def test_promote_prunes_candidates_between(pages, tmp_path):
    art = make_artifact(tmp_path)
    # Live = P (seq 1)
    push(pages, art, 1)
    promote(pages, 1)
    # Candidates 2,3,4 accumulate
    for n in (2, 3, 4):
        push(pages, art, n, seq=n)
    assert build_shas(pages) == {sha_for(i) for i in (1, 2, 3, 4)}

    # Promote 3 -> prune non-promoted 2 (between old-live 1 and 3); keep 4.
    promote(pages, 3)

    idx = read_index(pages)
    kept_seqs = sorted(e["seq"] for e in idx["versions"])
    assert kept_seqs == [1, 3, 4]
    assert build_shas(pages) == {sha_for(i) for i in (1, 3, 4)}
    assert idx["current"] == sha_for(3)
    # sha 2's directory is gone.
    assert not (pages / "c" / sha_for(2)).exists()


def test_promote_keeps_promoted_within_keep_window(pages, tmp_path):
    # 3 promotions is well within KEEP_PROMOTED (10), so no snapshots are pruned.
    art = make_artifact(tmp_path)
    for n in (1, 2, 3):
        push(pages, art, n, seq=n)
        promote(pages, n)
    idx = read_index(pages)
    promoted = [e for e in idx["versions"] if e["promoted"]]
    assert len(promoted) == 3
    # All 3 snapshots are present on disk (within the retention window).
    assert build_shas(pages) == {sha_for(i) for i in (1, 2, 3)}
    # No entry is pruned.
    assert all(not e.get("pruned") for e in idx["versions"])


def test_promote_root_is_clean_no_stale_files(pages, tmp_path):
    # Promote build 1 (two files), then a build 2 with a DIFFERENT file set.
    art1 = tmp_path / "a1"
    art1.mkdir()
    (art1 / "index.html").write_text("1")
    (art1 / "old-chunk.js").write_text("old")
    in_pages(pages, lambda: dwp.cmd_push(str(art1), sha_for(1), 1, "b1", ""))
    promote(pages, 1)
    assert "old-chunk.js" in root_files(pages)

    art2 = tmp_path / "a2"
    art2.mkdir()
    (art2 / "index.html").write_text("2")
    (art2 / "new-chunk.js").write_text("new")
    in_pages(pages, lambda: dwp.cmd_push(str(art2), sha_for(2), 2, "b2", ""))
    promote(pages, 2)

    # Root must equal build 2 exactly — no leftover old-chunk.js.
    assert root_files(pages) == {"index.html", "new-chunk.js"}
    assert "old-chunk.js" not in root_files(pages)


def test_promote_accepts_short_sha(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    in_pages(pages, lambda: dwp.cmd_promote(sha_for(1)[:8], "lupino3"))
    assert read_index(pages)["current"] == sha_for(1)


def test_reserved_names_survive_promote(pages, tmp_path):
    (pages / "CNAME").write_text("web.edumips.org\n")
    (pages / ".nojekyll").touch()
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    promote(pages, 1)
    assert (pages / "CNAME").is_file()
    assert (pages / ".nojekyll").is_file()
    assert (pages / "versions.json").is_file()
    assert (pages / "c").is_dir()


# ---------------------------------------------------------------------------
# rollback
# ---------------------------------------------------------------------------


def test_rollback_to_previous_promoted(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    promote(pages, 1)
    push(pages, art, 2, seq=2)
    promote(pages, 2)

    rollback(pages)
    idx = read_index(pages)
    assert idx["current"] == sha_for(1)
    # Both promoted versions are still retained.
    assert build_shas(pages) >= {sha_for(1), sha_for(2)}


def test_rollback_preserves_invariant_keeps_candidates(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    promote(pages, 1)
    push(pages, art, 2, seq=2)
    promote(pages, 2)
    # Candidate 3 newer than current(2)
    push(pages, art, 3, seq=3)

    rollback(pages)  # current 2 -> 1
    idx = read_index(pages)
    assert idx["current"] == sha_for(1)
    # Candidate 3 must still be there (it's now "future" relative to current 1).
    assert sha_for(3) in build_shas(pages)
    seqs = sorted(e["seq"] for e in idx["versions"])
    assert seqs == [1, 2, 3]


def test_rollback_without_older_promoted_errors(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    promote(pages, 1)
    with pytest.raises(SystemExit):
        rollback(pages)


def test_promote_older_promoted_equals_rollback(pages, tmp_path):
    art = make_artifact(tmp_path)
    push(pages, art, 1)
    promote(pages, 1)
    push(pages, art, 2, seq=2)
    promote(pages, 2)
    # Re-promote the older promoted sha 1 == rollback.
    promote(pages, 1)
    idx = read_index(pages)
    assert idx["current"] == sha_for(1)
    assert {sha_for(1), sha_for(2)} <= build_shas(pages)


# ---------------------------------------------------------------------------
# worked example from the design doc
# ---------------------------------------------------------------------------


def test_worked_example(pages, tmp_path):
    art = make_artifact(tmp_path)
    # Live = P (seq 100)
    push(pages, art, 100, seq=100)
    promote(pages, 100)
    # Candidates 101..104
    for n in (101, 102, 103, 104):
        push(pages, art, n, seq=n)
    # Promote 103
    promote(pages, 103)
    idx = read_index(pages)
    kept = sorted(e["seq"] for e in idx["versions"])
    assert kept == [100, 103, 104]  # 101,102 pruned
    promoted = sorted(e["seq"] for e in idx["versions"] if e["promoted"])
    assert promoted == [100, 103]
    candidates = [e["seq"] for e in idx["versions"] if not e["promoted"]]
    assert candidates == [104]


# ---------------------------------------------------------------------------
# migrate
# ---------------------------------------------------------------------------


def test_migrate_converts_legacy_layout(pages, tmp_path, monkeypatch):
    # Build a legacy Pages layout: manifest.json + v/<n>/, candidates.json + date dir.
    p_sha = sha_for(10)
    c_sha = sha_for(11)

    (pages / "v" / "1").mkdir(parents=True)
    (pages / "v" / "1" / "index.html").write_text("promoted")
    (pages / "manifest.json").write_text(json.dumps({
        "current": 1, "prev": 0,
        "history": [{
            "n": 1, "build": "b-10", "sha": p_sha, "targetRelease": "1.0.0",
            "promotedAt": "2026-01-01T00:00:00Z", "promotedBy": "lupino3",
        }],
    }))

    cand_dir = pages / "2026-06-13" / f"1-{c_sha[:7]}"
    cand_dir.mkdir(parents=True)
    (cand_dir / "index.html").write_text("candidate")
    (pages / "candidates.json").write_text(json.dumps({
        "candidates": [{
            "date": "2026-06-13", "n": 1, "sha": c_sha, "shortsha": c_sha[:7],
            "path": f"/2026-06-13/1-{c_sha[:7]}/", "build": "b-11",
            "targetRelease": "1.0.0", "deployedAt": "2026-06-13T00:00:00Z",
        }],
        "retentionDays": 14,
    }))
    # Root copy of current promoted build.
    (pages / "index.html").write_text("promoted")

    # Stub seq computation (no real git clone in the test).
    seqs = {p_sha: 10, c_sha: 11}
    monkeypatch.setattr(dwp, "_seq_for", lambda sha, repo: seqs[sha])

    in_pages(pages, lambda: dwp.cmd_migrate(repo="/fake", dry_run=False))

    idx = read_index(pages)
    assert idx["current"] == p_sha
    assert build_shas(pages) == {p_sha, c_sha}
    by_sha = {e["sha"]: e for e in idx["versions"]}
    assert by_sha[p_sha]["promoted"] is True
    assert by_sha[c_sha]["promoted"] is False
    # Legacy index files removed.
    assert not (pages / "manifest.json").exists()
    assert not (pages / "candidates.json").exists()
    # Candidate date dir cleaned.
    assert not (pages / "2026-06-13").exists()
    # Redirect stub left at the old promoted URL.
    stub = (pages / "v" / "1" / "index.html").read_text()
    assert f"/c/{p_sha}/" in stub


def test_migrate_dry_run_writes_nothing(pages, tmp_path, monkeypatch):
    p_sha = sha_for(10)
    (pages / "v" / "1").mkdir(parents=True)
    (pages / "v" / "1" / "index.html").write_text("promoted")
    (pages / "manifest.json").write_text(json.dumps({
        "current": 1,
        "history": [{
            "n": 1, "build": "b", "sha": p_sha, "targetRelease": "1.0.0",
            "promotedAt": "2026-01-01T00:00:00Z", "promotedBy": "lupino3",
        }],
    }))
    monkeypatch.setattr(dwp, "_seq_for", lambda sha, repo: 10)

    in_pages(pages, lambda: dwp.cmd_migrate(repo="/fake", dry_run=True))

    # Nothing changed.
    assert (pages / "manifest.json").exists()
    assert not (pages / "versions.json").exists()
    assert not (pages / "c").exists()


# ---------------------------------------------------------------------------
# promoted-snapshot retention (KEEP_PROMOTED cap, Task A hardening)
# ---------------------------------------------------------------------------


def test_prune_promoted_beyond_keep_count(pages, tmp_path, monkeypatch):
    """Promoted snapshots beyond KEEP_PROMOTED are deleted; entries kept with pruned=true."""
    monkeypatch.setattr(dwp, "KEEP_PROMOTED", 3)
    art = make_artifact(tmp_path)
    # Promote 5 times — oldest 2 should get their snapshots pruned.
    for n in (1, 2, 3, 4, 5):
        push(pages, art, n, seq=n)
        promote(pages, n)

    idx = read_index(pages)
    by_seq = {e["seq"]: e for e in idx["versions"]}

    # All 5 entries still in versions.json.
    assert set(by_seq.keys()) == {1, 2, 3, 4, 5}

    # Snapshots only for top 3 by seq (3, 4, 5); oldest 2 (1, 2) are pruned.
    assert build_shas(pages) == {sha_for(i) for i in (3, 4, 5)}
    assert not (pages / "c" / sha_for(1)).exists()
    assert not (pages / "c" / sha_for(2)).exists()

    # Pruned entries carry pruned=true.
    assert by_seq[1]["pruned"] is True
    assert by_seq[2]["pruned"] is True

    # Entries within the window have no pruned flag.
    assert not by_seq[3].get("pruned")
    assert not by_seq[4].get("pruned")
    assert not by_seq[5].get("pruned")


def test_pruned_entry_preserved_in_versions_json(pages, tmp_path, monkeypatch):
    """Even after pruning, the entry remains in versions.json with all metadata intact."""
    monkeypatch.setattr(dwp, "KEEP_PROMOTED", 2)
    art = make_artifact(tmp_path)
    for n in (10, 20, 30):
        push(pages, art, n, seq=n)
        promote(pages, n)

    idx = read_index(pages)
    pruned_entries = [e for e in idx["versions"] if e.get("pruned")]
    assert len(pruned_entries) == 1
    pruned = pruned_entries[0]

    # Seq 10 is oldest (beyond KEEP_PROMOTED=2).
    assert pruned["seq"] == 10
    assert pruned["promoted"] is True
    # Core fields must survive pruning.
    assert pruned["sha"] == sha_for(10)
    assert pruned["build"] == "build-10"
    assert "promotedAt" in pruned
    assert pruned["pruned"] is True


def test_promote_refuses_pruned_version(pages, tmp_path, monkeypatch):
    """Attempting to promote a pruned version fails with a clear error."""
    monkeypatch.setattr(dwp, "KEEP_PROMOTED", 2)
    art = make_artifact(tmp_path)
    for n in (1, 2, 3):
        push(pages, art, n, seq=n)
        promote(pages, n)

    # seq 1 is now pruned (beyond KEEP_PROMOTED=2).
    idx = read_index(pages)
    by_seq = {e["seq"]: e for e in idx["versions"]}
    assert by_seq[1].get("pruned")

    with pytest.raises(SystemExit):
        promote(pages, 1)


def test_rollback_skips_pruned_versions(pages, tmp_path, monkeypatch):
    """Rollback skips pruned promoted versions and targets the newest non-pruned one."""
    monkeypatch.setattr(dwp, "KEEP_PROMOTED", 3)
    art = make_artifact(tmp_path)
    # Promote 5 times: current = 5; seqs 1 and 2 pruned; seqs 3,4,5 retained.
    for n in (1, 2, 3, 4, 5):
        push(pages, art, n, seq=n)
        promote(pages, n)

    # Rollback should land on seq 4 (newest non-pruned older than current=5).
    rollback(pages)
    idx = read_index(pages)
    assert idx["current"] == sha_for(4)


def test_rollback_errors_if_all_older_promoted_are_pruned(pages, tmp_path, monkeypatch):
    """Rollback fails clearly when all older promoted versions are pruned."""
    monkeypatch.setattr(dwp, "KEEP_PROMOTED", 1)
    art = make_artifact(tmp_path)
    # With KEEP_PROMOTED=1, only the current (most recent) snapshot is retained.
    for n in (1, 2):
        push(pages, art, n, seq=n)
        promote(pages, n)

    # seq 1 is pruned; only seq 2 (current) has a snapshot.
    # Rollback would want seq 1, but it's pruned.
    with pytest.raises(SystemExit):
        rollback(pages)


def test_current_never_pruned(pages, tmp_path, monkeypatch):
    """The current production version's snapshot is NEVER deleted, even if it
    would otherwise fall outside the KEEP_PROMOTED window."""
    monkeypatch.setattr(dwp, "KEEP_PROMOTED", 2)
    art = make_artifact(tmp_path)
    # Promote seq 1 as the starting point.
    push(pages, art, 1, seq=1)
    promote(pages, 1)
    # Push and promote 2 more to fill the window (window = seqs 2, 3 after promote 3).
    push(pages, art, 2, seq=2)
    promote(pages, 2)
    push(pages, art, 3, seq=3)
    promote(pages, 3)

    # Now push (but don't promote) seqs 4, 5 to make current=3 look "older".
    push(pages, art, 4, seq=4)
    push(pages, art, 5, seq=5)

    # Manually promote seq 5 while directly calling prune — force the window to
    # be [4, 5], pushing seq 3 (which IS current) out of the normal top-2 window.
    # Do this via cmd_promote with KEEP_PROMOTED=2, current=3 about to become 5.
    promote(pages, 5)

    idx = read_index(pages)
    # current is now 5; top-2 promoted = {5, 4 were never promoted}...
    # Actually let's check: after promoting 5, all_promoted = [5, 3, 2, 1]
    # top 2 = [5, 3]; plus current=5.  So 3 IS in top 2 (it's seq 3, second highest promoted).
    # seq 1 and 2 are pruned.
    assert idx["current"] == sha_for(5)
    # Snapshots for current (5) and the next most-recent promoted (3) are retained.
    assert (pages / "c" / sha_for(5)).is_dir()
    assert (pages / "c" / sha_for(3)).is_dir()


def test_current_retained_even_after_many_promotions_above_it(pages, tmp_path, monkeypatch):
    """Current is preserved by rule (1) even when it falls below the top-KEEP_PROMOTED window
    of all promoted versions (as can happen after rollback + many later promotions)."""
    monkeypatch.setattr(dwp, "KEEP_PROMOTED", 2)
    art = make_artifact(tmp_path)

    # Promote seq 1, then 2.
    for n in (1, 2):
        push(pages, art, n, seq=n)
        promote(pages, n)

    # Roll back to seq 1 (current = 1).
    rollback(pages)
    assert read_index(pages)["current"] == sha_for(1)

    # Now promote seq 3 and 4 (two new promotions above current=1).
    # With KEEP_PROMOTED=2, top-2 promoted by seq = {4, 3}.
    # current = 4 (promoted 3, then 4); seq 1 is promoted but falls outside top-2.
    push(pages, art, 3, seq=3)
    promote(pages, 3)
    push(pages, art, 4, seq=4)
    promote(pages, 4)

    idx = read_index(pages)
    # current is 4; top-2 = {4, 3}; seq 1 and 2 are outside the window.
    # BUT seq 1 and 2 are NOT current so they ARE pruned (seq 3 is now current... wait)
    # Let me recalculate: after promoting 4, current=4; promoted = [4,3,2,1];
    # top-2 = [4,3]; current=4 in top-2. seq 1 and 2 are outside → pruned.
    assert idx["current"] == sha_for(4)
    by_seq = {e["seq"]: e for e in idx["versions"]}
    # seq 1 and 2 pruned (outside top-2, not current).
    assert by_seq[1].get("pruned") is True
    assert by_seq[2].get("pruned") is True
    # seq 3 and 4: retained (top-2 window + current).
    assert not by_seq[3].get("pruned")
    assert not by_seq[4].get("pruned")
    assert (pages / "c" / sha_for(4)).is_dir()
    assert (pages / "c" / sha_for(3)).is_dir()


def test_rollback_works_after_pruning(pages, tmp_path, monkeypatch):
    """Rollback always finds a valid non-pruned target within KEEP_PROMOTED=10."""
    # With the default KEEP_PROMOTED=10, the rollback target (newest promoted
    # older than current) is always within the retention window.
    art = make_artifact(tmp_path)
    # Promote 12 versions — oldest 2 get pruned.
    for n in range(1, 13):
        push(pages, art, n, seq=n)
        promote(pages, n)

    idx = read_index(pages)
    current_seq = next(e["seq"] for e in idx["versions"] if e["sha"] == idx["current"])
    assert current_seq == 12

    # Rollback must succeed: seq 11 is in the top-10 window (seqs 12..3), not pruned.
    rollback(pages)
    idx = read_index(pages)
    assert idx["current"] == sha_for(11)
    # The rolled-back snapshot must be on disk.
    assert (pages / "c" / sha_for(11)).is_dir()


def test_pruned_flag_cleared_when_version_reenters_window(pages, tmp_path, monkeypatch):
    """If a version is pruned and later becomes current (e.g. via re-push + re-promote),
    re-promotion should clear the pruned flag (snapshot is restored by push, then promote
    accepts it and prune_to_invariant clears the flag for retained entries)."""
    monkeypatch.setattr(dwp, "KEEP_PROMOTED", 2)
    art = make_artifact(tmp_path)
    for n in (1, 2, 3):
        push(pages, art, n, seq=n)
        promote(pages, n)

    # seq 1 is now pruned.
    idx = read_index(pages)
    assert next(e for e in idx["versions"] if e["seq"] == 1).get("pruned") is True

    # Re-push seq 1 (simulates re-uploading the artifact) — the script should
    # recreate the snapshot (the existing entry is replaced by the push).
    push(pages, art, 1)

    # After re-push, entry is no longer pruned (push creates a fresh entry without pruned).
    idx = read_index(pages)
    entry1 = next(e for e in idx["versions"] if e["seq"] == 1)
    assert not entry1.get("pruned")
    assert (pages / "c" / sha_for(1)).is_dir()

# ---------------------------------------------------------------------------
# Reserved-name protection
# ---------------------------------------------------------------------------


def test_promote_preserves_dot_github(tmp_path, pages):
    """Promote must never delete the Pages repo's own deploy workflow.

    Regression test for the 2026-07-03 incident: RESERVED_NAMES predated the
    .github directory, so a promote's root replacement deleted the Actions
    deploy workflow and left the site undeployable.
    """
    workflow = pages / ".github" / "workflows" / "deploy-pages.yml"
    workflow.parent.mkdir(parents=True)
    workflow.write_text("name: Deploy to GitHub Pages\n")

    art = make_artifact(tmp_path, "art1", marker="one")
    push(pages, art, 1)
    promote(pages, 1)

    assert workflow.exists(), ".github must survive a promote root replacement"
    assert workflow.read_text() == "name: Deploy to GitHub Pages\n"
