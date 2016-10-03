package sbtmarathon

import sbt._

object TemplatingKeys {
  val marathonTemplatesDir = settingKey[File]("Directory of templates")
  val marathonGeneratedDir = settingKey[File]("Target directory for generated resources")
  val marathonCommonTemplatesDir = settingKey[File]("Target directory for extracted, common templates")
  val marathonCommonGeneratedTemplatesDir = settingKey[File]("Target directory for generated, common templates")
  val marathonTemplates = taskKey[Seq[Template]]("Template instances")
  val marathonGeneratedTemplates = taskKey[Seq[File]]("Results of template evaluation")
  val marathonExtractCommonResources = taskKey[Unit]("Extract common resources from archive")
  val marathonCleanDirs = taskKey[Unit]("Cleans common and generated template directories")
}
