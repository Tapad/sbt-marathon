package com.tapad.util

import org.scalatest.FlatSpec

class VersionSpec extends FlatSpec {

  import Version._

  behavior of "Version"

  it should "enforce its non-negative value preconditions upon instance creation" in {
    intercept[IllegalArgumentException] {
      new Version(-1, 1, 1, "rc0")
    }
    intercept[IllegalArgumentException] {
      new Version(1, -1, 1, "rc0")
    }
    intercept[IllegalArgumentException] {
      new Version(1, 1, -1, "rc0")
    }
  }

  it should "extract major, minor, and release parts from a version string" in {
    val input = "0.1.2"
    val version = Version(input)
    assert(version.major == 0)
    assert(version.minor == 1)
    assert(version.release == 2)
    assert(version.isRelease)
  }

  it should "disallow negative values for major, minor, and release parts" in {
    intercept[RuntimeException] {
      Version("-1.1.2")
    }
    intercept[RuntimeException] {
      Version("1.-1.2")
    }
    intercept[RuntimeException] {
      Version("1.1.-2")
    }
  }

  it should "disallow multiple, leading zeros from major, minor, and release parts" in {
    intercept[RuntimeException] {
      Version("00.1.2")
    }
    intercept[RuntimeException] {
      Version("1.00.2")
    }
    intercept[RuntimeException] {
      Version("1.0.02")
    }
  }

  it should "optionally allow a 'v' prefix in version strings" in {
    val input = "v0.1.2"
    val version = Version(input)
    assert(version.major == 0)
    assert(version.minor == 1)
    assert(version.release == 2)
  }

  it should "allow the inclusion of prerelease identifiers, via exclusive '-'" in {
    val input = "0.1.2-alpha"
    val version = Version(input)
    assert(version.major == 0)
    assert(version.minor == 1)
    assert(version.release == 2)
    assert(version.prerelease.get == "alpha")
  }

  it should "allow the inclusion of prerelease identifiers, via the non-standard, inclusive 'rc' token" in {
    val input = "0.1.2rc0"
    val version = Version(input)
    assert(version.major == 0)
    assert(version.minor == 1)
    assert(version.release == 2)
    assert(version.prerelease.get == "rc0")
  }

  it should "allow the inclusion of dot-separated prerelease identifiers" in {
    val input = "0.1.2-0.3.7"
    val version = Version(input)
    assert(version.major == 0)
    assert(version.minor == 1)
    assert(version.release == 2)
    assert(version.prerelease.get == "0.3.7")
  }

  it should "allow the inclusion of arbitrary, non-metadata strings in the release part to signify a pre-release version" in {
    val input = "0.1.2rc0"
    val version = Version(input)
    assert(version.major == 0)
    assert(version.minor == 1)
    assert(version.release == 2)
    assert(version.prerelease.get == "rc0")
  }

  it should "allow the inclusion of metadata in the release part" in {
    val input = "0.1.2rc0+2015-12-31"
    val version = Version(input)
    assert(version.major == 0)
    assert(version.minor == 1)
    assert(version.release == 2)
    assert(!version.isRelease)
    assert(version.prerelease.get == "rc0")
    assert(version.metadata.nonEmpty)
    assert(version.metadata.get == "2015-12-31")
  }

  it should "print all version information without the 'v' prefix" in {
    {
      val version = Version(2, 1, 3)
      assert(version.toString == "2.1.3")
    }
    {
      val version = new Version(2, 1, 3, "alpha")
      assert(version.toString == "2.1.3-alpha")
    }
    {
      val version = Version(2, 1, 3, Some("alpha"), Some("20150101"))
      assert(version.toString == "2.1.3-alpha+20150101")
    }
    {
      val version = Version(2, 1, 3, None, Some("20150101"))
      assert(version.toString == "2.1.3+20150101")
    }
    {
      val input = "v0.1.2rc0+2015-12-31"
      val version = Version(input)
      assert(version.toString == "0.1.2rc0+2015-12-31")
    }
  }

  it should "print an 'rc' version without the pre-release hyphen prefix" in {
    val version = new Version(2, 1, 3, "rc0")
    assert(version.toString == "2.1.3rc0")
  }

  behavior of "Version.comparator"

  it should "compare equivalent release versions" in {
    {
      val a = Version("1.0.0")
      val b = Version("1.0.0")
      assert(comparator.compare(a, b) == 0)
    }
    {
      val a = Version("0.4.0")
      val b = Version("0.4.0")
      assert(comparator.compare(a, b) == 0)
    }
    {
      val a = Version("9.0.3")
      val b = Version("9.0.3")
      assert(comparator.compare(a, b) == 0)
    }
  }

