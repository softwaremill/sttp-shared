![sttp-shared](https://github.com/softwaremill/sttp-shared/raw/master/banner.png)

[![Join the chat at https://gitter.im/softwaremill/sttp](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/softwaremill/sttp)
[![CI](https://github.com/softwaremill/sttp-shared/workflows/CI/badge.svg)](https://github.com/softwaremill/sttp-shared/actions?query=workflow%3A%22CI%22)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp.shared/core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.sttp.shared/core_2.12)

sttp is a family of Scala HTTP-related projects, and currently includes:

* [sttp client](https://github.com/softwaremill/sttp): The Scala HTTP client you always wanted!
* [sttp tapir](https://github.com/softwaremill/tapir): Typed API descRiptions
* [sttp model](https://github.com/softwaremill/sttp-model): Simple Scala HTTP model.
* sttp shared: this project. Shared web socket, FP abstractions, capabilities and streaming code.

## Quickstart with sbt

Add the following dependency:

```scala
"com.softwaremill.sttp.shared" %% "core" % "1.1.0"
```

sttp model is available for Scala 2.11, 2.12, 2.13, 3.0.0-Mx, Scala.JS and Scala Native.

### Building & testing the scala-native version

By default, sttp-native will **not** be included in the aggregate build of the root project. To include it, define the `STTP_NATIVE` environmental variable before running sbt, e.g.:

```
STTP_NATIVE=1 sbt
```

You might need to install some additional libraries, see the [scala native](http://www.scala-native.org/en/latest/user/setup.html) documentation site.

## Commercial Support

We offer commercial support for sttp and related technologies, as well as development services. [Contact us](https://softwaremill.com) to learn more about our offer!

## Copyright

Copyright (C) 2017-2020 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
