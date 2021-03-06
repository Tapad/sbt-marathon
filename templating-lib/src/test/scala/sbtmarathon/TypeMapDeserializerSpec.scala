package sbtmarathon

import org.json4sbt._
import org.json4sbt.jackson.Serialization.{read, write}
import org.scalatest.{FlatSpec, Matchers}

class TypeMapDeserializerSpec extends FlatSpec with Matchers {

  behavior of "TypeMapDeserializer"

  it should "extract type information from serialized driver data" in {
    implicit var formats = DefaultFormats + TemplateDriverSerializer
    val json = write[TemplateDriver](TemplateDriverSerializerSpec.SampleDriver)
    formats = DefaultFormats + TypeMapDeserializer
    val result = read[TypeMap](json)
    val resultStrings = result.underlying.mapValues(_.toString)
    resultStrings should contain ("myName" -> "java.lang.String")
    resultStrings should contain ("volumes" -> "scala.collection.Seq[java.lang.String]")
    resultStrings should contain ("someString" -> "scala.Option[java.lang.String]")
    resultStrings should contain ("someEmptyString" -> "scala.Option[java.lang.String]")
    resultStrings should contain ("noneString" -> "scala.Option[java.lang.String]")
    resultStrings should contain ("none" -> "scala.None$")
  }
}
