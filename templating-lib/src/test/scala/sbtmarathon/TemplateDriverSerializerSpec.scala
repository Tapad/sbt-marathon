package sbtmarathon

import org.json4sbt._
import org.json4sbt.jackson.JsonMethods
import org.json4sbt.jackson.Serialization.write
import org.scalatest.{FlatSpec, Matchers}

class TemplateDriverSerializerSpec extends FlatSpec with Matchers {

  import TemplateDriverSerializerSpec._

  behavior of "TemplateDriverSerializer"

  it should "serialize drivers with full type information" in {
    implicit var formats = DefaultFormats.preservingEmptyValues + TemplateDriverSerializer
    val result = write[TemplateDriver](SampleDriver)
    val resultJson = JsonMethods.parse(result)

    (resultJson \ "myName" \ "manifest" \ "runtimeName") shouldBe
      JString("java.lang.String")

    (resultJson \ "myName" \ "value") shouldBe
      JString(""""alice"""")

    (resultJson \ "volumes" \ "manifest" \ "runtimeName") shouldBe
      JString("scala.collection.Seq")

    ((resultJson \ "volumes" \ "manifest" \ "typeArguments")(0) \ "runtimeName") shouldBe
      JString("java.lang.String")

    (resultJson \ "volumes" \ "value") shouldBe
      JString("""["/foo","/bar"]""")

    (resultJson \ "none" \ "manifest" \ "runtimeName") shouldBe
      JString("scala.None$")

    (resultJson \ "none" \ "value") shouldBe
      JString("")
  }
}

object TemplateDriverSerializerSpec {
  val SampleDriver = TemplateDriver(
    new {
      val myName = "alice"
      val volumes = Seq("/foo", "/bar")
      val someString: Option[String] = Some("foo")
      val someEmptyString: Option[String] = Some("")
      val noneString: Option[String] = None
      val none = None
    }
  )
}
