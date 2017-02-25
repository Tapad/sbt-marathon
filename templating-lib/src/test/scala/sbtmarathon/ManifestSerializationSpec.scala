package sbtmarathon

import scala.reflect.Manifest
import org.json4sbt._
import org.json4sbt.jackson.JsonMethods
import org.scalatest.{FlatSpec, Matchers}

class ManifestSerializationSpec extends FlatSpec with Matchers {

  import ManifestSerialization._

  behavior of "ManifestSerializer"

  Seq(
    manifestOf[String],
    manifestOf[Int],
    manifestOf[Array[String]],
    manifestOf[List[String]],
    manifestOf[Map[String, Seq[Int]]],
    manifestOf[Map[String, Seq[Map[java.lang.Integer, String]]]]
  ).foreach { m =>
    it should s"emit and parse JSON representation of Manifest[$m]" in {
      ensureSuccessfulRoundTrip(m)
    }
  }

  def ensureSuccessfulRoundTrip(manifest: Manifest[_]): Unit = {
    implicit val formats = DefaultFormats
    val json = JsonMethods.compact(JsonMethods.render(emit(manifest)))
    manifest shouldBe parse(JsonMethods.parse(json))
  }
}
