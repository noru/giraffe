package com.acrd.giraffe.models

import java.sql.Timestamp
import com.acrd.giraffe.common.Consts
import com.acrd.giraffe.common.implicits.Timestamps._
import com.acrd.giraffe.common.implicits.JsonFormats._
import com.acrd.giraffe.models.gen.Models.CommentRow
import com.acrd.giraffe.services.TextService.en
import AppStoreTables.CommentStatus._
import play.api.i18n.Lang
import play.api.libs.json.Json._
import scala.language.implicitConversions

case class CommentBody(id: Option[Long] = None,
                       author: Option[String],
                       appId: Option[Long],
                       createAt: Option[Timestamp] = None,
                       updateAt: Option[Timestamp] = None,
                       lang: Option[Lang] = Some(en),
                       title: Option[String] = None,
                       text: Option[String] = None,
                       status: Option[CommentStatus] = None,
                       rating: Option[Int] = None,
                       replyTo: Option[Long] = None,
                       storeId: Option[String] = None,
                       isVerifiedDownload: Option[Boolean] = Some(false)
                      ){
  def toCommentRow = CommentBody.toCommentRow(this)
}

object CommentBody {

  implicit val commentBodyFormatter = format[CommentBody]

  implicit def toCommentRow(body: CommentBody): CommentRow =  {
    lazy val timestamp = now
    CommentRow(id = body.id.getOrElse(Consts.DummyId),
               appId = body.appId.getOrElse(Consts.DummyId),
               author = body.author.getOrElse(""),
               lang = body.lang,
               title = body.title,
               text = body.text,
               createAt = body.createAt.getOrElse(timestamp),
               updateAt = body.updateAt.getOrElse(timestamp),
               status = body.status.getOrElse(Preview),
               rating = body.rating.getOrElse(0),
               replyTo = body.replyTo,
               storeId = body.storeId,
               isVerifiedDownload = body.isVerifiedDownload)
  }

  implicit def fromCommentRow(row: CommentRow): CommentBody = {
    import com.acrd.giraffe.common.implicits.Options._
    CommentBody(
      id = row.id,
      author = row.author,
      appId = row.appId,
      createAt = row.createAt,
      updateAt = row.updateAt,
      lang = row.lang,
      title = row.title,
      text = row.text,
      status = Option(row.status),
      rating =  row.rating,
      replyTo = row.replyTo,
      storeId = row.storeId,
      isVerifiedDownload = row.isVerifiedDownload
    )
  }

  def isEqual(body: CommentBody, row: CommentRow): Boolean =
    body.author.contains(row.author) &&
    body.lang == row.lang &&
    body.title == row.title &&
    body.text == row.text &&
    body.status.getOrElse(Preview) == row.status &&
    body.rating.getOrElse(0) == row.rating &&
    body.replyTo == row.replyTo &&
    body.appId.contains(row.appId)
}
