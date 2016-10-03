package com.tapad.util

import java.util.Comparator
import scala.math.Ordering
import scala.util.parsing.combinator.RegexParsers

/** Semantic versioning struct */
case class Version(major: Int, minor: Int, release: Int, prerelease: Option[String] = None, metadata: Option[String] = None) {

  def this(major: Int, minor: Int, release: Int, prerelease: String) = this(major, minor, release, Some(prerelease))

  require(major >= 0, "`major` part must be a non-negative integer value")

  require(minor >= 0, "`major` part must be a non-negative integer value")

  require(release >= 0, "`release` part must be a non-negative integer value")

  override def toString: String = {
    s"${major}.${minor}.${release}" +
    prerelease.map {
      case token if token.startsWith("rc") => token
      case token => "-" + token
    }.getOrElse("") +
    metadata.map("+" + _).getOrElse("")
  }

  def isRelease = prerelease.isEmpty

  /* Known, common aliases */

  val bugfix = release

  val patch = release
}

object Version {

  def apply(versionString: String): Version = VersionParser(versionString)

  def latest(versions: Seq[Version]): Version = {
    require(versions.nonEmpty)
    versions.sorted.last
  }

  def latestRelease(versions: Seq[Version]): Option[Version] = {
    require(versions.nonEmpty)
    versions.sorted.filter(_.isRelease).lastOption
  }

  /** Parse a Version struct from a string value, following the 'MUST' and 'MAY' specifications outlined at [[http://semver.org]] */
  object VersionParser extends RegexParsers {

    private val EOL: Parser[String] = {
      """\z""".r.withFailureMessage("end-of-input expected")
    }

    private val positiveWholeNumber: Parser[Int] = {
      ("0".r | """[1-9]?\d*""".r).map(_.toInt).withFailureMessage("non-negative integer value expected")
    }

    private val prereleaseParser: Parser[String] = {
      (("-" | "rc") ~ repsep("""[0-9A-Za-z-]+""".r, ".")) ^^ {
        case "-" ~ identifiers => identifiers.mkString(".")
        case prefix ~ identifiers => prefix + identifiers.mkString(".")
      }
    }

    private val metadataParser: Parser[String] = {
      "+" ~> """[0-9A-Za-z-]+""".r
    }

    private val releaseParser: Parser[(String, Option[String])] = {
      positiveWholeNumber ~ opt(excludingCharClass("""\+ """)) ~ opt(metadataParser) ^^ {
        case n ~ Some(prerelease) ~ metadata => (n + prerelease, metadata)
        case n ~ None ~ metadata => (n.toString, metadata)
      }
    }

    val parser: Parser[Version] = {
      opt("v") ~> positiveWholeNumber ~ ("." ~> positiveWholeNumber) ~ ("." ~> positiveWholeNumber) ~ (prereleaseParser ?) ~ (metadataParser ?) <~ EOL ^^ {
        case major ~ minor ~ release ~ prerelease ~ metadata => Version(major, minor, release, prerelease, metadata)
      }
    }

    def apply(versionString: String) = {
      parse(parser, versionString) match {
        case Success(version, _) => version
        case NoSuccess(msg, _) => throw new RuntimeException(s"Could not parse Version from $versionString: $msg")
      }
    }

    private def excludingCharClass(charClass: String, description: Option[String] = None): Parser[String] = {
      ("""([^""" + charClass + """])*""").r.withFailureMessage(description.getOrElse(s"[$charClass]") + " not expected")
    }
  }

  implicit val comparator: Comparator[Version] = new Comparator[Version] {

    import scala.math.Ordering.Implicits._

    def compare(a: Version, b: Version) = {
      compareRelease(a, b) match {
        case 0 if !a.isRelease && !b.isRelease => comparePrerelease(a, b)
        case 0 if a.isRelease && !b.isRelease => 1
        case 0 if !a.isRelease && b.isRelease => -1
        case n => n
      }
    }

    def compareRelease(a: Version, b: Version): Int = {
      val as = (a.major, a.minor, a.release)
      val bs = (b.major, b.minor, b.release)
      if (as < bs) -1 else if (as > bs) 1 else 0
    }

    def comparePrerelease(a: Version, b: Version): Int = {
      require(!a.isRelease && !b.isRelease, "Can not compare prerelease tokens of release versions")
      val ap = a.prerelease.get
      val bp = b.prerelease.get
      val aw = getPrereleasePrefixWeight(ap)
      val bw = getPrereleasePrefixWeight(bp)
      val (aids, bids) = getPrereleaseIdentifiers(ap, bp)
      comparePrereleaseIdentifiers(aw :: aids, bw :: bids)
    }

    def comparePrereleaseIdentifiers(as: Iterable[String], bs: Iterable[String]): Int = {
      var result = 0
      val ai = as.iterator
      val bi = bs.iterator
      while (result == 0 && ai.hasNext && bi.hasNext) {
        val an = ai.next()
        val bn = bi.next()
        result = (isNumericIdentifier(an), isNumericIdentifier(bn)) match {
          case (true, false)  => 1
          case (false, true)  => -1
          case (false, false) => an.compare(bn)
          case (true, true)   => Integer.compare(an.toInt, bn.toInt)
        }
      }
      result
    }

    def getPrereleaseIdentifiers(ap: String, bp: String): (List[String], List[String]) = {
      def stripPrefix(s: String): String = s.replaceFirst("^(rc)?", "")
      // stripped prerelease
      val sap = stripPrefix(ap)
      val sbp = stripPrefix(bp)
      // unpadded prerelease ids
      val uap = sap.split('.').toList
      val ubp = sbp.split('.').toList
      val width = math.max(uap.size, ubp.size)
      // padded prerelease ids
      (uap.padTo(width, "0"), ubp.padTo(width, "0"))
    }

    def getPrereleasePrefixWeight(prerelease: String): String = {
      if (prerelease.startsWith("rc")) "1" else "0"
    }

    private def isNumericIdentifier(identifier: String): Boolean = {
      identifier.matches("[0-9]*")
    }
  }

  implicit val ordering: Ordering[Version] = Ordering.comparatorToOrdering[Version](comparator)
}
