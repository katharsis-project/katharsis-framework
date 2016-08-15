#!/bin/bash
set -ev

mvn clean install
mvn jacoco:report coveralls:report
if [ "${TRAVIS_PULL_REQUEST}" != "true" ]; then

    if [[ $TRAVIS_BRANCH == 'master' ]]; then
      # deploy to staging repository
      echo "TODO: ADD DEPLOYMENT TO STAGING REPOSITORY"
    else
      mvn deploy -DskipTests=true --settings settings.xml
    fi
fi