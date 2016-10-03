package sbtmarathon

import org.json4s._
import org.json4s.jackson.JsonMethods._

case class TypeMap(underlying: Map[String, Manifest[_]])

object TypeMapDeserializer extends Serializer[TypeMap] {

  private val TypeMapClass = classOf[TypeMap]

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    throw new UnsupportedOperationException
  }

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), TypeMap] = {
    case (TypeInfo(TypeMapClass, _), json) => json match {
      case JObject(jfields) => TypeMap(
        jfields.map {
          case (name, JObject(JField("manifest", jvalue) :: _)) => name -> ManifestSerialization.parse(jvalue)
          case _ => throw new MappingException(s"Can not convert JSON to TypeMap: `manifest` field expected")
        }.toMap
      )
      case _ => throw new MappingException(s"Can not convert JSON to TypeMap: top-level JSON object expected")
    }
  }
}
