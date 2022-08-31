package com.acrd.giraffe.services

import play.api.i18n.Lang
import scala.concurrent.Future

trait TextService {

  def get(id: String)(implicit lang: Lang): Future[Option[String]]
  def upsert(id: String, text: String)(implicit lang: Lang): Future[Int]
  def delete(id: String)(implicit lang: Lang): Future[Int]

}
object TextService {

  implicit def lang2Str(lang: Lang): String = lang.code
  implicit def str2Lang(str: String): Lang = Lang(str)

  val en = Lang("en")
  val cs = Lang("cs")
  val fr = Lang("fr")
  val ja = Lang("ja")
  val de = Lang("de")
  val hu = Lang("hu")
  val it = Lang("it")
  val ko = Lang("ko")
  val pl = Lang("pl")
  val pt = Lang("pt")
  val ru = Lang("ru")
  val zh_CN = Lang("zh", "CN")
  val zh_TW = Lang("zh", "TW")

  val default = en
}



