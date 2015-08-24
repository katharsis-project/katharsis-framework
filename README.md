# katharsis-servlet

[![Join the chat at https://gitter.im/katharsis-project/katharsis-servlet](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/katharsis-project/katharsis-servlet?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/katharsis-project/katharsis-servlet.svg?branch=develop)](https://travis-ci.org/katharsis-project/katharsis-servlet)
[![Coverage Status](https://coveralls.io/repos/katharsis-project/katharsis-servlet/badge.svg?branch=develop)](https://coveralls.io/r/katharsis-project/katharsis-servlet?branch=develop)
[![Stories in Ready](https://badge.waffle.io/katharsis-project/katharsis-servlet.png?label=ready&title=Ready)](https://waffle.io/katharsis-project/katharsis-servlet)
[![Maven Central](https://img.shields.io/maven-central/v/io.katharsis/katharsis-servlet.svg)](http://mvnrepository.com/artifact/io.katharsis/katharsis-servlet)

Generic Servlet Adapter of Katharsis JSON:API middleware library.

# Introduction

This module aims to provide a generic invoker module for
Katharsis JSON:API middleware library (https://github.com/katharsis-project/katharsis-core).
This module can be used in simple servlet or filter,
servlet-based application framework such as Spring Framework,
or even non-ServletAPI-based frameworks such as Portal/Portlet, Wicket, etc.

# How to use this in my Servlet Filter

This module provides an abstract class, [AbstractKatharsisFilter.java](src/main/java/io/katharsis/servlet/AbstractKatharsisFilter.java). Basically you need to override the following method at least as well:

```java
    abstract protected KatharsisInvokerBuilder createKatharsisInvokerBuilder();
```

Also see [SampleKatharsisFilter.java](src/main/java/io/katharsis/servlet/SampleKatharsisFilter.java) as a servlet filter implementation example, and [web.xml](src/test/webapp/WEB-INF/web.xml) as a configuration example.

# How to use this in my Servlet

This module provides an abstract class, [AbstractKatharsisServlet.java](src/main/java/io/katharsis/servlet/AbstractKatharsisServlet.java). Basically you need to override the following method at least:

```java
    abstract protected KatharsisInvokerBuilder createKatharsisInvokerBuilder();
```

Also see [SampleKatharsisServlet.java](src/main/java/io/katharsis/servlet/SampleKatharsisServlet.java) as a servlet implementation example, and [web.xml](src/test/webapp/WEB-INF/web.xml) as a configuration example.

# How to integrate with my IoC (Dependency Injection) Container

You can override #createKatharsisInvokerBuilder() method in
either [SampleKatharsisFilter.java](src/main/java/io/katharsis/servlet/SampleKatharsisFilter.java)
or [SampleKatharsisServlet.java](src/main/java/io/katharsis/servlet/SampleKatharsisServlet.java)
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
      <groupId>io.katharsis</groupId>
      <artifactId>katharsis-servlet</artifactId>
      <version>${katharsis-servlet.version}</version>
    </dependency>
```

See [changes.xml](changes.xml) for details.

# Demo in a Web Application

Please run the following command in this project root folder:

```bash
    $ mvn -Prun clean verify
```

Visit [http://localhost:8080/katharsis/](http://localhost:8080/katharsis/) and test out each JSON API link.
