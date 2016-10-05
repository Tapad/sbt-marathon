package sbtmarathon

import sbt._

object MarathonPlugin extends AutoPlugin {

  object autoImport {
    val MarathonKeys            = sbtmarathon.MarathonKeys
    val MarathonSettings        = sbtmarathon.MarathonSettings
    val dockerRegistry          = MarathonKeys.dockerRegistry
    val marathonApplicationId   = MarathonKeys.marathonApplicationId
    val marathonServiceUrl      = MarathonKeys.marathonServiceUrl
    val marathonServiceRequest  = MarathonKeys.marathonServiceRequest
    val marathonService         = MarathonKeys.marathonService
    val marathonServiceStart    = MarathonKeys.marathonServiceStart
    val marathonServiceDestroy  = MarathonKeys.marathonServiceDestroy
    val marathonServiceUpdate   = MarathonKeys.marathonServiceUpdate
    val marathonServiceRestart  = MarathonKeys.marathonServiceRestart
    val marathonServiceScale    = MarathonKeys.marathonServiceScale
  }

  override def projectSettings = MarathonSettings.projectSettings
}
