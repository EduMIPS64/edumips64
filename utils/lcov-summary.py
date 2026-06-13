#!/usr/bin/env python3
"""Summarise an lcov coverage report into a small Markdown table.

Usage: lcov-summary.py <lcov.info> <output.md>

This script only reads the lcov report (generated data) and writes a Markdown
summary. It performs no network access and executes no project code, so it is
safe to run on untrusted pull request builds.
"""
import sys

# Map lcov "found"/"hit" record prefixes to a human-readable metric label.
METRICS = [
    ("Lines", "LF", "LH"),
    ("Functions", "FNF", "FNH"),
    ("Branches", "BRF", "BRH"),
]


def main():
    if len(sys.argv) != 3:
        sys.stderr.write("usage: lcov-summary.py <lcov.info> <output.md>\n")
        return 1

    lcov_path, out_path = sys.argv[1], sys.argv[2]

    totals = {key: 0 for _, found, hit in METRICS for key in (found, hit)}
    with open(lcov_path, encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if ":" not in line:
                continue
            prefix, _, value = line.partition(":")
            if prefix in totals:
                try:
                    totals[prefix] += int(value)
                except ValueError:
                    pass

    lines = [
        "## Web UI code coverage",
        "",
        "| Metric | Covered | Total | Coverage |",
        "| --- | ---: | ---: | ---: |",
    ]
    for label, found, hit in METRICS:
        total = totals[found]
        covered = totals[hit]
        pct = (100.0 * covered / total) if total else 0.0
        lines.append(f"| {label} | {covered} | {total} | {pct:.1f}% |")

    lines.append("")

    with open(out_path, "w", encoding="utf-8") as out:
        out.write("\n".join(lines) + "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
