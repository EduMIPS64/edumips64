#!/bin/bash
# Upload script taken from http://sleepycoders.blogspot.ie/2013/03/sharing-travis-ci-generated-files.html

set -e
set -u

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  if [[ "$JAVA_HOME" == *java-8* ]]; then
    echo "Java 8 worker, should update JAR on GitHub pages"
  else
    echo "Non-java 8 worker. Not updating JAR"
    echo "JAVA_HOME=${JAVA_HOME}"
    exit 0
  fi

  echo -n "Cloning git repo.. "
  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis"
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/lupino3/edumips64.git gh-pages > /dev/null
  echo "done."
  
  echo -n "Copying web UI..."
  rm -rf gh-pages/webui  
  mkdir gh-pages/webui
  cp -r src/main/web/dist/* gh-pages/webui
  
  GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

  if [ "$GIT_BRANCH" == "master" ]; then 
    echo -n "Building JAR.. "
    ant latest-jar > /dev/null
    cp edumips64-latest.jar $HOME > /dev/null
    echo "done."
	cd gh-pages
	mv $HOME/edumips64-latest.jar .
  else
    echo "Non-master branch: $GIT_BRANCH. Not updating the JAR."
  fi

  # Add, commit and push files.
  echo -n "Committing files.. "
  git add -f .
  git commit -m "Travis build $TRAVIS_BUILD_NUMBER pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null
  echo "done."
fi
