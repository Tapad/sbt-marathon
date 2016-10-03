package sbtmarathon

import java.io.File
import java.nio.file.Path
import scala.reflect.runtime.universe._
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import org.slf4j.LoggerFactory
import play.twirl.api.BufferedContent
import sbtmarathon.TemplateUtils._

object TemplateEvaluatorFacade {

  import TemplateEvaluator._

  implicit val formats = DefaultFormats ++ Seq(TypeMapDeserializer, ValueMapDeserializer)

  def main(args: Array[String]): Unit = {
    val filePath = args(0)
    val templatesDirPath = args(1)
    val generatedDirPath = args(2)
    val driverJsonPath = args(3)
    val resultsPath = args(4)
    val generatedFile = generate(
      new File(filePath),
      new File(templatesDirPath),
      new File(generatedDirPath),
      Serialization.read[TypeMap](new File(driverJsonPath))
        .underlying
        .mapValues(manifestToType),
      Serialization.read[ValueMap](new File(driverJsonPath))
        .underlying
    )
    write(
      file = new File(resultsPath),
      content = generatedFile.getAbsolutePath
    )
  }
}

object TemplateEvaluator {

  private val logger = LoggerFactory.getLogger(getClass)

  private val classLoader = getClass.getClassLoader

  private val mirror = runtimeMirror(classLoader)

  def manifestToType(manifest: Manifest[_]): Type = {
    val symbol = mirror.classSymbol(manifest.runtimeClass)
    if (manifest.typeArguments.isEmpty) {
      symbol.toType
    } else {
      mirror.universe.appliedType(
        symbol.toTypeConstructor,
        manifest.typeArguments.map(manifestToType)
      )
    }
  }

  def generate(
    template: File,
    source: File,
    target: File,
    suppliedTypesByName: Map[String, Type],
    suppliedValuesByName: Map[String, Any]
  ): File = {
    val templateClass = getTemplateClass(source, template)
    val moduleSymbol = mirror.moduleSymbol(templateClass)
    val moduleInstance = mirror.reflectModule(moduleSymbol).instance
    val classSymbol = mirror.classSymbol(templateClass)
    val methodName = reflect.runtime.universe.newTermName("apply")
    val method = classSymbol.toType.member(methodName).asMethod
    val methodMirror = mirror.reflect(moduleInstance).reflectMethod(method)
    val methodParamSymbols = method.paramss.flatten
    val methodParamTermSymbols = method.paramss.flatten.map(_.asTerm).toSet
    val defaultValuesByName: Map[String, Any] = {
      val defaultedMethods = classSymbol.toType.members.filter(_.name.decoded.startsWith("apply$default$"))
      val numDefaultParams = defaultedMethods.size
      val params = method.paramss.flatten
      method.paramss.flatten.zipWithIndex.drop(params.size - numDefaultParams).map {
        case (param, i) => (param, i + 1)
      }.map {
        case (param, i) =>
          val defaultMethodName = reflect.runtime.universe.newTermName("apply$default$" + i)
          val defaultMethodSymbol = classSymbol.toType.member(defaultMethodName)
          val defaultMethod = defaultMethodSymbol.asMethod
          val defaultMethodMirror = mirror.reflect(moduleInstance).reflectMethod(defaultMethod)
          (param.name.decoded, defaultMethodMirror.apply())
      }(scala.collection.breakOut)
    }
    if (paramsConform(methodParamTermSymbols, suppliedTypesByName)) {
      logger.debug(s"Supplied params fully satisfies method requirements")
      val evaluatedTemplate = new File(target, removeTemplateExtension(getRelativePath(source, template).toString))
      val methodArgs = methodParamSymbols.flatMap { s =>
        suppliedValuesByName.get(s.name.decoded) orElse
        defaultValuesByName.get(s.name.decoded)
      }
      val content = methodMirror(methodArgs: _*)
      if (content.isInstanceOf[BufferedContent[_]]) {
        val stringContent = content.asInstanceOf[BufferedContent[_]].body
        val prettyContent = stringContent.trim
        write(evaluatedTemplate, prettyContent)
      } else {
        write(evaluatedTemplate, content.toString)
      }
      evaluatedTemplate
    } else {
      sys.error(s"Supplied params ${suppliedTypesByName} does not satisfy method requirements $methodParamTermSymbols")
    }
  }

  @inline
  def getRelativePath(templatesDir: File, template: File): Path = {
    val templatesPath = templatesDir.toPath
    val templatePath = template.toPath
    templatesPath.relativize(templatePath)
  }

  /* /example/template.scala.ext -> example.ext.template$ */
  @inline
  def getTemplateClass(templatesDir: File, template: File): Class[_] = {
    val relativePath = getRelativePath(templatesDir, template)
    val relativeParentPath = relativePath.getParent
    val namespace = Option(relativeParentPath).map(_.toString.replace("/", "."))
    val unqualifiedClassName = s"${removeTemplateExtension(template.getName).split('.').reverse.mkString(".")}$$"
    val qualifiedClassName = namespace.fold(unqualifiedClassName)(_ + "." + unqualifiedClassName)
    logger.debug(s"Loading class $qualifiedClassName")
    classLoader.loadClass(qualifiedClassName)
  }

  @inline
  def removeTemplateExtension(path: String): String = {
    path.replace(".scala", "")
  }

  /** Do the supplied types (weakly) conform to the driver instance's required members? */
  private def paramsConform(requiredTermSymbols: Set[TermSymbol], suppliedTypesByName: Map[String, Type]): Boolean = {
    val requiredNamesWithDefaultValue = requiredTermSymbols.filter(_.isParamWithDefault).map(_.name.decoded)
    val requiredTypesByName = requiredTermSymbols.map { symbol =>
      symbol.name.decoded -> symbol.typeSignature
    }(scala.collection.breakOut)
    val failures = requiredTypesByName.filterNot { case (name, typeSignature) =>
      if (suppliedTypesByName.contains(name)) {
        val suppliedType = suppliedTypesByName(name)
        if ((suppliedType weak_<:< typeSignature) || (suppliedType.erasure weak_<:< typeSignature.erasure)) {
          true
        } else {
          logger.warn(s"$name is expected to be compatible with type $typeSignature")
          false
        }
      } else {
        requiredNamesWithDefaultValue.contains(name)
      }
    }
    failures.isEmpty
  }
}
