import sbtdocker.ImmutableDockerfile
import sbtmarathon.adt._

name := "marathon-docker"

organization := "com.tapad.sbt-test"

version := "0.1.0"

mainClass in Compile := Some("HelloWorld")

dockerRegistry := "localhost:5000"

imageNames in docker := Seq(
  ImageName(
    registry = Some(dockerRegistry.value),
    namespace = Some(organization.value),
    repository = name.value.toLowerCase,
    tag = Some(version.value)
  )
)

mainClass in docker := (mainClass in Compile).value

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

marathonServiceUrl := "http://localhost:8080"

marathonServiceRequest := Request.newBuilder()
  .withId(marathonApplicationId.value)
  .withContainer(
    DockerContainer(
      image = s"${dockerRegistry.value}/${organization.value}/${name.value.toLowerCase}:${version.value}",
      network = "BRIDGE"
    )
  )
  .build()

enablePlugins(DockerPlugin, MarathonPlugin)
