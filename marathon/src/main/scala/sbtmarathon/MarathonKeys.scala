package sbtmarathon

import sbt._

object MarathonKeys {
  val dockerRegistry = settingKey[String]("Docker registry")
  val marathonApplicationId = settingKey[String]("The Marathon application identifier")
  val marathonServiceUrl = settingKey[String]("The Marathon service URL")
  val marathonServiceRequest = taskKey[String]("The Marathon service request entity")
  val marathonService = taskKey[MarathonService]("The Marathon API service instance")
  val marathonServiceStart = taskKey[Unit]("Start instance(s) of an application")
  val marathonServiceDestroy = taskKey[Unit]("Destroy instance(s) of an application")
  val marathonServiceUpdate = taskKey[Unit]("Update the configuration of an application")
  val marathonServiceRestart = taskKey[Unit]("Restart application instance(s)")
  val marathonServiceScale = inputKey[Unit]("Scale an application up/down to n instances")
}
