package com.acrd.giraffe.models

import com.acrd.giraffe.models.gen.Models.CommentRow
import play.api.libs.json._

case class CommentSeqBody(count: Int, comments: Seq[CommentRow])

object CommentSeqBody {
  implicit val formatter = Json.format[CommentSeqBody]
}