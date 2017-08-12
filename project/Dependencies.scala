import sbt._

object Dependencies {

  val ScalaVersion = "2.10.6"

  val SupportedScalaVersions = Seq(ScalaVersion, "2.11.11", "2.12.3")

  val ScalaTestVersion = "3.0.1"

  val ScalacticVersion = "3.0.1"

  val Json4sbtVersion = "3.4.1"

  def finagleVersion(scalaVersion: String): String = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 10)) => "6.35.0"
      case _ => "6.45.0"
    }
  }

  def parserCombinators(scalaVersion: String): Option[ModuleID] = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 10)) => None
      case _ => Some("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4")
    }
  }
}
