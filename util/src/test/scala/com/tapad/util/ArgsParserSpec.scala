package com.tapad.util

import org.scalatest.FlatSpec

class ArgsParserSpec extends FlatSpec {

  behavior of "ArgsParser"

  it should "parse binary args" in {
    val input = "--key value"
    val args = ArgsParser(input)
    assert(args.required("key") == "value")
  }

  it should "parse unary args" in {
    val input = "--key"
    val args = ArgsParser(input)
    assert(args.boolean("key"))
    intercept[IllegalArgumentException] {
      args.required("key")
    }
  }

  it should "parse unary and binary args in the same input string" in {
    val input = "--key1 --key2 value"
    val args = ArgsParser(input)
    assert(args.boolean("key1"))
    assert(args.required("key2") == "value")
  }

  it should "parse args with multiple associative arguments" in {
    val input = "--foo FOO --bar BAR --baz BAZ"
    val output = ArgsParser(input)
    assert(output.contains("foo"))
    assert(output.required("foo") == "FOO")
    assert(output.contains("bar"))
    assert(output.required("bar") == "BAR")
    assert(output.contains("baz"))
    assert(output.required("baz") == "BAZ")
  }

  it should "parse args with multiple non-associative arguments" in {
    val input = "--foo --bar --baz"
    val output = ArgsParser(input)
    assert(output.boolean("foo"))
    assert(output.boolean("bar"))
    assert(output.boolean("baz"))
  }

  it should "parse args with mixed-style arguments" in {
    val associativeArgFirst = "--foo FOO --bar"
    val associativeArgSecond = "--bar --foo FOO"
    val output1 = ArgsParser(associativeArgFirst)
    val output2 = ArgsParser(associativeArgSecond)
    assert(output1 == output2)
    assert(output1.contains("foo"))
    assert(output1.required("foo") == "FOO")
    assert(output1.contains("bar"))
  }

  it should "parse args that have string values enclosed in single quotes" in {
    val input = "--foo 'abc def ghi'"
    val output = ArgsParser(input)
    assert(output.contains("foo"))
    assert(output.required("foo") == "abc def ghi")
  }

  it should "parse args that have string values enclosed in double quotes" in {
    val input = "--foo \"abc def ghi\""
    val output = ArgsParser(input)
    assert(output.contains("foo"))
    assert(output.required("foo") == "abc def ghi")
  }

  it should "parse args that have string values enclosed in single quotes which contain escaped quotes" in {
    val input = "--foo '\\'twas bar'"
    val output = ArgsParser(input)
    assert(output.contains("foo"))
    assert(output.required("foo") == "'twas bar")
  }

  it should "parse args that have multiple string values enclosed in single quotes" in {
    val input = "--foo 'foo' --bar ''"
    val output = ArgsParser(input)
    assert(output.required("foo") == "foo")
    assert(output.required("bar") == "")
  }

  it should "parse args that have multiple string values enclosed in single quotes, interspersed with other values" in {
    val input = "--props classpath:application.properties --foo 'foo' --qux quxx --bar ''"
    val output = ArgsParser(input)
    assert(output.required("props") == "classpath:application.properties")
    assert(output.required("foo") == "foo")
    assert(output.required("qux") == "quxx")
    assert(output.required("bar") == "")
  }

  it should "parse args that have string values enclosed in double quotes which contain escaped quotes" in {
    val input = "--foo \"\\\"bar\\\"\""
    val output = ArgsParser(input)
    assert(output.contains("foo"))
    assert(output.required("foo") == "\"bar\"")
  }

  it should "deduplicate parsed args so that the last specified key 'wins'" in {
    val input = "--foo --foo bar"
    val output = ArgsParser(input)
    assert(output.contains("foo"))
    assert(output.required("foo") == "bar")
  }
}
