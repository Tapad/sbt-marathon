# sbt-marathon
An [sbt](http://scala-sbt.org) plugin for launching application containers on the Mesosphere [Marathon](https://mesosphere.github.io/marathon) platform.

## Requirements
- sbt (0.13.5+)
- An installation of Marathon (1.0.0+) to target

## Installation
Add the following line to `./project/plugins.sbt`. See the [Using plugins](http://www.scala-sbt.org/release/docs/Using-Plugins.html) section of the sbt documentation for more information.

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

### Running tests
Although unit tests exist, the main features and functionality of `sbt-marathon` and `sbt-marathon-templating` are tested using sbt's [`scripted-plugin`](https://github.com/sbt/sbt/tree/0.13/scripted). `scripted` tests exist in the `src/sbt-test` directories of the `sbt-marathon` and `sbt-marathon-templating` subprojects.

To run these tests, issue `scripted` from an sbt session after targeting either of these subprojects:

```
$ sbt
> project marathon
> scripted
> project templating
> scripted
```

To selectively run a single `scripted` test suite, issue `scripted <name of plugin>/<name of test project>`. e.g. `scripted sbt-marathon/simple`.

Please note that `publishLocal` will be invoked when running `scripted`. `scripted` tests take longer to run than unit tests and will log myriad output to stdout. Also note that any output written to stderr during the execution of a `scripted` test will result in `ERROR` level log entries. These log entries will not effect the resulting status of the actual test.

### Releasing artifacts
`sbt-marathon` uses [https://github.com/sbt/sbt-release](sbt-release). Simply invoke `release` from the root project to release all artifacts.
