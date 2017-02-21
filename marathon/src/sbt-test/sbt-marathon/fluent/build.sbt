name := "marathon-fluent"

organization := "com.tapad.sbt"

version := "0.1.0"

dockerRegistry := "localhost"

marathonServiceUrl := "http://localhost:8000"

marathonServiceRequest := sbtmarathon.adt.Request.newBuilder()
  .withId(marathonApplicationId.value)
  .build()

enablePlugins(MarathonPlugin)
