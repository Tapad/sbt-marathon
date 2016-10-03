import sbt._

object Repositories {

  val TapadSnapshots = "Tapad Nexus Snapshots" at "http://nexus.tapad.com:8080/nexus/content/repositories/snapshots"

  val TapadReleases = "Tapad Nexus Releases" at "http://nexus.tapad.com:8080/nexus/content/repositories/releases"

  val TapadAggregate = "Tapad Aggregate" at "http://nexus.tapad.com:8080/nexus/content/groups/aggregate"

  val SonatypeSnapshots = "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

  val MavenCentral = "Maven Central" at "http://repo1.maven.org/maven2"

  val LocalMaven = "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
}
