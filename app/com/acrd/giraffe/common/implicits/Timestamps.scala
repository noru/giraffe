package com.acrd.giraffe.common.implicits

import java.sql.Timestamp
import org.joda.time.format.ISODateTimeFormat

object Timestamps {
  import com.github.nscala_time.time.Imports._
  val fmt = ISODateTimeFormat.dateTime().withZoneUTC()

  implicit def date2Timestamp(date: DateTime): Timestamp = new Timestamp(date.getMillis)

  implicit def long2Timestamp(long: Long): Timestamp = new Timestamp(long)

  implicit def utcString2Timestamp(utc: String): Timestamp = new DateTime(utc).toDateTime

  implicit def timestamp2UtcString(t: Timestamp): String = fmt.print(t.getTime)

  implicit def now: Timestamp = date2Timestamp(DateTime.now)

}
