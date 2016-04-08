![Katharsis logo](http://katharsis.io/assets/img/engine_katharsis_github_4.png)

The Katharsis library adds an additional layer on top of RESTful endpoint to provide easy HATEOAS support for Java by implementing JSON API standard.

# katharsis-core

[![Build Status](https://travis-ci.org/katharsis-project/katharsis-core.svg?branch=development)](https://travis-ci.org/katharsis-project/katharsis-core)
[![Coverage Status](https://coveralls.io/repos/katharsis-project/katharsis-core/badge.svg?branch=development)](https://coveralls.io/r/katharsis-project/katharsis-core?branch=development)
[![Dependency Status](https://www.versioneye.com/user/projects/56633a74f376cc003c000a94/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56633a74f376cc003c000a94)
[![Maven Central](https://img.shields.io/maven-central/v/io.katharsis/katharsis-core.svg)](http://mvnrepository.com/artifact/io.katharsis/katharsis-core)

Systems nowadays utilize data from various systems to leverage the business needs. To achieve that, many of them provide usually inconsistent REST interface.

__Providing homogeneous REST interface__

Katharsis implements JSON API standard which introduces consistent REST interface definition. Now it can be easy to integrate with other systems through uniform mechanisms.

__Use the purest form of REST__

JSON API is based on HATEOAS which means Hypermedia as the Engine of Application State. It is the highest form of REST which allows producing and storing as little documentation as possible.

---

By using Katharsis it is easier to develop both Customer Facing Applications and server side services. The developers can have one unified base for their work.

__Consistent resources and repositories__

Katharsis introduces a way of defining both resources which can be shared over the REST interface and a repository for their handling.

__Integration with other libraries__

Because of the usage of JSON API, Katharsis can be used with many other libraries which support the standard.

## Quick start
Add dependency to your `pom.xml`:

```xml
<dependency>
	<groupId>io.katharsis</groupId>
	<artifactId>katharsis-core</artifactId>
	<version>2.3.1</version>
</dependency>

```

## Documentation and examples
Documentation, along with example projects and project details are available on project website  [katharsis.io](http://katharsis.io) 

## Contributing

Please submit pull-requests for new features and/or bugs.

The project uses `git flow` development where `master` branch builds the latest stable release and `develop` contains the latest features getting worked on and soon to be released. Please make pull requests on `develop` branch. 

Git flow tools and detailed description are available at [git flow](https://github.com/nvie/gitflow) .

## Chat
Need to directly talk to us? Write on gitter: 

[![Join the chat at https://gitter.im/katharsis-project/katharsis-core](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/katharsis-project/katharsis-core?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
