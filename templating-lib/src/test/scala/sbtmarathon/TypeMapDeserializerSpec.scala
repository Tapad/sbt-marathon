package sbtmarathon

import org.json4sbt._
import org.json4sbt.jackson.Serialization.{read, write}
import org.scalatest.{FlatSpec, Matchers}

class TypeMapDeserializerSpec extends FlatSpec with Matchers {

  behavior of "TypeMapDeserializer"

  it should "extract type information from serialized driver data" in {
    val driver = TemplateDriver(
      new {
        val myName = "alice"
        val volumes = Seq("/foo", "/bar")
      }
    )
    implicit var formats = DefaultFormats + TemplateDriverSerializer
    val json = write[TemplateDriver](driver)
    formats = DefaultFormats + TypeMapDeserializer
    val result = read[TypeMap](json)
    val resultStrings = result.underlying.mapValues(_.toString)
    resultStrings should contain ("myName" -> "java.lang.String")
    resultStrings should contain ("volumes" -> "scala.collection.Seq[java.lang.String]")
  }
}
