# katharsis-servlet

[![Build Status](https://api.travis-ci.org/woonsan/katharsis-servlet.svg?branch=develop)](https://api.travis-ci.org/woonsan/katharsis-servlet.svg?branch=develop)
[![Coverage Status](https://coveralls.io/repos/woonsan/katharsis-servlet/badge.svg?branch=master&service=github)](https://coveralls.io/github/woonsan/katharsis-servlet?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.woonsan/katharsis-servlet.svg)]()


Generic Servlet Adapter of Katharsis JSON:API middleware library.

# Introduction

This module aims to provide a generic invoker module for
Katharsis JSON:API middleware library (https://github.com/katharsis-project/katharsis-core).
This module can be used in simple servlet or filter,
servlet-based application framework such as Spring Framework,
or even non-ServletAPI-based frameworks such as Portal/Portlet, Wicket, etc.

# How to use this in my Servlet Filter

This module provides an abstract class, [AbstractKatharsisFilter.java](src/main/java/com/github/woonsan/katharsis/servlet/AbstractKatharsisFilter.java). Basically you need to override the following method at least as well:

```java
    abstract protected KatharsisInvokerBuilder createKatharsisInvokerBuilder();
```

Also see [SampleKatharsisFilter.java](src/main/java/com/github/woonsan/katharsis/servlet/SampleKatharsisFilter.java) as a servlet filter implementation example, and [web.xml](src/test/webapp/WEB-INF/web.xml) as a configuration example.

# How to use this in my Servlet

This module provides an abstract class, [AbstractKatharsisServlet.java](src/main/java/com/github/woonsan/katharsis/servlet/AbstractKatharsisServlet.java). Basically you need to override the following method at least:

```java
    abstract protected KatharsisInvokerBuilder createKatharsisInvokerBuilder();
```

Also see [SampleKatharsisServlet.java](src/main/java/com/github/woonsan/katharsis/servlet/SampleKatharsisServlet.java) as a servlet implementation example, and [web.xml](src/test/webapp/WEB-INF/web.xml) as a configuration example.

# How to integrate with my IoC (Dependency Injection) Container

You can override #createKatharsisInvokerBuilder() method in
either [SampleKatharsisFilter.java](src/main/java/com/github/woonsan/katharsis/servlet/SampleKatharsisFilter.java)
or [SampleKatharsisServlet.java](src/main/java/com/github/woonsan/katharsis/servlet/SampleKatharsisServlet.java)
to use your own JsonServiceLocator component.

For example, you can create a new JsonServiceLocator to get a bean (singleton or prototype)
from Spring Web Application Context like the following example:


```java
            /**
             * NOTE: A class extending this must provide a platform specific {@link JsonServiceLocator}
             *       instead of the (testing-purpose) {@link SampleJsonServiceLocator} below
             *       in order to provide advanced dependency injections for the repositories.
             */
            @Override
            protected KatharsisInvokerBuilder createKatharsisInvokerBuilder() {
                return new KatharsisInvokerBuilder()
                        .resourceSearchPackage(getResourceSearchPackage())
                        .resourceDefaultDomain(getResourceDefaultDomain())
                        .jsonServiceLocator(new JsonServiceLocator() {
                            @Override
                            public <T> T getInstance(Class<T> clazz) {
                                // assuming the repository beans can be retrieved from the WebApplicationContext and are identified by the FQCN in this exmaple.
                                return WebApplicationContextUtils.getWebApplicationContext(getServletContext()).getBean(clazz.getName());
                            }
                        });
            }
```

You will probably get the idea on how to integrate with other containers as well from the example.

# How to Add this module in my Project

Add the following dependency:

```xml
            <dependency>
                <groupId>com.github.woonsan</groupId>
                <artifactId>katharsis-servlet</artifactId>
                <version>${katharsis-servlet.version}</version>
            </dependency>
```

# Releases and Version Compatibility

| katharsis-servlet.version | katharsis-core.version |
| :-----------------------: | :--------------------: |
|          0.1.1            |         0.9.3          |
|          0.1.0            |         0.9.2          |

See [changes.xml](changes.xml) for details.

# Demo in a Web Application

Please run the following command in this project root folder:

```bash
    $ mvn -Prun clean verify
```

Visit [http://localhost:8080/katharsis/](http://localhost:8080/katharsis/) and test out each JSON API link.
