name := "marathon-templating-simple"

organization := "com.tapad.sbt"

version := "0.1.0"

dockerRegistry := "localhost"

marathonServiceUrl := "http://localhost:8000"

marathonServiceRequest := IO.read((target in Templating).value / "marathon_request.json")

marathonServiceRequest := {
  marathonServiceRequest.dependsOn(marathonEvaluateTemplates).value
}

marathonTemplates += Template(
  file = (sourceDirectory in Templating).value / "marathon_request.json.scala",
  driver = new {
    val ctx = name.value
  }
)

enablePlugins(MarathonPlugin, TemplatingPlugin)
