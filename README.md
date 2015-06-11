# katharsis-servlet

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

    abstract protected KatharsisInvoker createKatharsisInvoker();

See [KatharsisServletTest.java](src/test/java/com/github/woonsan/katharsis/servlet/KatharsisServletTest.java)
for detail on how to create the KatharsisInvoker using the builder object
and how it works in servlet environment.

How to use this in my Servlet Filter?
=====================================

This module provides an abstract class, [AbstractKatharsisFilter.java](src/main/java/com/github/woonsan/katharsis/servlet/AbstractKatharsisFilter.java). Basically you need to override the following method at least as well:

    abstract protected KatharsisInvoker createKatharsisInvoker();

See [KatharsisFilterTest.java](src/test/java/com/github/woonsan/katharsis/servlet/KatharsisFilterTest.java)
for detail on how to create the KatharsisInvoker using the builder object
and how it works in servlet environment.
