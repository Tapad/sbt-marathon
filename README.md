# sbt-marathon
An [sbt](http://scala-sbt.org) plugin for launching application containers on the Mesosphere [Marathon](https://mesosphere.github.io/marathon) platform.

## Table of contents

  * [Requirements](#requirements)
  * [Installation](#installation)
  * [Usage](#usage)
    * [Marathon `Request.Builder`](#marathon-requestbuilder)
    * [Integration with sbt-native-packager](#integration-with-sbt-native-packager)
    * [Integration with sbt-docker](#integration-with-sbt-docker)
    * [Templating](#templating)
  * [Contributing](#contributing)
    * [Project structure](#project-structure)
    * [Running tests](#running-tests)
    * [Releasing artifacts](#releasing-artifacts)

## Requirements
- sbt (0.13.5+ or 1.0.0+)
- An installation of Marathon (1.0.0+) to target

## Installation
Add the following line to `project/plugins.sbt`. See the [Using plugins](http://www.scala-sbt.org/release/docs/Using-Plugins.html) section of the sbt documentation for more information.

```
addSbtPlugin("com.tapad.sbt" % "sbt-marathon" % "0.2.1")
```

## Usage

### Marathon `Request.Builder`
The sbt-marathon plugin provides a fluent interface to construct Marathon requests for a given application.

When communicating with Marathon's REST API, a JSON payload is required to specify the identity, properties, and constraints of your application.

Leverage `sbtmarathon.adt.Request.Builder` from within your build definition to create these JSON payloads:

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

### Integration with sbt-native-packager
To use sbt-marathon in conjunction with sbt-native-packager, add the following to your `project/plugins.sbt` and `build.sbt` files, respectively:

```
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")

addSbtPlugin("com.tapad.sbt" % "sbt-marathon" % "0.2.1")
```

```
// specify the url of your Marathon service
marathonServiceUrl := "http://localhost:8080"

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

### Integration with sbt-docker
To use sbt-marathon in conjunction with sbt-docker, add the following to your `project/plugins.sbt` and `build.sbt` files, respectively:

```
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")

addSbtPlugin("com.tapad.sbt" % "sbt-marathon" % "0.2.1")
```

```
// specify the url of your Marathon service
marathonServiceUrl := "http://localhost:8080"

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

### Templating
The [twirl templating engine](https://github.com/playframework/twirl) can be leveraged to help author Marathon requests by using the sbt-marathon-templating plugin.

NOTE: If using sbt 1.0.x, sbt-twirl requires that you use sbt version 1.0.1 or greater due to the `Append` instance backwards compatibility issue addressed in the [release notes](http://www.scala-sbt.org/1.x/docs/sbt-1.0-Release-Notes.html#WatchSource).

Add the following lines to `project/plugins.sbt`.

```
addSbtPlugin("com.tapad.sbt" % "sbt-marathon" % "0.2.1")

addSbtPlugin("com.tapad.sbt" % "sbt-marathon-templating" % "0.2.1")
```

Create a twirl template in the location specifed by `templating:sourceDirectory`. By default, this location will be the `templates` subdirectory inside of your project's resources directory (e.g. `src/main/resources/templates`).

For the following example, a template with the file name `marathon_request.scala.json` has been added to `src/main/resources/templates/marathon_request.scala.json`. Its contents are:

```
@(appId: String, instances: Int, cmd: Option[String], cpus: Double, mem: Double, requirePorts: Boolean)

{
  "id": "@appId",
  "instances": @instances,
  "cmd": "@{cmd.getOrElse("sleep 1")}",
  "cpus": @cpus,
  "mem": @mem,
  "portDefinitions": [
    { "port": 9000,
      "protocol": "tcp",
      "name": "admin"
    }
  ],
  "requirePorts": @requirePorts
}
```

In your `build.sbt` file, specify the location of this template and the parameters to pass to it:

```
marathonTemplates += Template(
  file = (sourceDirectory in Templating).value / "marathon_request.scala.json",
  driver = new {
    val appId = marathonApplicationId.value
    val instances = 5
    val cmd = (mainClass in (Compile, run)).value
    val cpus = 4.0
    val mem = 256.0
    val requirePorts = false
  }
)
```

Ensure that the `marathonServiceRequest` task depends on and utilizes the output of the evaluated template:

```
marathonServiceRequest := {
  val _ = marathonEvaluateTemplates.value
  IO.read((target in Templating).value / "marathon_request.json")
}
```

Lastly, be sure to enable both sbt-marathon and sbt-marathon-templating in your `build.sbt` file:

```
enablePlugins(MarathonPlugin, TemplatingPlugin)
```

When the `marathonServiceRequest` and/or `marathonEvaluateTemplates` tasks are executed, the result from evaluating this template will be placed in `templating:target`, which by default, will be the `generated` subdirectory of your project's resources directory (e.g. `src/main/resources/generated`).

The values for the settings provided by sbt-marathon-templating, given a default build definition, can be found in the table below:

| Setting key (scope:name)   | Default value                |
| -------------------------- | ---------------------------- |
| templating:sourceDirectory | src/main/resources/templates |
| templating:target          | src/main/resources/generated |

These can be customized to suit your project's structure.

sbt-marathon-templating need not only be used for templating Marathon API requests. It is possible to template any type of resource and evaluate it for inclusion in a Docker image, for instance. This is a handy way of injecting information about your project and your project's build definition into your containers and their constituent components.

For example, given a (templated) shell script that will live alongside our application, which needs to access to project metadata:

```
@(appName: String, appVersion: String, appDependencies: Seq[String])

#!/bin/bash

usage() {
  echo "usage: $(basename "$0") [-h|-v|--dependencies]"
}

print_version() {
  echo "@{appName} @{appVersion}"
}

print_dependencies() {
  echo "@{appName} dependencies:"
  @for(dependency <- appDependencies) {
    echo "@{dependency}"
  }
}

for arg in "$@@"
do
  argi=$((argi + 1))
  next=${args[argi]}

  case $arg in
    -h)
      usage
      ;;
    -v)
      print_version
      ;;
    --dependencies)
      print_dependencies
      ;;
    *)
      echo 'Unknown option'
      usage
      exit 1
  esac
done
```

We can adjust our build accordingly so that this template will be evaluated and the generated resource will be available for inclusion in a Docker image:

```
// ensure that twirl will evaluate `scala.sh` templates
TwirlKeys.templateFormats += "sh" -> "play.twirl.api.TxtFormat"

// add the trivial_script.scala.sh template to our list of templates that will be evaluated
marathonTemplates += Template(
  file = (sourceDirectory in Templating).value / "trivial_script.scala.sh",
  driver = new {
    val appName = name.value
    val appVersion = version.value
    val appDependencies = allDependencies.value.map(_.toString)
  }
)
```

The generated resource will appear in `templating:target` after evaluating templates via the `marathonEvaluateTemplates` task.

Leverage the generated resource, by referencing its non-template file name in the `templating:target` directory.

To add the resource to your Docker image (using sbt-docker), for instance:

```
dockerfile in docker := {
  (mainClass in docker).value match {
    case None => sys.error("A main class is not defined. Please declare a value for the `mainClass` setting.")
    case Some(mainClass) =>
      ImmutableDockerfile.empty
        .from("java")
        .add((target in Templating).value / "trivial_script.sh", "/bin/")
        .add((fullClasspath in Compile).value.files, "/app/")
        .entryPoint("java", "-cp", "/app:/app/*", mainClass, "$@")
  }
}
```

For more information, refer to the scripted integration test found at [templating/src/sbt-test/sbt-marathon-templating/simple](templating/src/sbt-test/sbt-marathon-templating/simple).

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

Additionally, certain tests are tagged with ScalaTest tags and will be excluded when running the sbt `test` task.

To execute these tagged tests, you can manually lift their exclusions in an interactive sbt session:

```
$ sbt
> project marathon
> set testOptions in Test := Seq.empty
```

You can also reinstate their exclusion in the same session, if desired:
```
> set testOptions in Test += Tests.Argument("-l", "sbtmarathon.FunctionalTest")
```

Manually lifting this exclusion is not a permanent change. Any reload of the build configuration will reinstate the project's default exclusions.

### Releasing artifacts
`sbt-marathon` uses [https://github.com/sbt/sbt-release](sbt-release). Simply invoke `release` from the root project to release all artifacts.
