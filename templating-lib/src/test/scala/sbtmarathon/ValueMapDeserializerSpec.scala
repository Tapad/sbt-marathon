package sbtmarathon

import org.json4sbt._
import org.json4sbt.jackson.Serialization.{read, write}
import org.scalatest.{FlatSpec, Matchers}

class ValueMapDeserializerSpec extends FlatSpec with Matchers {

  behavior of "ValueMapDeserializer"

  it should "extract data values from serialized driver data" in {
    implicit var formats = DefaultFormats.preservingEmptyValues + TemplateDriverSerializer
    val json = write[TemplateDriver](TemplateDriverSerializerSpec.SampleDriver)
    formats = DefaultFormats + ValueMapDeserializer
    val results = read[ValueMap](json)
    results.underlying should contain ("myName" -> "alice")
    results.underlying should contain ("volumes" -> Seq("/foo", "/bar"))
    results.underlying should contain ("someString" -> Some("foo"))
    results.underlying should contain ("someEmptyString" -> Some(""))
    results.underlying should contain ("noneString" -> None)
    results.underlying should contain ("none" -> None)
  }
}
