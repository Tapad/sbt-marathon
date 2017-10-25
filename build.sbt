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
    "org.scalatest" %% "scalatest" % ScalaTestVersion % Test
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
    crossSbtVersions := Seq("0.13.16", "1.0.0"),
    libraryDependencies ++= Seq(
      "org.slf4j"      % "slf4j-api"        % "1.7.21",
      "org.slf4j"      % "slf4j-jdk14"      % "1.7.21" % Test,
      "org.scalactic" %% "scalactic"        % ScalacticVersion,
      "com.twitter"   %% "finagle-core"     % finagleVersion(scalaVersion.value),
      "com.twitter"   %% "finagle-http"     % finagleVersion(scalaVersion.value),
      "org.json4sbt"  %% "json4sbt-jackson" % Json4sbtVersion
    ),
    testOptions in Test += Tests.Argument("-l", "sbtmarathon.FunctionalTest")
  )
  .dependsOn(util)

lazy val templating = (project in file("templating"))
  .settings(PluginSettings: _*)
  .settings(PluginPublishSettings: _*)
  .settings(
    name := "sbt-marathon-templating",
    crossSbtVersions := Seq("0.13.16", "1.0.0"),
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "sbtmarathon",
    libraryDependencies ++= {
      val currentSbtVersion = (sbtBinaryVersion in pluginCrossBuild).value
      Seq(
        Defaults.sbtPluginExtra(
          "com.typesafe.sbt" % "sbt-twirl" % "1.3.12",
          currentSbtVersion,
          scalaBinaryVersion.value
        ),
        Defaults.sbtPluginExtra(
          (organization in marathon).value % (name in marathon).value % version.value,
          currentSbtVersion,
          scalaBinaryVersion.value
        )
      )
    },
    sourceDirectories in (Compile, TwirlKeys.compileTemplates) += {
      (resourceDirectory in Compile).value / "templates"
    },
    TwirlKeys.templateFormats ++= Map(
      "sh"    -> "play.twirl.api.TxtFormat",
      "json"  -> "play.twirl.api.TxtFormat"
    )
  )
  .dependsOn(templatingLib)
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
      "com.typesafe.play" %% "twirl-api"        % "1.3.12" % Provided
    ),
    parallelExecution in test := false
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
