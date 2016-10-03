import sbt._

object Dependencies {

  val ScalaVersion = "2.10.6"

  val SupportedScalaVersions = Seq(ScalaVersion, "2.11.8")

  val ScalaTestVersion = "2.2.6"

  val ScalacticVersion = "2.2.6"

  val FinagleVersion = "6.35.0"

  def parserCombinators(scalaVersion: String): Option[ModuleID] = {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 10)) => None
      case _ => Some("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4")
    }
  }
}
