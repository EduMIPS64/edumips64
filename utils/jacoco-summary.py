#!/usr/bin/env python3
"""Summarise a JaCoCo XML report into a small Markdown table.

Usage: jacoco-summary.py <jacocoTestReport.xml> <output.md>

This script only reads the JaCoCo report (which is generated data) and writes a
Markdown summary. It performs no network access and executes no project code, so
it is safe to run on untrusted pull request builds.
"""
import sys
import xml.etree.ElementTree as etree  # nosec B405 -- parses only the locally generated JaCoCo report

COUNTERS = ["INSTRUCTION", "BRANCH", "LINE", "METHOD", "CLASS"]


def main():
    if len(sys.argv) != 3:
        sys.stderr.write("usage: jacoco-summary.py <report.xml> <output.md>\n")
        return 1

    report_path, out_path = sys.argv[1], sys.argv[2]
    root = etree.parse(report_path).getroot()  # nosec B314 -- trusted local file

    totals = {}
    for counter in root.findall("counter"):
        ctype = counter.get("type")
        missed = int(counter.get("missed", "0"))
        covered = int(counter.get("covered", "0"))
        totals[ctype] = (covered, missed + covered)

    lines = ["## Java code coverage", "", "| Metric | Covered | Total | Coverage |", "| --- | ---: | ---: | ---: |"]
    for ctype in COUNTERS:
        covered, total = totals.get(ctype, (0, 0))
        pct = (100.0 * covered / total) if total else 0.0
        label = ctype.capitalize()
        lines.append(f"| {label} | {covered} | {total} | {pct:.1f}% |")

    lines.append("")
    lines.append(
        "_The Swing UI is excluded from coverage (see developer-guide.md)._"
    )

    with open(out_path, "w", encoding="utf-8") as fh:
        fh.write("\n".join(lines) + "\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
