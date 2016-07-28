![Katharsis logo](http://katharsis.io/assets/img/engine_katharsis_github_4.png)

The Katharsis library adds an additional layer on top of RESTful endpoint to provide easy HATEOAS support for Java by implementing JSON API standard.

# katharsis-servlet

[![Join the chat at https://gitter.im/katharsis-project/katharsis-servlet](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/katharsis-project/katharsis-servlet?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/katharsis-project/katharsis-servlet.svg?branch=develop)](https://travis-ci.org/katharsis-project/katharsis-servlet)
[![Coverage Status](https://coveralls.io/repos/katharsis-project/katharsis-servlet/badge.svg?branch=develop)](https://coveralls.io/r/katharsis-project/katharsis-servlet?branch=develop)
[![Dependency Status](https://www.versioneye.com/user/projects/56633a70f376cc003d0009a9/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56633a70f376cc003d0009a9)
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

For example, you can create a new *JsonServiceLocator* to get a bean (singleton or prototype)
from Spring Web Application Context like the following example:


```java

    /**
     * NOTE: A class extending this must provide a platform specific {@link JsonServiceLocator}
     *       instead of the (testing-purpose) {@link SampleJsonServiceLocator} below
     *       in order to provide advanced dependency injections for the ResourceRepository beans.
     */
    @Override
    protected KatharsisInvokerBuilder createKatharsisInvokerBuilder() {
        return new KatharsisInvokerBuilder()
                .resourceSearchPackage(getResourceSearchPackage())
                .resourceDefaultDomain(getResourceDefaultDomain())
                .jsonServiceLocator(new JsonServiceLocator() {
                    @Override
                    public <T> T getInstance(Class<T> clazz) {
                        // assuming the following in this example:
                        // - you can get the BeanFactory through WebApplicationContextUtils and servlet context.
                        // - your ResourceRepository beans are retrieved by the type through the BeanFactory.
                        BeanFactory beanFactory = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
                        return beanFactory.getBean(clazz);
                    }
                });
    }

```

In the example above, a custom *JsonServiceLocator* was provided to find *ResourceRepository* beans by the type
from the underlying *WebApplicationContext*.

For demonstration purpose, the example assumes that it can retrieve Repository beans
from the *WebApplicationContext* in a Web MVC application. So you will need to register the JSON API Repository beans
by either annotation-based or XML configuration based approach in the *WebApplicationContext*.

You can also choose a more generic approach by using a bean which implements *BeanFactoryAware*
instead of using *WebApplicationContextUtils*.

In the [spring-boot-simple-example](https://github.com/katharsis-project/katharsis-examples/tree/master/spring-boot-simple-example),
it demonstrates how to register the filter bean itself by using *@Configuration*-annotated class
(see [WebConfig.java](https://github.com/katharsis-project/katharsis-examples/blob/master/spring-boot-simple-example/src/main/java/io/katharsis/example/springboot/simple/WebConfig.java))
and how to let the filter bean ([SpringBootSampleKatharsisFilter.java](https://github.com/katharsis-project/katharsis-examples/blob/master/spring-boot-simple-example/src/main/java/io/katharsis/example/springboot/simple/filter/SpringBootSampleKatharsisFilter.java))
get access to the *BeanFactory* by implementing *BeanFactoryAware*, for instance.
So, the filter bean can retrieve *ResourceRepository* beans by invoking on *BeanFactory#getBean(Class)* directly.

Please read more about [The IoC container](http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/beans.html)
if you need more detail about Spring IoC container and beans configuration.

You have probably got the idea on how to integrate with other containers as well from the example.

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

