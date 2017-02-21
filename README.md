# sbt-marathon
An [sbt](http://scala-sbt.org) plugin for launching application containers on the Mesosphere [Marathon](https://mesosphere.github.io/marathon) platform.

## Requirements
- sbt (0.13.5+)
- An installation of Marathon (1.0.0+) to target

## Installation
Add the following line to `./project/plugins.sbt`. See the section [Using plugins](http://www.scala-sbt.org/release/docs/Using-Plugins.html) for more information.

```
addSbtPlugin("com.tapad.sbt" % "sbt-marathon" % "0.1.0-SNAPSHOT")
```

## Integration with sbt-docker

## Integration with sbt-native-packager

## Marathon `Request.Builder`
The sbt-marathon plugin provides a fluent interface to construct Marathon requests for a given application.

## Templating
The [twirl templating engine](https://github.com/playframework/twirl) can be leveraged to help author Marathon requests by using the sbt-marathon-templating plugin.

Add the following line to `./project/plugins.sbt`.

```
addSbtPlugin("com.tapad.sbt" % "sbt-marathon-templating" % "0.1.0-SNAPSHOT")
```

## Tutorial

## Contributing

### Project structure
- marathon
- templating
- templating-lib
- util

#### marathon
An sbt plugin and underlying service interface used to deploy (i.e. start, destroy, update, restart, and scale) applications on Marathon.

#### templating
An sbt plugin that provides the capability to template a Marathon request using [twirl](https://github.com/playframework/twirl).

#### templating-lib
Supporting library code leveraged by the templating plugin.

#### util
Supporting library code and common abstractions.

### Releasing artifacts
`sbt-marathon` uses [https://github.com/sbt/sbt-release](sbt-release). Simply invoke `release` from the root project to release all artifacts.
