#!/bin/bash
# Upload script taken from http://sleepycoders.blogspot.ie/2013/03/sharing-travis-ci-generated-files.html

set -e
set -u

# Debug info.
echo "Starting update-gh-pages.sh"

deploy_webui() {
  # TODO update this with gradle build files
  JS_DIR=$1
  WAR_PATH=bazel-bin/src/main/java/org/edumips64/
  echo "Copying the JS code to ${JS_DIR}."
  mkdir -p ${JS_DIR}
  cp ${WAR_PATH}/webclient.war ${JS_DIR}
  pushd .
  cd ${JS_DIR}
  unzip -o webclient.war
  GIT_REV=$(git rev-parse HEAD)
  echo "<br/><!--Built at Git revision ${GIT_REV}-->" >> edumips64.html
  popd
}

if [ "$TRAVIS_PULL_REQUEST" == "true" ]; then
  echo "Travis invoked from a pull request. Not doing any deployment step."
  return
fi

GH_PAGES_DIR=${HOME}/gh-pages
echo -n "Cloning gh-pages branch to ${GH_PAGES_DIR}.. "
git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis"
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/EduMIPS64/edumips64.git ${GH_PAGES_DIR} > /dev/null
echo "done."

echo "Git branch: $TRAVIS_BRANCH"

# Execute special per-branch actions.
if [ "$TRAVIS_BRANCH" == "master" ]; then
  # Build JAR.
  echo -n "Copying latest JAR to ${GH_PAGES_DIR}.. "
  cp java -jar build/libs/edumips64-*-standalone.jar ${GH_PAGES_DIR}/edumips64-latest.jar
  echo "done."

  # Deploy the last version of the web UI.
  deploy_webui ${GH_PAGES_DIR}
elif [ "$TRAVIS_BRANCH" == "webui-prototype" ]; then
  echo -n "Copying web UI... "
  mkdir -p ${GH_PAGES_DIR}/webui
  rm -rf ${GH_PAGES_DIR}/webui/*
  cp -Rf $HOME/webui/* ${GH_PAGES_DIR}/webui
  echo "done."
fi

# Add, commit and push files.
cd ${GH_PAGES_DIR}
git add -f .
echo "Git status:"
git status
if ! git diff-index --quiet HEAD; then
  echo -n "Committing files.. "
  git commit -m "Travis build $TRAVIS_BUILD_NUMBER pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null
  echo "done."
fi
