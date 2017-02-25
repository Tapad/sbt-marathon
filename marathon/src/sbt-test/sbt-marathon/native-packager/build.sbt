import sbtmarathon.adt._

name := "marathon-native-packager"

organization := "com.tapad.sbt-test"

version := "0.1.0"

mainClass in Compile := Some("HelloWorld")

dockerRegistry := "localhost:5000"

dockerRepository in Docker := Some(dockerRegistry.value)

marathonServiceUrl := "http://localhost:8080/v2/apps"

marathonServiceRequest := Request.newBuilder()
  .withId(marathonApplicationId.value)
  .withContainer(
    DockerContainer(
      image = s"${dockerRegistry.value}/${(packageName in Docker).value}:${(version in Docker).value}",
      network = "BRIDGE"
    )
  )
  .build()

enablePlugins(JavaAppPackaging, DockerPlugin, MarathonPlugin)
