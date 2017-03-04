name := "marathon-templating-simple"

organization := "com.tapad.sbt"

version := "0.1.0"

dockerRegistry := "localhost:5000"

marathonServiceUrl := "http://localhost:8080"

marathonServiceRequest := {
  val _ = marathonEvaluateTemplates.value
  IO.read((target in Templating).value / "marathon_request.json")
}

marathonTemplates ++= Seq(
  Template(
    file = (sourceDirectory in Templating).value / "marathon_request.scala.json",
    driver = new {
      val appId = marathonApplicationId.value
      val instances = 5
      val cmd = (mainClass in (Compile, run)).value
      val cpus = 4.0
      val mem = 256.0
      val requirePorts = false
    }
  ),
  Template(
    file = (sourceDirectory in Templating).value / "trivial_script.scala.sh",
    driver = new {
      val appName = name.value
      val appVersion = version.value
      val appDependencies = allDependencies.value.map(_.toString)
    }
  )
)

TwirlKeys.templateFormats += "sh" -> "play.twirl.api.TxtFormat"

enablePlugins(MarathonPlugin, TemplatingPlugin)