  it should "compare differing release versions" in {
    {
      val a = Version("1.0.0")
      val b = Version("1.0.1")
      assert(comparator.compare(a, b) == -1)
    }
    {
      val a = Version("3.3.0")
      val b = Version("3.5.0")
      assert(comparator.compare(a, b) == -1)
    }
    {
      val a = Version("3.3.0")
      val b = Version("6.0.0")
      assert(comparator.compare(a, b) == -1)
    }
  }

  it should "compare a release and prerelease version" in {
    // a release has precedence over a pre-release
    val a = Version("1.0.0rc0")
    val b = Version("1.0.0")
    assert(comparator.compare(a, b) == -1)
  }

  it should "compare prerelease versions" in {
    val a = Version("1.0.0-0.0.1")
    val b = Version("1.0.0-0.0.2")
    assert(comparator.compare(a, b) == -1)
  }

  it should "compare a release and prerelease of a differing release version" in {
    val a = Version("1.0.0-0.0.2")
    val b = Version("1.0.1")
    assert(comparator.compare(a, b) == -1)
  }

  it should "compare prerelease versions of differing weight" in {
    val a = Version("1.0.0-0.1")
    val b = Version("1.0.0rc0")
    assert(comparator.compare(a, b) == -1)
  }

  behavior of "Version.ordering"

  it should "sort Version instances" in {
    val versions = Seq(Version("4.0.1"), Version("7.1.1"), Version("1.0.2"))
    assert(versions.sorted == Seq(Version("1.0.2"), Version("4.0.1"), Version("7.1.1")))
  }

  it should "sort a heterogeneous releasVersion instances" in {
    val versions = Seq(Version("1.0.2"), Version("4.0.1rc0"), Version("4.0.1rc1"), Version("4.0.1rc2"), Version("4.0.1"), Version("7.1.1"))
    val shuffledVersions = scala.util.Random.shuffle(versions)
    assert(shuffledVersions.sorted == versions)
  }

  behavior of "Version.latest"

  it should "throw when given an empty list of versions"in {
    intercept[IllegalArgumentException] {
      Version.latest(Seq.empty[Version])
    }
  }

  it should "return the latest known version when given a list of releases" in {
    val versions = Seq(Version("1.0.0"), Version("1.0.1"), Version("1.0.2"), Version("1.1.0"), Version("2.0.0"), Version("2.0.1"))
    val shuffledVersions = scala.util.Random.shuffle(versions)
    assert(Version.latest(shuffledVersions) == Version("2.0.1"))
  }

  it should "return the latest known version when given a list of pre-releases" in {
    val versions = Seq(Version("1.1.12-SNAPSHOT"), Version("1.1.13-SNAPSHOT"), Version("1.2.12-SNAPSHOT"), Version("2.1.12-SNAPSHOT"))
    val shuffledVersions = scala.util.Random.shuffle(versions)
    assert(Version.latest(shuffledVersions) == Version("2.1.12-SNAPSHOT"))
  }

  it should "return the latest known version when given a heterogeneous list of releases and pre-releases" in {
    val versions = Seq(Version("1.1.12-SNAPSHOT"), Version("1.1.13-SNAPSHOT"), Version("1.2.12-SNAPSHOT"), Version("2.1.12-SNAPSHOT"), Version("2.1.12"), Version("2.1.13-SNAPSHOT"))
    val shuffledVersions = scala.util.Random.shuffle(versions)
    assert(Version.latest(shuffledVersions) == Version("2.1.13-SNAPSHOT"))
  }

  behavior of "Version.latestRelease"

  it should "throw when given an empty list of versions"in {
    intercept[IllegalArgumentException] {
      Version.latestRelease(Seq.empty[Version])
    }
  }

  it should "return None when no known release versions exist" in {
    val versions = Seq(Version("1.1.12-SNAPSHOT"), Version("1.1.13-SNAPSHOT"), Version("1.2.12-SNAPSHOT"), Version("2.1.12-SNAPSHOT"))
    val shuffledVersions = scala.util.Random.shuffle(versions)
    assert(Version.latestRelease(shuffledVersions).isEmpty)
  }

  it should "return the latest known release when given a heterogeneous list of releases and pre-releases" in {
    val versions = Seq(Version("1.1.12"), Version("1.1.13-SNAPSHOT"), Version("1.2.12"), Version("2.1.12-SNAPSHOT"), Version("2.1.12"))
    val shuffledVersions = scala.util.Random.shuffle(versions)
    assert(Version.latestRelease(shuffledVersions).exists(_ == Version("2.1.12")))
  }

  it should "return the latest known release when given a list of releases" in {
    val versions = Seq(Version("1.0.0"), Version("1.0.1"), Version("1.0.2"), Version("1.1.0"), Version("2.0.0"), Version("2.0.1"))
    val shuffledVersions = scala.util.Random.shuffle(versions)
    assert(Version.latestRelease(shuffledVersions).exists(_ == Version("2.0.1")))
  }
}
