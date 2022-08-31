package com.acrd.giraffe.common.implicits

import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.i18n.Lang
import play.api.libs.json._

object JsonFormats {

  implicit def timestampFormat = new Format[Timestamp] {
    import Timestamps._
    override def writes(o: Timestamp): JsValue = Json.toJson(timestamp2UtcString(o))
    override def reads(json: JsValue): JsResult[Timestamp] = Json.fromJson[String](json).map(utcString2Timestamp)
  }

  implicit def langFormat = new Format[Lang] {
    override def writes(o: Lang): JsValue = Json.toJson(o.code)
    override def reads(json: JsValue): JsResult[Lang] = Json.fromJson[String](json).map(Lang.apply)
  }

}
