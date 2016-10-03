package sbtmarathon

import sbt._
import Keys._

object TemplatingPlugin extends AutoPlugin {

  object autoImport {
    val TemplatingKeys = sbtmarathon.TemplatingKeys
  }

  override def requires = MarathonPlugin

  override def projectSettings = Seq.empty
}
