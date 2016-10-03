package sbtmarathon

import java.io.File
import sbt._
import sbt.Keys._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import play.twirl.sbt.Import.TwirlKeys

object TemplatingSettings {

  import TemplatingKeys._

  lazy val settings = Seq(
    // configure default first-party template directories
    marathonTemplatesDir := (resourceDirectory in Compile).value / "templates",
    marathonGeneratedDir := (resourceDirectory in Compile).value / "generated",
    marathonCommonTemplatesDir := marathonTemplatesDir.value / "common",
    marathonCommonGeneratedTemplatesDir := marathonGeneratedDir.value / marathonCommonTemplatesDir.value.getName,

    marathonExtractCommonResources := {
      val Some((major, minor)) = CrossVersion.partialVersion(scalaVersion.value)
      val jarName = s"templating-util_$major.$minor"
      val jars = Classpaths.managedJars(Compile, classpathTypes.value, update.value)
      jars.map(_.data).find(_.getName == jarName).foreach { jar =>
        streams.value.log.info(s"Extracting ${jar.getName}'s templated resources to " + marathonCommonTemplatesDir.value)
        IO.unzip(jar, marathonCommonTemplatesDir.value, (fileName: String) => fileName.contains(".scala."))
      }
    },

    marathonCleanDirs := {
      streams.value.log.info("Deleting contents of " + marathonCommonTemplatesDir.value)
      IO.delete(marathonCommonTemplatesDir.value)
      IO.createDirectory(marathonCommonTemplatesDir.value)
      streams.value.log.info("Deleting contents of " + marathonGeneratedDir.value)
      IO.delete(marathonGeneratedDir.value)
      IO.createDirectory(marathonGeneratedDir.value)
    },

    compile in Compile := {
      (compile in Compile).dependsOn(marathonExtractCommonResources).value
    },

    clean := {
      clean.dependsOn(marathonCleanDirs).value
    },

    sourceDirectories in (Compile, TwirlKeys.compileTemplates) += marathonTemplatesDir.value,
    marathonGeneratedTemplates := {
      marathonGeneratedTemplates.dependsOn(compile in (Compile, TwirlKeys.compileTemplates)).value
    },
    TwirlKeys.templateImports ++= Seq("sbtmarathon._"),
    TwirlKeys.templateFormats ++= Map(
      "sh"    -> "play.twirl.api.TxtFormat",
      "json"  -> "play.twirl.api.TxtFormat"
    ),

    marathonTemplates := Seq.empty,
    marathonGeneratedTemplates := {
      implicit val formats = DefaultFormats.preservingEmptyValues + TemplateDriverSerializer
      // ensure compiled templates are on the classpath
      // evaluate each requested template using the given args
      val templatesDir = marathonTemplatesDir.value
      val generatedTemplates = marathonTemplates.value.map {
        case Template(file, driver) =>
          streams.value.log.info(s"Serializing $driver")
          val driverJsonFile = File.createTempFile(s"driver-", ".json")
          val driverJsonString = Serialization.write[TemplateDriver](driver)
          IO.write(file = driverJsonFile, content = driverJsonString)
          streams.value.log.info(s"Generating template @ $file with driver: $driver")
          val classRunner = (runner in Compile).value
          val mainClass = "sbtmarathon.TemplateEvaluatorFacade"
          val classpathFiles =
            (fullClasspath in (Compile, TwirlKeys.compileTemplates)).value.files ++
            (fullClasspath in Compile).value.files
          val resultsFile = File.createTempFile(s"results-", ".txt")
          val opts = Seq(
            file.getAbsolutePath,
            templatesDir.getAbsolutePath,
            marathonGeneratedDir.value.getAbsolutePath,
            driverJsonFile.getAbsolutePath,
            resultsFile.getAbsolutePath
          )
          classRunner.run(mainClass, classpathFiles, opts, streams.value.log)
          new File(IO.read(resultsFile))
      }
      generatedTemplates
    }
  )
}
