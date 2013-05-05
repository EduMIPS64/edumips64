# Upload script taken from http://sleepycoders.blogspot.ie/2013/03/sharing-travis-ci-generated-files.html

if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo "Starting to update gh-pages"
  echo "Building JAR"

  ant latest-jar
  cp edumips64-latest.jar $HOME

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis"

  # Use token to clone gh-pages branch
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/lupino3/edumips64.git gh-pages > /dev/null

  cd gh-pages
  mv $HOME/edumips64-latest.jar .

  # Add, commit and push files.
  git add -f .
  git commit -m "Travis build $TRAVIS_BUILD_NUMBER pushed to gh-pages"
  git push -fq origin gh-pages > /dev/null
  echo "Done."
fi
