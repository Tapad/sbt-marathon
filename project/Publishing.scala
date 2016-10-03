import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import Repositories._

object Publishing {

  /* Publish artifacts to the respective Tapad repository */
  val PublishSettings = Seq(
    autoAPIMappings := true,
    pomIncludeRepository := { _ => false },
    publishArtifact in Test := false,
    publishArtifact in (Compile, packageDoc) := true,
    publishMavenStyle := true,
    publishTo := {
      if (version.value.endsWith("SNAPSHOT")) Some(TapadSnapshots) else Some(TapadReleases)
    }
  )

  val CrossPublishSettings = PublishSettings ++ Seq(
    crossScalaVersions := Dependencies.SupportedScalaVersions,
    releaseCrossBuild := true
  )

  /* `publish` performs a no-op */
  val NoopPublishSettings = Seq(
    releaseCrossBuild := true,
    publish := (),
    publishLocal := (),
    publishArtifact := false,
    publishTo := Some(LocalMaven)
  )
}
