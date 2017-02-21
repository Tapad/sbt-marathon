package sbtmarathon

import scala.reflect.{Manifest, ManifestFactory}
import org.json4sbt.{Formats, JValue, JObject, JArray, JString}

object ManifestSerialization {

  private val NameKey = "runtimeName"

  private val TypeParamsKey = "typeArguments"

  def manifestOf[A : Manifest]: Manifest[_] = manifest[A]

  def emit(m: Manifest[_]): JValue = {
    val head = NameKey -> JString(m.runtimeClass.getName)
    val tail = m.typeArguments.map(emit) match {
      case Nil => Nil
      case jvalues => (TypeParamsKey -> JArray(jvalues)) :: Nil
    }
    JObject((head :: tail): _*)
  }

  def parse(jvalue: JValue)(implicit formats: Formats): Manifest[_] = {
    val runtimeManifest = manifestFromString((jvalue \ NameKey).extract[String])
    val typeArguments = jvalue \ TypeParamsKey match {
      case JArray(jvalues) => jvalues.map(parse)
      case _ => Nil
    }
    typeArguments match {
      case Nil => runtimeManifest
      case head :: tail => ManifestFactory.classType(runtimeManifest.runtimeClass, head, tail: _*)
    }
  }

  private def manifestFromString(value: String): Manifest[_] = value match {
    case "boolean"  => manifestOf[Boolean]
    case "byte"     => manifestOf[Byte]
    case "char"     => manifestOf[Char]
    case "short"    => manifestOf[Short]
    case "int"      => manifestOf[Int]
    case "long"     => manifestOf[Long]
    case "float"    => manifestOf[Float]
    case "double"   => manifestOf[Double]
    case _          => ManifestFactory.classType(Class.forName(value))
  }
}
