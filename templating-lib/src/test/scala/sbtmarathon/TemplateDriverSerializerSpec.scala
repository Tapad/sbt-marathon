package sbtmarathon

import org.json4sbt._
import org.json4sbt.jackson.JsonMethods
import org.json4sbt.jackson.Serialization.write
import org.scalatest.{FlatSpec, Matchers}

class TemplateDriverSerializerSpec extends FlatSpec with Matchers {

  behavior of "TemplateDriverSerializer"

  it should "serialize drivers with full type information" in {
    val driver = TemplateDriver(
      new {
        val myName = "alice"
        val volumes = Seq("/foo", "/bar")
        val optional = None
      }
    )
    implicit var formats = DefaultFormats.preservingEmptyValues + TemplateDriverSerializer
    val result = write[TemplateDriver](driver)
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

    (resultJson \ "optional" \ "manifest" \ "runtimeName") shouldBe
      JString("scala.None$")

    (resultJson \ "optional" \ "value") shouldBe
      JString("")
  }
}
