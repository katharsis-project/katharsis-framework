#!/bin/bash
set -ev

mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
mvn test -B
if [ "${TRAVIS_PULL_REQUEST}" != "true" ]; then

    if [[ $TRAVIS_BRANCH == 'master' ]]; then
      # deploy to staging repository
      echo "TODO: ADD DEPLOYMENT TO STAGING REPOSITORY"
    else
      mvn deploy -DskipTests=true --settings settings.xml
    fi
fi