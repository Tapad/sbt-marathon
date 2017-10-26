package sbtmarathon

import java.io.File
import sbt._
import Keys._
import org.json4sbt.DefaultFormats
import org.json4sbt.jackson.Serialization
import play.twirl.sbt.SbtTwirl
import play.twirl.sbt.Import.TwirlKeys

object TemplatingPlugin extends AutoPlugin {

  object autoImport {
    val Template = sbtmarathon.Template
    type Template = sbtmarathon.Template
    val TemplateDriver = sbtmarathon.TemplateDriver
    type TemplateDriver = sbtmarathon.TemplateDriver
    val Templating = config("templating").extend(Compile)
    val TemplatingKeys = sbtmarathon.TemplatingKeys
    val marathonTemplates = TemplatingKeys.marathonTemplates
    val marathonEvaluateTemplates = TemplatingKeys.marathonEvaluateTemplates
    implicit def newDriver(a: AnyRef): TemplateDriver = TemplateDriver(a)
  }

  import autoImport._

  override def requires = MarathonPlugin && SbtTwirl

  override def projectSettings = inConfig(Templating)(scopedSettings) ++ Seq(
    ivyConfigurations += Templating,
    TwirlKeys.templateFormats ++= Map("json" -> "play.twirl.api.TxtFormat"),
    sourceDirectories in (Compile, TwirlKeys.compileTemplates) += (sourceDirectory in Templating).value,
    marathonTemplates := Seq.empty,
    marathonEvaluateTemplates := {
      implicit val formats = DefaultFormats.preservingEmptyValues + TemplateDriverSerializer
      val log = streams.value.log
      marathonTemplates.value.map {
        case Template(file, driver) =>
          log.debug(s"Serializing $driver")
          val driverJsonFile = File.createTempFile(s"driver-", ".json")
          val driverJsonString = Serialization.write[TemplateDriver](driver)
          IO.write(file = driverJsonFile, content = driverJsonString)
          log.info(s"Evaluating template $file")
          log.debug(s"Using driver $driver")
          val classRunner = (runner in Compile).value
          val mainClass = "sbtmarathon.TemplateEvaluatorFacade"
          val classpathFiles =
            (fullClasspath in (Compile, TwirlKeys.compileTemplates)).value.files ++
            (fullClasspath in Compile).value.files ++
            (managedClasspath in Templating).value.files
          val resultsFile = File.createTempFile(s"results-", ".txt")
          val opts = Seq(
            file.getAbsolutePath,
            (sourceDirectory in Templating).value.getAbsolutePath,
            (target in Templating).value.getAbsolutePath,
            driverJsonFile.getAbsolutePath,
            resultsFile.getAbsolutePath
          )
          classRunner.run(mainClass, classpathFiles, opts, log)
          new File(IO.read(resultsFile))
      }
    },
    marathonEvaluateTemplates := {
      marathonEvaluateTemplates.dependsOn(compile in (Compile, TwirlKeys.compileTemplates)).value
    },
    clean := {
      clean.dependsOn(clean in Templating).value
    },
    libraryDependencies += "com.tapad.sbt" %% "marathon-templating-lib" % BuildInfo.version % Templating.name,
    managedClasspath in Templating := {
      val artifactTypes: Set[String] = (classpathTypes in Templating).value
      Classpaths.managedJars(Templating, artifactTypes, update.value)
    }
  )

  private def scopedSettings = Seq(
    sourceDirectory := (resourceDirectory in Compile).value / "templates",
    target := (resourceDirectory in Compile).value / "generated",
    clean := {
      streams.value.log.info("Deleting contents of " + target.value)
      IO.delete(target.value)
      IO.createDirectory(target.value)
    }
  )
}
