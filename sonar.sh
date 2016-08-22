#!/bin/bash
set -ev

#INITIAL_VERSION=`maven_expression "project.version"`
#
#if [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
#    mvn org.jacoco:jacoco-maven-plugin:prepare-agent sonar:sonar \
#      -Dsonar.login=$SONAR_TOKEN \
#      -Dsonar.projectVersion=$INITIAL_VERSION
#
#el
if [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
    echo 'Internal pull request: trigger QA and analysis'

    mvn org.jacoco:jacoco-maven-plugin:prepare-agent deploy sonar:sonar \
        $MAVEN_OPTIONS \
        -Dsource.skip=true \
        -Dsonar.analysis.mode=issues \
        -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
        -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
        -Dsonar.github.oauth=$GITHUB_TOKEN \
        -Dsonar.login=$SONAR_TOKEN

else
     mvn org.jacoco:jacoco-maven-plugin:prepare-agent sonar:sonar -Dsonar.login=$SONAR_TOKEN
fi
