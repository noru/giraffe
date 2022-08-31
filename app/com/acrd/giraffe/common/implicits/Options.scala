package com.acrd.giraffe.common.implicits

import java.sql.Timestamp

import scala.language.implicitConversions

object Options {

  implicit def strToOption(s: String): Option[String] = Some(s)

  implicit def boolToOption(b: Boolean): Option[Boolean] = Some(b)

  implicit def intToOption(i: Int): Option[Int] = Some(i)

  implicit def longToOption(l: Long): Option[Long] = Some(l)

  implicit def optionToLang(o: Option[Long]): Long = o.getOrElse(0)

  implicit def throwableToOption(t: Throwable): Option[Throwable] = Some(t)

  implicit def timestampToOption(t: Timestamp): Option[Timestamp] = Some(t)

}
