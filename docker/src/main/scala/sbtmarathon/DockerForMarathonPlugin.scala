package sbtmarathon

import sbt._
import Keys._

object DockerForMarathonPlugin extends AutoPlugin {

  object autoImport {
    val DockerForMarathonKeys = sbtmarathon.DockerForMarathonKeys
  }

  override def requires = MarathonPlugin

  override def projectSettings = Seq.empty
}
