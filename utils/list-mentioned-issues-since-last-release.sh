#!/bin/bash

set -e

LAST_TAG=$(git describe --abbrev=0 --tags)
echo "All issues since ${LAST_TAG}"

ISSUES=$(git log HEAD...${LAST_TAG} | egrep "#[0-9]{1,3}" -o | sed -e "s/#//" | sort -n | uniq)
for issue in $ISSUES; do
  curl https://api.github.com/repos/lupino3/edumips64/issues/${issue} 2>/dev/null | jq '[.number, .title, .state] | map(tostring) | join(" - ")' | sed 's/"//g'
done
