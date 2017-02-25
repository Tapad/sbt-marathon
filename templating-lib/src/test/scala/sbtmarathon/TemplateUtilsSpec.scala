package sbtmarathon

import java.io.File
import scala.io.Source
import org.scalatest.{FlatSpec, Matchers}

class TempalteUtilsTest extends FlatSpec with Matchers {

  import TemplateUtils._

  behavior of "TemplateUtils.path"

  it should "construct paths given part paths without file separators" in {
    val a = "foo"
    val b = "bar"
    path(a, b) shouldBe "foo/bar"
  }

  it should "construct paths given part paths with starting file separators" in {
    val a = "foo"
    val b = "/bar"
    path(a, b) shouldBe "foo/bar"
  }

  it should "construct paths given part paths with ending file separators" in {
    val a = "foo/"
    val b = "bar"
    path(a, b) shouldBe "foo/bar"
  }

  it should "construct paths given part paths with starting and ending file separators" in {
    val a = "foo/"
    val b = "/bar"
    path(a, b) shouldBe "foo/bar"
  }

  behavior of "TemplateUtils.valueOrErr"

  it should "return the underlying value when given Some value" in {
    valueOrErr(Some("foo"), errMsg = "Test failed") shouldBe "foo"
  }

  it should "throw an error when given None" in {
    intercept[Exception] {
      valueOrErr(None, errMsg = "Test succeeded")
    }
  }

  behavior of "TemplateUtils.write"

  it should "write the given contents to the given file" in {
    val file = File.createTempFile("TemplateUtilsSpec-", ".txt")
    val content = "foo bar baz"
    try {
      write(file, content)
      readFileAsString(file) shouldBe content
    } finally {
      file.delete()
    }
  }

  it should "append the given contents to the given file" in {
    val file = File.createTempFile("TemplateUtilsSpec-", ".txt")
    try {
      write(file, "foo")
      readFileAsString(file) shouldBe "foo"
      write(file, "/bar/baz", append = true)
      readFileAsString(file) shouldBe "foo/bar/baz"
    } finally {
      file.delete()
    }
  }

  def readFileAsString(file: File): String = {
    Source.fromFile(file).getLines.mkString("\n")
  }
}
