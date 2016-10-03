package com.tapad.util

import scala.util.parsing.combinator.RegexParsers

object ArgsParser extends RegexParsers {

  override def skipWhitespace = false

  private val EOL = """\z""".r.withFailureMessage("end-of-input expected")

  private val singleQuotedStringLiteral = ("'" + """([^'\p{Cntrl}\\]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""" + "'").r

  private val doubleQuotedStringLiteral = ("\"" + """([^"\p{Cntrl}\\]|\\[\\'"bfnrt]|\\u[a-fA-F0-9]{4})*""" + "\"").r

  private val singleQuotedStringValue = singleQuotedStringLiteral.map(_.replaceAll("^'(.*)'$", "$1").replace("\\'", "'"))

  private val doubleQuotedStringValue = doubleQuotedStringLiteral.map(_.replaceAll("^\"(.*)\"$", "$1").replace("\\\"", "\""))

  private val unquotedStringValue = excludingCharClass(" ", description = Some("whitespace"))

  private val keyPrefix = "--"

  private val key = (keyPrefix ~> unquotedStringValue).withFailureMessage(s"argument name prefixed with '$keyPrefix' expected")

  private val value = (singleQuotedStringValue | doubleQuotedStringValue | unquotedStringValue) ^? {
    case value if !value.startsWith(keyPrefix) => value
  }

  private val arg = key ~ opt(whiteSpace ~> value) ^^ {
    case key ~ None => key -> Seq.empty[String]
    case key ~ Some(value) => key -> Seq(value)
  }

  val parser = repsep(arg, whiteSpace) <~ EOL

  def apply(input: String): Args = {
    parse(parser, input) match {
      case Success(args, _) =>
        val underlying = args.groupBy(_._1).collect { case (key, values) => key -> values.flatMap(_._2) }
        new Args(underlying)
      case NoSuccess(msg, _) =>
        throw new RuntimeException(s"Can not parse CLI args from '$input': $msg")
    }
  }

  private def excludingCharClass(charClass: String, description: Option[String] = None): Parser[String] = {
    ("""([^""" + charClass + """])*""").r.withFailureMessage(description.getOrElse(s"[$charClass]") + " not expected")
  }
}
