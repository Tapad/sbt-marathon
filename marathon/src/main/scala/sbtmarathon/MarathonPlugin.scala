package sbtmarathon

import sbt._
import Keys._

object MarathonPlugin extends AutoPlugin {

  object autoImport {
    val MarathonKeys = sbtmarathon.MarathonKeys

    val marathonApplicationId   = MarathonKeys.marathonApplicationId
    val marathonServiceStart    = MarathonKeys.marathonServiceStart
    val marathonServiceDestroy  = MarathonKeys.marathonServiceDestroy
    val marathonServiceUpdate   = MarathonKeys.marathonServiceUpdate
    val marathonServiceRestart  = MarathonKeys.marathonServiceRestart
    val marathonServiceScale    = MarathonKeys.marathonServiceScale
  }

  override def projectSettings = Seq.empty
}
