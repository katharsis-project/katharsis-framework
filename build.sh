#!/bin/bash
set -ev

mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V -Dexamples=true
mvn -pl '!:dropwizard-mongo-example,!:dropwizard-simple-example,!:jersey-example,!:spring-boot-simple-example,!:wildfly-example,!:katharsis-build,!:katharsis-examples,!:katharsis-parent' test jacoco:report coveralls:report
if [ "${TRAVIS_PULL_REQUEST}" != "true" ]; then

    if [[ $TRAVIS_BRANCH == 'master' ]]; then
      # deploy to staging repository
      echo "TODO: ADD DEPLOYMENT TO STAGING REPOSITORY"
    else
      mvn deploy -DskipTests=true --settings settings.xml
    fi
fi