package com.acrd.giraffe.models

import java.sql.Timestamp
import com.acrd.giraffe.models.gen.Models.{AttachmentRow, PayloadRow}
import AppStoreTables.PayloadStatus._
import AppStoreTables.Platform._
import play.api.i18n.Lang

case class PayloadBody(id: Option[Long],
                       lang: Lang,
                       os: Platform,
                       createAt: Option[Timestamp],
                       updateAt: Option[Timestamp],
                       productCode: String,
                       upgradeCode: String,
                       reviewStatus: Option[PayloadStatus] = Some("draft"),
                       url: String,
                       size: Option[Int] = None,
                       mime: Option[String] = None,
                       attachmentSetId: Option[Long] = None,
                       attachments: Option[Seq[AttachmentRow]],
                       version: String,
                       title: String,
                       shortDescription: String,
                       description: String,
                       versionDescription: String,
                       instruction: String,
                       installation: String,
                       additionalInfo: String,
                       knownIssue: String,
                       supportInfo: String) {
  def toPayloadRowAndAttachmentRow = PayloadBody.toPayloadRowAndAttachmentRow(this)

}

object PayloadBody {

  import com.acrd.giraffe.common.implicits.Options._
  import com.acrd.giraffe.common.implicits.JsonFormats._
  import org.cvogt.play.json.Jsonx
  import org.cvogt.play.json.implicits.optionWithNull

  implicit val formatter = Jsonx.formatCaseClass[PayloadBody]

  def toPayloadRowAndAttachmentRow(p: PayloadBody) = {
    val payload = PayloadRow(
      id = p.id,
      lang = p.lang,
      os = p.os,
      createAt = p.createAt.orNull,
      updateAt = p.updateAt.orNull,
      reviewStatus = p.reviewStatus,
      attachmentSetId = p.attachmentSetId,
      url = p.url,
      size = p.size,
      mime = p.mime,
      title = p.title,
      shortDescription = p.shortDescription,
      description = p.description,
      installation = p.installation,
      instruction = p.instruction,
      additionalInfo = p.additionalInfo,
      knownIssue = p.knownIssue,
      supportInfo = p.supportInfo,
      productCode = p.productCode,
      upgradeCode = p.upgradeCode,
      version = p.version,
      versionDescription = p.versionDescription
    )
    val attachments = p.attachments.map(_.map(_.copy(setId = payload.attachmentSetId, appId = p.id)))

    (payload, attachments)
  }
  def fromPayloadRowAndMediaRow(p: PayloadRow, m: Seq[AttachmentRow] = Seq.empty) = {
    PayloadBody(
      id = p.id,
      lang = p.lang,
      os = p.os,
      createAt = p.createAt,
      updateAt = p.updateAt,
      productCode = p.productCode,
      upgradeCode = p.upgradeCode,
      reviewStatus = p.reviewStatus,
      url = p.url,
      size = p.size,
      mime = p.mime,
      attachmentSetId = p.attachmentSetId,
      attachments = Option(m),
      title = p.title,
      shortDescription = p.shortDescription,
      description = p.description,
      version = p.version,
      versionDescription = p.versionDescription,
      instruction = p.instruction,
      installation = p.installation,
      additionalInfo = p.additionalInfo,
      knownIssue = p.knownIssue,
      supportInfo = p.supportInfo
    )
  }
}

