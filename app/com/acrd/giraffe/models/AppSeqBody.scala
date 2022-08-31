package com.acrd.giraffe.models

import play.api.libs.json._

case class AppSeqBody(count: Int, skip: Option[Int], top: Option[Int], apps: Seq[AppBody])

object AppSeqBody {
  implicit val formatter = Json.format[AppSeqBody]
}