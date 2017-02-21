import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
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
    crossScalaVersions := Dependencies.SupportedScalaVersions
  )

  /* `publish` performs a no-op */
  val NoopPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false,
    publishTo := Some(LocalMaven)
  )

  val ReleaseSettings = Seq(
    releaseCrossBuild := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}
