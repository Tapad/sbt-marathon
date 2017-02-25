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

## Usage

### Marathon `Request.Builder`
The sbt-marathon plugin provides a fluent interface to construct Marathon requests for a given application.

When communicating with Marathon's REST API, a JSON payload is required to specify the identity, properties, and constraints of your application.

The `sbtmarathon.adt.Request.Builder` provides a fluent interface for creating these JSON payloads.

You can leverage the `Request.Builder` as follows in your build definition:

```
import sbtmarathon.adt._

marathonServiceRequest := Request.newBuilder()
  .withId(marathonApplicationId.value)
  .withContainer(
    DockerContainer(
      image = s"${dockerRegistry.value}/${organization.value}/${name.value}:${version.value}",
      network = "BRIDGE"
    )
    .addPortMapping(containerPort = 8080, hostPort = 0, servicePort = Some(9000), protocol = "tcp")
    .addVolume(containerPath = "/etc/a", hostPath = "/var/data/a", mode = "RO")
  )
  .withCpus(4)
  .withMem(256)
  .addEnv("LD_LIBRARY_PATH", "/usr/local/lib/myLib")
  .addLabel("environment", "staging")
  .build()
```

For more information on how to use the `Request.Builder`, please refer to [`sbtmarathon.adt`](marathon/src/main/scala/sbtmarathon/adt/package.scala) and the [`AdtSpec.scala`](marathon/src/test/scala/sbtmarathon/adt/AdtSpec.scala) unit test.

### Integration with sbt-docker
To use sbt-marathon in conjunction with sbt-docker, add the following to your `./project/plugins.sbt` and `build.sbt` files, respectively:

```
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")

addSbtPlugin("com.tapad.sbt" % "sbt-marathon" % "0.1.0-SNAPSHOT")
```

```
// specify the url of your Marathon service endpoint
marathonServiceUrl := "http://localhost:8080/v2/apps"

// specify the docker registry to which your images will be pushed
dockerRegistry := "registry.hub.docker.com" // or a host/port pair pointing to your.private.registry:5000

// specify the image name for your application
imageNames in docker += ImageName(
  registry = Some(dockerRegistry.value),
  namespace = Some(organization.value),
  repository = name.value.toLowerCase,
  tag = Some(version.value)
)

// optionally define a mainClass (if necessary for your application's entrypoint)
mainClass in docker := (mainClass in Compile).value

// build the Dockerfile for your application, using sbt-docker's fluent interface
dockerfile in docker := {
  (mainClass in docker).value match {
    case None => sys.error("A main class is not defined. Please declare a value for the `mainClass` setting.")
    case Some(mainClass) =>
      ImmutableDockerfile.empty
        .from("java")
        .add((fullClasspath in Compile).value.files, "/app/")
        .entryPoint("java", "-cp", "/app:/app/*", mainClass, "$@")
  }
}

// build the request that will be used to start and modify your application
marathonServiceRequest := sbtmarathon.adt.Request.newBuilder()
  .withId(marathonApplicationId.value)
  .withContainer(
    DockerContainer(
      image = s"${dockerRegistry.value}/${organization.value}/${name.value}:${version.value}",
      network = "BRIDGE"
    )
  )
  .build()
```

Lastly, be sure to enable both sbt-docker and sbt-marathon in your `build.sbt` file:

```
enablePlugins(DockerPlugin, MarathonPlugin)
```

Once configured properly, the typical workflow to deploy your application on Marathon is:

1.  Build and push your image to the docker registry, if not already done
2.  Generate your Marathon API request
3.  Execute the request against the running Marathon server

An example workflow, run from an interactive sbt session, is presented below:
```
$ sbt
> dockerBuildAndPush      // build and push image to docker registry
> marathonServiceStart    // start application on Mesos
> marathonServiceScale 5  // scale application to 5 instances
...
> marathonServiceDestroy  // at some point in the future, destroy your application, shutting down all running instances
```

For more information, refer to the documentation provided by [sbt-docker](https://github.com/marcuslonnberg/sbt-docker) and the scripted integration test found at [marathon/src/sbt-test/sbt-marathon/docker](marathon/src/sbt-test/sbt-marathon/docker).

### Integration with sbt-native-packager
To use sbt-marathon in conjunction with sbt-native-packager, add the following to your `./project/plugins.sbt` and `build.sbt` files, respectively:

```
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")

addSbtPlugin("com.tapad.sbt" % "sbt-marathon" % "0.1.0-SNAPSHOT")
```

```
// specify the url of your Marathon service endpoint
marathonServiceUrl := "http://localhost:8080/v2/apps"

// specify the docker registry to which your images will be pushed
dockerRegistry := "localhost:5000"
dockerRepository in Docker := Some(dockerRegistry.value)

// build the request that will be used to start and modify your application
marathonServiceRequest := sbtmarathon.adt.Request.newBuilder()
  .withId(marathonApplicationId.value)
  .withContainer(
    DockerContainer(
      image = s"${dockerRegistry.value}/${(packageName in Docker).value}:${(version in Docker).value}",
      network = "BRIDGE"
    )
  )
  .build()
```

Lastly, be sure to enable both sbt-docker and sbt-marathon in your `build.sbt` file:

```
enablePlugins(JavaAppPackaging, DockerPlugin, MarathonPlugin)
```

Once configured properly, the typical workflow to deploy your application on Marathon is:

1.  Build and push your image to the docker registry, if not already done
2.  Generate your Marathon API request
3.  Execute the request against the running Marathon server

An example workflow, run from an interactive sbt session, is presented below:
```
$ sbt
> docker:publish          // build and push image to docker registry
> marathonServiceStart    // start application on Mesos
> marathonServiceScale 5  // scale application to 5 instances
...
> marathonServiceDestroy  // at some point in the future, destroy your application, shutting down all running instances
```

For more information, refer to the documentation provided by [sbt-native-packager](https://github.com/sbt/sbt-native-packager) and the scripted integration test found at [marathon/src/sbt-test/sbt-marathon/native-packager](marathon/src/sbt-test/sbt-marathon/native-packager).

### Templating
The [twirl templating engine](https://github.com/playframework/twirl) can be leveraged to help author Marathon requests by using the sbt-marathon-templating plugin.

Add the following line to `./project/plugins.sbt`.

```
addSbtPlugin("com.tapad.sbt" % "sbt-marathon-templating" % "0.1.0-SNAPSHOT")
```

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
