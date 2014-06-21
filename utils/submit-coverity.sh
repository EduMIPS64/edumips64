#!/bin/bash
set -e

TOKEN_FILE=~/.coverity-token

out() {
  local _bold=$(tput bold)
  local _clear=$(tput sgr0)
  echo "${_bold}*** $@${_clear}"
}

if [ ! -f ${TOKEN_FILE} ]; then
  echo "Please put your Coverity token in ${TOKEN_FILE}." >&2
  exit 1
fi

TOKEN=$(cat ${TOKEN_FILE})

DESCRIPTION="EduMIPS64 Coverity Scan"
VERSION=$(git describe HEAD --tags)
TARBALL=edumips64-${VERSION}.tar.gz
COVERITY_DIR=cov-int

out "Cleaning up ${COVERITY_DIR} before running cov-build"
rm -rf ${COVERITY_DIR}

out "Executing local Coverity helper (cov-build).."
cov-build --dir ${COVERITY_DIR} ant

out "Packing results.."
tar zcvf ${TARBALL} ${COVERITY_DIR}

out "Done. Tarball stats:"
ls -lh ${TARBALL}

out "Uploading tarball to Coverity"
curl --form project=lupino3%2Fedumips64 \
     --form token=${TOKEN} \
     --form email=andrea.spadaccini@gmail.com \
     --form file=@${TARBALL} \
     --form version="${VERSION}" \
     --form description="${DESCRIPTION}" \
     https://scan.coverity.com/builds?project=lupino3%2Fedumips64

out "Cleaning up.."
rm -rf ${COVERITY_DIR}
rm ${TARBALL}
