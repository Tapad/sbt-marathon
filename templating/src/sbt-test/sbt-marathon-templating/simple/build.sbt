name := "marathon-templating-simple"

organization := "com.tapad.sbt"

version := "0.1.0"

dockerRegistry := "localhost:5000"

marathonServiceUrl := "http://localhost:8080/v2/apps"

marathonServiceRequest := {
  val _ = marathonEvaluateTemplates.value
  IO.read((target in Templating).value / "marathon_request.json")
}

marathonTemplates += Template(
  file = (sourceDirectory in Templating).value / "marathon_request.json.scala",
  driver = new {
    val appId = marathonApplicationId.value
    val instances = 5
    val cmd = (mainClass in (Compile, run)).value
    val cpus = 4.0
    val mem = 256.0
    val requirePorts = false
  }
)

enablePlugins(MarathonPlugin, TemplatingPlugin)
