# io.katharsis:katharsis-servlet v2.0.4-SNAPSHOT git changelog

2015-11-24    katharsis-core-172 added changelog (Patryk Orwat)  

**v2.0.3**  
2015-11-22    v2.0.3 (Patryk Orwat)  
2015-11-22    katharsis-core-167 servlet query sting fix (Patryk Orwat)  
2015-11-22    katharsis-core-167 servlet query sting is always encoded (Patryk Orwat)  
2015-11-22    katharsis-core-167 query params can be decoded by servlet implementations (Patryk Orwat)  
2015-11-22    katharsis-core-167 UTF-8 encode all the things! (Patryk Orwat)  

**v2.0.2**  
2015-11-20    v2.0.2 (Patryk Orwat)  
2015-11-20    v2.0.2 (Patryk Orwat)  
2015-11-20    [#11](https://github.com/katharsis-project/katharsis-rs/issues/11) - Need to URL Decode the query string from the request before parsing into QueryParams (Błażej Krysiak)  

**v2.0.1**  
2015-11-19    v2.0.1 (Patryk Orwat)  

**v2.0.0**  
2015-11-15    v2.0.0 (Patryk Orwat)  
2015-11-14    katharsis-spring-2 Spring adjustments (Patryk Orwat)  
2015-11-13    katharsis-core-157 ServletParameterProvider refactor (Patryk Orwat)  
2015-11-01    katharsis-core-114 adjusted to changes in the core (Patryk Orwat)  
2015-10-29    katharsis-core-135 repo interface update (Patryk Orwat)  
2015-10-25    [#9](https://github.com/katharsis-project/katharsis-rs/issues/9) added parameter provider (Patryk Orwat)  

**v1.0.1**  
2015-10-14    v1.0.1 (Patryk Orwat)  

**v1.0.0**  
2015-10-01    v1.0.0 (Patryk Orwat)  
2015-09-16    italicize type name (Woonsan Ko)  
2015-09-16    adding more description about spring IoC integration with example links (Woonsan Ko)  
2015-09-07    added snapshots deployment (Patryk Orwat)  

**v0.9.4**  
2015-08-30    v0.9.4 (Patryk Orwat)  
2015-08-26    [#2](https://github.com/katharsis-project/katharsis-rs/issues/2) adding settings.xml (Woonsan Ko)  
2015-08-25    using katharsis prefixed properties in filter/servlet init params (Woonsan Ko)  
2015-08-24    issue [#2](https://github.com/katharsis-project/katharsis-rs/issues/2), correcting develop branch name (Woonsan Ko)  
2015-08-24    issue [#2](https://github.com/katharsis-project/katharsis-rs/issues/2), renaming packages (Woonsan Ko)  
2015-08-17    simple section name change (Woonsan Ko)  
2015-08-17    re-indenting java example code (Woonsan Ko)  
2015-08-17    adding changes.xml (Woonsan Ko)  
2015-08-16    Update release-process.md (Woonsan Ko)  
2015-08-16    updating release-process (Woonsan Ko)  
2015-08-16    bump up nexus-staging-maven-plugin to work with java18 (Woonsan Ko)  
2015-08-16    adding release-process info (Woonsan Ko)  
2015-08-16    bump up for next dev cycle (Woonsan Ko)  
2015-08-16    Bumped version number to 0.1.1 (Woonsan Ko)  
2015-08-16    adding version compatibility (Woonsan Ko)  
2015-08-15    Exception handling changes based upon katharsis-core-0.9.3 (Woonsan Ko)  
2015-08-15    upgrading katharsis-core dep to 0.9.3; closing request entity stream (Woonsan Ko)  
2015-08-13    \[TASK\] Correct small typo (Cedric Ziel)  
2015-07-20    log stack trace by default on Katharsis Invoker exception (Woonsan Ko)  
2015-07-20    Exceptional QUERY_STRING handling (Woonsan Ko)  
2015-07-18    Adding IoC integration instruction (Woonsan Ko)  
2015-07-18    adding coveralls (Woonsan Ko)  

**v0.1.0**  
2015-07-18    tagging 0.1.0 (Woonsan Ko)  
2015-07-18    bump up version (Woonsan Ko)  
2015-07-17    setting jdk1.8 in travis-ci (Woonsan Ko)  
2015-07-17    adding travis-ci config (Woonsan Ko)  
2015-07-01    handling unsupported media type; adding more unit tests (Woonsan Ko)  
2015-06-29    upgrade katharsis-core to 0.9.2 (Woonsan Ko)  
2015-06-26    unit test for JsonApiMediaType (Woonsan Ko)  
2015-06-16    expose jackson data binding module in the invoker builder (Woonsan Ko)  
2015-06-16    query string handling (Woonsan Ko)  
2015-06-15    configuring surefire-report-plugin and corbetura plugin (Woonsan Ko)  
2015-06-13    first write to a buffer first because objectMapper may fail while writing. (Woonsan Ko)  
2015-06-13    Registering json object mapper and fixing tests. (Woonsan Ko)  
2015-06-12    nexus-staging-maven-plugin added (Woonsan Ko)  
2015-06-12    adding ossrh-release profile (Woonsan Ko)  
2015-06-12    fixing javadoc; configuring javadoc/source attaching plugins (Woonsan Ko)  
2015-06-12    Updating pom based on central component requirements (Woonsan Ko)  
2015-06-12    use katharsis-parent (Woonsan Ko)  
2015-06-12    updating README to reflect class/operation changes. (Woonsan Ko)  
2015-06-12    KatharsisInvokerBuilder shouldn't imply SampleJsonServiceLocator can be used. So, SampleJsonServiceLocator is only used in either SampleKatharsisFilter and SampleKatharsisServlet. (Woonsan Ko)  
2015-06-12    Fixing thread-safety issue when creating KatharsisInvoker; adding FIXME to use an advanced JsonServiceLocator instead of testing-purpose 'SampleJsonServiceLocator' in the near future. (Woonsan Ko)  
2015-06-12    adding simple servlet/filter implementation and integration testing purpose jetty plugin configuration (Woonsan Ko)  
2015-06-11    changing groupId; fixing mediaType detection and filter handling (Woonsan Ko)  
2015-06-11    fixing links (Woonsan Ko)  
2015-06-11    more description (Woonsan Ko)  
2015-06-11    adding more in README (Woonsan Ko)  
2015-06-11    fixing link url (Woonsan Ko)  
2015-06-11    Initial generic katharsis adaptor module for various servlet and non-servlet frameworks. (Woonsan Ko)  
2015-06-10    Initial commit (Woonsan Ko)  
