package com.tapad.util

import java.util.Properties

case class Args(underlying: Map[String, Seq[String]]) {

  def apply(key: String): String = {
    required(key)
  }

  def required(key: String): String = {
    val values = list(key)
    require(values.nonEmpty, s"Please provide a value for --$key")
    require(values.size == 1, s"Please only provide a single value for --$key")
    values.head
  }

  def getOrElse(key: String, default: => String): String = {
    if (underlying.contains(key)) {
      required(key)
    } else {
      default
    }
  }

  def contains(key: String): Boolean = {
    underlying.contains(key)
  }

  def boolean(key: String): Boolean = {
    underlying.contains(key) && underlying(key).isEmpty
  }

  def list(key: String): Seq[String] = {
    underlying.get(key).getOrElse(Seq.empty)
  }

  def toProperties: Properties = {
    val props = new Properties
    underlying.mapValues {
      case values if values.isEmpty => "true"
      case values => values.mkString(",")
    }.map {
      case (key, value) => props.setProperty(key, value)
    }
    props
  }
}

object Args {

  def apply(args: Array[String]): Args = {
    fromString(args.mkString(" "))
  }

  def fromString(args: String): Args = {
    ArgsParser(args)
  }
}
