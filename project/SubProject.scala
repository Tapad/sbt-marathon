import sbt._
import sbt.Keys._
import sbt.ScriptedPlugin._

object SubProject {

  /* The base, minimal settings for every project, including the root aggregate project */
  val MinimalSettings = Seq(
    organization := "com.tapad.sbt",
    scalaVersion := Dependencies.ScalaVersion
  )

  /* Common settings for all non-aggregate subprojects */
  val CommonSettings = MinimalSettings ++ Seq(
    scalacOptions ++= Seq("-deprecation", "-language:_"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    resolvers ++= Seq(
      Repositories.SonatypeSnapshots,
      Repositories.MavenCentral,
      Repositories.LocalMaven
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % Dependencies.ScalaTestVersion % "test"
    )
  )

  val PluginSettings = CommonSettings ++ scriptedSettings ++ Seq(
    sbtPlugin := true,
    name := "sbt-" + name.value,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false
  )
}
