#!/bin/sh
#
# Lists the MIPS64 instructions implemented in the simulator and compares that
# list against the instructions actually referenced in the assembly test files
# under src/test/resources. Exits non-zero if any implemented instruction is
# not exercised by at least one test, which lets the CI pipeline enforce that
# every instruction is covered by a unit test.

set -u

TESTDIR="src/test/resources"
TESTED_FILENAME="/tmp/tested-instructions$(date +%F-%T).txt"
ISDIR="src/main/java/org/edumips64/core/is"
IS_FILENAME="/tmp/instructions-$(date +%F-%T).txt"
UNTESTED_FILENAME="/tmp/untested-instructions-$(date +%F-%T).txt"

# Instructions that look untested to this script but are intentionally not
# user-addressable opcodes:
#   - BUBBLE is an internal pseudo-instruction inserted by the pipeline to
#     represent stalls; it cannot appear in assembly source.
#   - DDIV3 is the Java class name for the 3-operand form of DDIV. The
#     mnemonic written in assembly is DDIV, which is already exercised by
#     the DDIV tests.
EXCLUDE_REGEX="^(BUBBLE|DDIV3)$"

find "${TESTDIR}" -name "*.s" | xargs cat | sed "s/;.*$//" | sed "s/\s/ /g" | sed "s/^\s*//g" | sed "s/^.*://g" | cut -d " " -f 1 | grep -v "^[.;#/\\]" | tr "a-z" "A-Z" | grep -v "^$" | sort -u > "${TESTED_FILENAME}"
echo "Tested instructions: $(wc -l < "${TESTED_FILENAME}")"

grep -ri class.*extends src/main/java/org/edumips64/core/is/ | grep -v Exception | grep -v abstract | sed "s/^.*class//" | cut -d" " -f 2 | sed "s/_/./g" | grep -vE "${EXCLUDE_REGEX}" | sort -u > "${IS_FILENAME}"
echo "Total instructions in the instruction set (excluding non-user-addressable ones): $(wc -l < "${IS_FILENAME}")"

diff -u "${IS_FILENAME}" "${TESTED_FILENAME}" | grep "^-" | grep -v -- "^---" | grep -v -- "- " | sed "s/^-//" > "${UNTESTED_FILENAME}"
untested_count=$(wc -l < "${UNTESTED_FILENAME}")
echo "Untested instructions: ${untested_count}"
cat "${UNTESTED_FILENAME}"

if [ "${untested_count}" -ne 0 ]; then
  echo
  echo "ERROR: the instructions listed above are implemented under ${ISDIR}"
  echo "but are never used in any .s file under ${TESTDIR}. Every instruction"
  echo "must be exercised by at least one end-to-end test. See the developer"
  echo "guide (docs/developer-guide.md) for details."
  exit 1
fi
