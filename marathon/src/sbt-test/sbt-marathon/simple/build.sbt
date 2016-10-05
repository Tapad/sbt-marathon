name := "marathon-simple"

organization := "com.tapad.sbt"

version := "0.1.0"

dockerRegistry := "localhost"

marathonServiceUrl := "http://localhost:8000"

marathonServiceRequest := {
 s"""
  {"id": "${marathonApplicationId.value}"}
  """
}

enablePlugins(MarathonPlugin)
