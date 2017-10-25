import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import bintray.BintrayKeys._

object Publishing {

  val PublishSettings = Seq(
    autoAPIMappings := true,
    bintrayOrganization := Some("tapad-oss"),
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
    packagedArtifacts in RootProject(file(".")) := Map.empty,
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
    releaseProcess := Seq[ReleaseStep](
      inquireVersions,
      setReleaseVersion,
      withDiscreteSbtProcess(
        "root"          -> "^clean",
        "util"          -> "+test",
        "util"          -> "+publishLocal",
        "templatingLib" -> "+test",
        "templatingLib" -> "+publishLocal",
        "marathon"      -> "^test"
      ),
      releaseStepCommand("project root"),
      commitReleaseVersion,
      tagRelease,
      withDiscreteSbtProcess(
        "util"          -> "+publish",
        "marathon"      -> "^publish",
        "templatingLib" -> "+publish",
        "templating"    -> "^publish"
      ),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )

  private def withDiscreteSbtProcess(commandsByProjectName: (String, String)*) = (state: State) => {
    commandsByProjectName.foldLeft(state) { case (state, (projectName, command)) =>
      val result = Process("sbt" :: s";project $projectName" :: command :: Nil).!
      if (result != 0) {
        sys.error(s"An error was encountered during the release process ($projectName/$command)")
      } else {
        state
      }
    }
  }
}
