# katharsis-servlet

[![Build Status](https://api.travis-ci.org/woonsan/katharsis-servlet.svg?branch=develop)](https://api.travis-ci.org/woonsan/katharsis-servlet.svg?branch=develop)
[![Coverage Status](https://coveralls.io/repos/woonsan/katharsis-servlet/badge.svg?branch=master&service=github)](https://coveralls.io/github/woonsan/katharsis-servlet?branch=master)


Generic Servlet Adapter of Katharsis JSON:API middleware library.

Introduction
============
This module aims to provide a generic invoker module for
Katharsis JSON:API middleware library (https://github.com/katharsis-project/katharsis-core).
This module can be used in simple servlet or filter,
servlet-based application framework such as Spring Framework,
or even non-ServletAPI-based frameworks such as Portal/Portlet, Wicket, etc.

How to use this in my Servlet?
==============================

This module provides an abstract class, [AbstractKatharsisServlet.java](src/main/java/com/github/woonsan/katharsis/servlet/AbstractKatharsisServlet.java). Basically you need to override the following method at least:

    abstract protected KatharsisInvokerBuilder createKatharsisInvokerBuilder();

Also see [SampleKatharsisServlet.java](src/main/java/com/github/woonsan/katharsis/servlet/SampleKatharsisServlet.java) as an example.

How to use this in my Servlet Filter?
=====================================

This module provides an abstract class, [AbstractKatharsisFilter.java](src/main/java/com/github/woonsan/katharsis/servlet/AbstractKatharsisFilter.java). Basically you need to override the following method at least as well:

    abstract protected KatharsisInvokerBuilder createKatharsisInvokerBuilder();

Also see [SampleKatharsisFilter.java](src/main/java/com/github/woonsan/katharsis/servlet/SampleKatharsisFilter.java) as an example.

Demo in a Web Application?
==========================

Please run the following command in this project root folder:

    $ mvn -Prun clean verify

Visit [http://localhost:8080/katharsis/](http://localhost:8080/katharsis/) and test out each JSON API link.
