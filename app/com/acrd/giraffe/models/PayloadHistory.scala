package com.acrd.giraffe.models

import com.acrd.giraffe.models.AppStoreTables.Platform.Platform
import com.acrd.giraffe.models.gen.Models.PayloadRow
import play.api.i18n.Lang
import play.api.libs.json.Json
import com.acrd.giraffe.common.implicits.JsonFormats.langFormat

case class PayloadHistory(lang: Lang, os: Platform, payloads: Seq[PayloadRow])

object PayloadHistory {
  implicit val format = Json.format[PayloadHistory]
}