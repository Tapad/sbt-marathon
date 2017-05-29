name := "marathon-basic-auth"

organization := "com.tapad.sbt"

version := "0.1.0"

dockerRegistry := "localhost"

marathonServiceUrl := "https://user:password@example.com:8080"

marathonServiceRequest := {
 s"""
  {"id": "${marathonApplicationId.value}"}
  """
}

enablePlugins(MarathonPlugin)
