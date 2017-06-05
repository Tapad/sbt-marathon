name := "marathon-basic-auth"

organization := "com.tapad.sbt"

version := "0.1.0"

dockerRegistry := "localhost"

marathonServiceUrl := "https://user:password@httpbin.org/basic-auth/user/password"

marathonServiceRequest := {
 s"""
  {"id": "${marathonApplicationId.value}"}
  """
}

enablePlugins(MarathonPlugin)
