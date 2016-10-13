package sbtmarathon

import sbt._

object TemplatingKeys {
  val marathonTemplates = taskKey[Seq[Template]]("Template instances")
  val marathonEvaluateTemplates = taskKey[Seq[File]]("Evaluate templates")
}
