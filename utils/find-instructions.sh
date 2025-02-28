#!/bin/sh

TESTDIR="src/test/resources"
TESTED_FILENAME="/tmp/tested-instructions$(date +%F-%T).txt"
ISDIR="src/main/java/org/edumips64/core/is"
IS_FILENAME="/tmp/instructions-$(date +%F-%T).txt"
UNTESTED_FILENAME="/tmp/untested-instructions-$(date +%F-%T).txt"

find ${TESTDIR} -name "*.s" | xargs cat | sed "s/\s/ /g" | sed "s/^\s*//g" | sed "s/^.*://g" | cut -d " " -f 1 | grep -v "^[.;#/\\]" | tr "a-z" "A-Z" | grep -v "^$" | sort -u > ${TESTED_FILENAME}
echo "Tested instructions: $(wc -l ${TESTED_FILENAME})"

grep -ri class.*extends src/main/java/org/edumips64/core/is/ | grep -v Exception | grep -v abstract | sed "s/^.*class//" | cut -d" " -f 2 | sed "s/_/./g" | sort -u > ${IS_FILENAME}
echo "Total instructions in the instruction set: $(wc -l ${IS_FILENAME})"

diff -u ${IS_FILENAME} ${TESTED_FILENAME} | grep "^-" | grep -v -- "^---" | grep -v -- "- " | sed "s/^-//" > ${UNTESTED_FILENAME}
echo "Untested instructions: $(wc -l ${UNTESTED_FILENAME})"
cat ${UNTESTED_FILENAME}
