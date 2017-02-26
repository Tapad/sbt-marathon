import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import bintray.BintrayKeys._

object Publishing {

  val PublishSettings = Seq(
    autoAPIMappings := true,
    pomIncludeRepository := { _ => false },
    publishArtifact in Test := false,
    publishArtifact in (Compile, packageDoc) := true,
    publishArtifact in (Compile, packageSrc) := true
  )

  val CrossPublishSettings = PublishSettings ++ Seq(
    crossScalaVersions := Dependencies.SupportedScalaVersions
  )

  /* `publish` performs a no-op */
  val NoopPublishSettings = Seq(
    packagedArtifacts in file(".") := Map.empty,
    publish := (),
    publishLocal := (),
    publishArtifact := false,
    publishTo := None
  )

  val PluginPublishSettings = PublishSettings ++ Seq(
    bintrayRepository := "sbt-plugins"
  )

  val LibraryPublishSettings = CrossPublishSettings ++ Seq(
    bintrayRepository := "maven",
    bintrayPackage := "sbt-marathon-libs",
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    homepage := Some(new URL("https://github.com/Tapad/sbt-marathon")),
    pomExtra := {
      <developers>
        <developer>
          <id>jeffreyolchovy</id>
          <name>Jeffrey Olchovy</name>
          <email>jeffo@tapad.com</email>
          <url>https://github.com/jeffreyolchovy</url>
        </developer>
      </developers>
      <scm>
        <url>https://github.com/Tapad/sbt-marathon</url>
        <connection>scm:git:git://github.com/Tapad/sbt-marathon.git</connection>
      </scm>
    }
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
