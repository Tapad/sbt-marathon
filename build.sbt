import Dependencies._
import Publishing._

/* The base, minimal settings for every project, including the root aggregate project */
val BaseSettings = Seq(
  organization := "com.tapad.sbt",
  licenses += ("BSD New", url("https://opensource.org/licenses/BSD-3-Clause")),
  scalaVersion := Dependencies.ScalaVersion
)

/* Common settings for all non-aggregate subprojects */
val CommonSettings = BaseSettings ++ Seq(
  scalacOptions ++= Seq("-deprecation", "-language:_"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % ScalaTestVersion % "test"
  )
)

val PluginSettings = CommonSettings ++ scriptedSettings ++ Seq(
  sbtPlugin := true,
  name := "sbt-" + name.value,
  scriptedLaunchOpts ++= Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value),
  scriptedBufferLog := false
)

lazy val root = (project in file("."))
  .settings(BaseSettings: _*)
  .settings(NoopPublishSettings: _*)
  .settings(ReleaseSettings: _*)
  .aggregate(marathon, templating, templatingLib, util)
  .enablePlugins(CrossPerProjectPlugin)

lazy val marathon = (project in file("marathon"))
  .settings(PluginSettings: _*)
  .settings(PluginPublishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.slf4j"      % "slf4j-api"        % "1.7.21",
      "org.slf4j"      % "slf4j-jdk14"      % "1.7.21" % "test",
      "org.scalactic" %% "scalactic"        % ScalacticVersion,
      "com.twitter"   %% "finagle-core"     % FinagleVersion,
      "com.twitter"   %% "finagle-http"     % FinagleVersion,
      "org.json4sbt"  %% "json4sbt-jackson" % Json4sbtVersion
    ),
    publishLocal := {
      (publishLocal.dependsOn(publishLocal in util)).value
    }
  )
  .dependsOn(util)

lazy val templating = (project in file("templating"))
  .settings(PluginSettings: _*)
  .settings(PluginPublishSettings: _*)
  .settings(
    name := "sbt-marathon-templating",
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "sbtmarathon",
    addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.0.3"),
    sourceDirectories in (Compile, TwirlKeys.compileTemplates) += {
      (resourceDirectory in Compile).value / "templates"
    },
    TwirlKeys.templateFormats ++= Map(
      "sh"    -> "play.twirl.api.TxtFormat",
      "json"  -> "play.twirl.api.TxtFormat"
    ),
    publishLocal := {
      (publishLocal.dependsOn(publishLocal in marathon)).value
    },
    publishLocal := {
      (publishLocal.dependsOn(publishLocal in templatingLib)).value
    }
  )
  .dependsOn(marathon, templatingLib)
  .enablePlugins(BuildInfoPlugin, SbtTwirl)

lazy val templatingLib = (project in file("templating-lib"))
  .settings(CommonSettings: _*)
  .settings(LibraryPublishSettings: _*)
  .settings(
    name := "marathon-templating-lib",
    libraryDependencies ++= Seq(
      "org.scala-lang"     % "scala-reflect"    % scalaVersion.value,
      "org.slf4j"          % "slf4j-api"        % "1.7.21",
      "org.json4sbt"      %% "json4sbt-jackson" % "3.4.1",
      "com.typesafe.play" %% "twirl-api"        % "1.1.1" % "provided"
    )
  )

lazy val util = (project in file("util"))
  .settings(CommonSettings: _*)
  .settings(LibraryPublishSettings: _*)
  .settings(
    name := "marathon-util",
    libraryDependencies := parserCombinators(scalaVersion.value).fold(libraryDependencies.value) {
      libraryDependencies.value :+ _
    }
  )
