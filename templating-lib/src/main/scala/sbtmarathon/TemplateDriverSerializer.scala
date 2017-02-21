package sbtmarathon

import scala.reflect.api._
import scala.reflect.ManifestFactory
import scala.reflect.runtime.universe._
import org.json4sbt._
import org.json4sbt.jackson.JsonMethods._
import org.json4sbt.jackson.Serialization

object TemplateDriverSerializer extends Serializer[TemplateDriver] {

  private val mirror = runtimeMirror(getClass.getClassLoader)

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), TemplateDriver] = {
    throw new UnsupportedOperationException
  }

  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
    case TemplateDriver(instance) =>
      val manifests = manifestsByName(instance)
      val jfields = valuesByName(instance).toSeq.map {
        case (name, value) => name -> JObject(
          "manifest" -> ManifestSerialization.emit(manifests(name)),
          "value" -> JString(Serialization.write(value.asInstanceOf[AnyRef])(formats))
        )
      }
      JObject(jfields: _*)
  }

  def termSymbolsByName(instance: AnyRef): Map[String, TermSymbol] = {
    val instanceType = mirror.classSymbol(instance.getClass).toType
    val reservedNames = typeOf[AnyRef].members.map(_.name.decoded).toSet
    instanceType.members.map { symbol =>
      symbol.name.decoded -> symbol
    }.collect {
      case (name, symbol)
        if !reservedNames.contains(name)
        && symbol.isTerm
        && !symbol.isSynthetic => name -> symbol.asTerm
    }(scala.collection.breakOut)
  }

  def manifestsByName(instance: AnyRef): Map[String, Manifest[_]] = {
    val typesByName: Map[String, Type] = termSymbolsByName(instance).map {
      case (name, symbol) if symbol.isMethod => name -> symbol.asMethod.returnType
      case (name, symbol) => name -> symbol.typeSignature
    }
    typesByName.mapValues(typeToManifest)
  }

  def valuesByName(instance: AnyRef): Map[String, Any] = {
    termSymbolsByName(instance).mapValues {
      case symbol if symbol.isMethod => mirror.reflect(instance).reflectMethod(symbol.asMethod).apply()
      case symbol => mirror.reflect(instance).reflectField(symbol).get
    }
  }

  def typeToManifest(tpe: Type): Manifest[_] = {
    val runtimeClass = mirror.runtimeClass(tpe)
    val typeArguments = tpe.asInstanceOf[TypeRefApi].args.map(typeToManifest)
    typeArguments match {
      case Nil => ManifestFactory.classType(runtimeClass)
      case head :: tail => ManifestFactory.classType(runtimeClass, head, tail: _*)
    }
  }
}
