package sbtmarathon

import org.json4s._
import org.json4s.jackson.Serialization.read

case class ValueMap(underlying: Map[String, Any])

object ValueMapDeserializer extends Serializer[ValueMap] {

  private val ValueMapClass = classOf[ValueMap]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ValueMap] = {
    case (TypeInfo(ValueMapClass, _), json) => json match {
      case JObject(jfields) => ValueMap(
        jfields.map {
          case (name, jobject: JObject) => try {
            val manifest = ManifestSerialization.parse((jobject \ "manifest").extract[JObject])
            name -> read((jobject \ "value").extract[String])(DefaultFormats, manifest)
          } catch {
            case e: Exception => throw new MappingException(s"Can not convert JSON to ValueMap: ${e.getMessage}", e)
          }
          case (name, _) => throw new MappingException(s"Can not convert JSON to ValueMap: object literal expected @ $name")
        }(scala.collection.breakOut)
      )
      case _ => throw new MappingException(s"Can not convert JSON to ValueMap: top-level JSON object expected")
    }
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    throw new UnsupportedOperationException
  }
}
