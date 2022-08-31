package com.acrd.giraffe.common.implicits

import com.acrd.giraffe.models.AppStoreTables.PayloadStatus._
import com.acrd.giraffe.models.AppStoreTables.Platform._
import play.api.i18n.Lang
import slick.jdbc.{GetResult => GR}

object PlainSQLGetResult {

  implicit val GetResultLang: GR[Lang] = GR(r => {import r._; Lang(<<[String])})
  implicit val GetResultPlatform: GR[Platform] = GR(r => {import r._;strToPlatform(<<[String])})
  implicit val GetResultPS: GR[Option[PayloadStatus]] = GR(r => {import r._;Option(strToPayloadStatus(<<[String]))})

}
