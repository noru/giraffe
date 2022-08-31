package com.acrd.giraffe.models

import com.acrd.giraffe.models.gen.Models.ActionLogRow
import play.api.libs.json.Json
import com.acrd.giraffe.common.implicits.Options._
import com.acrd.giraffe.common.Consts._
import com.acrd.giraffe.common.implicits.JsonFormats.timestampFormat

case class ActionLogBody(id: Option[Long],
                         timestamp: Option[java.sql.Timestamp],
                         action: Option[String] = None,
                         fromState: Option[String] = None,
                         toState: Option[String] = None,
                         msg1: Option[String] = None,
                         msg2: Option[String] = None,
                         parentId: Option[Long],
                         userId: Option[String] = None,
                         `type`: Option[String] = None) {

  def toActionLogRow = ActionLogBody.toActionLogRow(this)
}


object ActionLogBody {

  implicit val formatter = Json.format[ActionLogBody]

  def fromActionLogRow(row: ActionLogRow) = {
    ActionLogBody(
      id = row.id,
      timestamp = row.timestamp,
      action = row.action,
      fromState = row.fromState,
      toState = row.toState,
      msg1 = row.msg1,
      msg2 = row.msg2,
      parentId = row.parentId,
      userId = row.userId,
      `type` = row.`type`
    )
  }

  def toActionLogRow(body: ActionLogBody) = {
    ActionLogRow(
      id = body.id.getOrElse(DummyId),
      timestamp = body.timestamp.orNull,
      action = body.action,
      fromState = body.fromState,
      toState = body.toState,
      msg1 = body.msg1,
      msg2 = body.msg2,
      parentId = body.parentId,
      userId = body.userId,
      `type` = body.`type`
    )
  }

}