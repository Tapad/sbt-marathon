import Dependencies._
import Publishing._
import SubProject._

lazy val root = (project in file("."))
  .settings(MinimalSettings: _*)
  .settings(NoopPublishSettings: _*)
  .aggregate(marathon, docker, nativePackager, templating, templatingUtil, util)
  .enablePlugins(CrossPerProjectPlugin)

lazy val marathon = (project in file("marathon"))
  .settings(PluginSettings: _*)
  .settings(PublishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic"    % ScalacticVersion,
      "com.twitter"   %% "finagle-core" % FinagleVersion,
      "com.twitter"   %% "finagle-http" % FinagleVersion
    )
  )

lazy val docker = (project in file("docker"))
  .settings(PluginSettings: _*)
  .settings(PublishSettings: _*)
  .settings(
    name := "sbt-docker-for-marathon"
  )
  .dependsOn(marathon)
  .enablePlugins(sbtdocker.DockerPlugin)

lazy val nativePackager = (project in file("native-packager"))
  .settings(PluginSettings: _*)
  .settings(PublishSettings: _*)
  .settings(
    name := "sbt-native-packager-for-marathon"
  )
  .dependsOn(marathon)
  .enablePlugins(com.typesafe.sbt.packager.docker.DockerPlugin)

lazy val templating = (project in file("templating"))
  .settings(PluginSettings: _*)
  .settings(PublishSettings: _*)
  .settings(
    name := "sbt-marathon-templating",
    addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.0.3"),
    sourceDirectories in (Compile, TwirlKeys.compileTemplates) += {
      (resourceDirectory in Compile).value / "templates"
    },
    TwirlKeys.templateFormats ++= Map(
      "sh"    -> "play.twirl.api.TxtFormat",
      "json"  -> "play.twirl.api.TxtFormat"
    )
  )
  .dependsOn(marathon, templatingUtil)
  .enablePlugins(BuildInfoPlugin, SbtTwirl)

lazy val templatingUtil = (project in file("templating-util"))
  .settings(CommonSettings: _*)
  .settings(CrossPublishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang"     % "scala-reflect"  % scalaVersion.value,
      "org.slf4j"          % "slf4j-api"      % "1.7.21",
      "org.json4s"        %% "json4s-jackson" % "3.4.1",
      "com.typesafe.play" %% "twirl-api"      % "1.1.1" % "provided"
    )
  )

lazy val util = (project in file("util"))
  .settings(CommonSettings: _*)
  .settings(CrossPublishSettings: _*)
  .settings(
    libraryDependencies := parserCombinators(scalaVersion.value).fold(libraryDependencies.value) {
      libraryDependencies.value :+ _
    }
  )
