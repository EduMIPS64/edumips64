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


def test_promote_keeps_all_promoted_forever(pages, tmp_path):
    art = make_artifact(tmp_path)
    for n in (1, 2, 3):
        push(pages, art, n, seq=n)
        promote(pages, n)
    idx = read_index(pages)
    promoted = [e for e in idx["versions"] if e["promoted"]]
    assert len(promoted) == 3  # none pruned


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
