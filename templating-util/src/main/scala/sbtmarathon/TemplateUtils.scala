package sbtmarathon

import java.io._
import java.nio.file.Path
import java.nio.charset.Charset

object TemplateUtils {

  def path(parts: String*): String = {
    parts.reduceLeft { (a: String, b: String) =>
      if (a.endsWith(File.separator) && b.startsWith(File.separator)) {
        a + b.substring(1, b.length)
      } else if (a.endsWith(File.separator) || b.startsWith(File.separator)) {
        a + b
      } else {
        a + File.separator + b
      }
    }
  }

  def valueOrErr(optionalValue: Option[String], errMsg: => String): String = {
    optionalValue match {
      case Some(value) => value
      case None => sys.error(errMsg)
    }
  }

  def write(file: File, content: String, charset: Charset = Charset.forName("UTF-8"), append: Boolean = false): Unit = {
    if (charset.newEncoder.canEncode(content)) {
      try {
        val parent = file.getParentFile
        if (parent != null) {
          parent.mkdirs()
        }
      } catch {
        case e: IOException => sys.error(s"Could not create parent directories for $file")
      }
      val fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset))
      try {
        fileWriter.write(content)
      } catch {
        case e: IOException => sys.error(s"error writing to $file: ${e.getMessage}")
      } finally {
        fileWriter.close()
      }
    } else {
      sys.error("string cannot be encoded by charset " + charset.name)
    }
  }
}
