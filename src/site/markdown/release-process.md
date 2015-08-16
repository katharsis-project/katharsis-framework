# Release Process

## Finishing a release branch

        $ git checkout master
        Switched to branch 'master'
        $ git merge --no-ff release-0.1.1
        Merge made by recursive.
        (Summary of changes)
        $ git tag -a 0.1.1

Need to merge those back into develop to merge hardening changes:

        $ git checkout develop
        Switched to branch 'develop'
        $ git merge --no-ff release-0.1.1
        Merge made by recursive.
        (Summary of changes)

Done and the release branch may be removed:

        $ git branch -d release-1.2
        Deleted branch release-1.2 (was ff452fe)


## Deploying to Maven Central

        $ git checkout master
        $ mvn -Possrh-release clean deploy


## Deploying to Sonatype Snapshot Repository

NOTE: The develop branch MUST have '-SNAPSHOT' version!!!

        $ git checkout develop
        $ mvn -Possrh-release clean deploy
        (After inspecting the staging repository content...)
        $ mvn -Possrh-release nexus-staging:release

You should add the following repository configuration in other projects to use the snapshot dependency:

        <repository>
          <id>sonatype-nexus-snapshots</id>
          <name>Sonatype Nexus Snapshots</name>
          <url>http://oss.sonatype.org/content/repositories/snapshots</url>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>


## References
- http://nvie.com/posts/a-successful-git-branching-model/#finishing-a-release-branch
- http://central.sonatype.org/pages/apache-maven.html
