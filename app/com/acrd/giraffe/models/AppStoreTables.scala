package com.acrd.giraffe.models

import com.acrd.giraffe.common.exceptions.InvalidEnumException
import com.acrd.giraffe.models.gen.Tables
import play.api.libs.json.Json._
import play.api.libs.json._
import scala.language.implicitConversions
import scala.util.{Success, Try}

trait AppStoreTables extends Tables {

}

object AppStoreTables {

  class BaseEnum extends Enumeration {

    protected def strToEnum[T <: Value](str: String): T = Try(this.withName(str).asInstanceOf[T]) match {
      case Success(s) => s
      case _ => throw new InvalidEnumException(str, this.values.toSeq)
    }

    def enumReads[A <: Value] = new Reads[A] {
      override def reads(json: JsValue): JsResult[A] = Json.fromJson[String](json).map(strToEnum[A])
    }
    def enumWrites[A <: Value] = new Writes[A] {
      override def writes(o: A): JsValue = toJson(o.toString)
    }
  }

  object CommentStatus extends BaseEnum {

    type CommentStatus = Value
    val Live = Value("live")
    val Preview = Value("preview")
    val Deleted = Value("deleted")
    val Unpublished = Value("unpublished")
    val Rejected = Value("rejected")
    val PendingAction = Value("pending_action")
    val PendingReview = Value("pending_review")
    implicit def strToCommentStatus(str: String): CommentStatus = strToEnum(str)
    implicit val commentStatusReads = enumReads[CommentStatus]
    implicit val commentStatusWrites = enumWrites[CommentStatus]
  }

  object PayloadStatus extends BaseEnum {

    type PayloadStatus = Value
    val Draft = Value("draft")
    val Submission = Value("submission")
    val Resubmission = Value("resubmission")
    val UnderReview = Value("under_review")
    val PendingApproval = Value("pending_approval")
    val PendingResubmission = Value("pending_resubmission")
    val PendingResubmissionUpdated = Value("pending_resubmission_updated")
    val Live = Value("live")
    val Unpublished = Value("unpublished")

    implicit def strToPayloadStatus(str: String): PayloadStatus = strToEnum(str)
    implicit val payloadStatusReads = enumReads[PayloadStatus]
    implicit val payloadStatusWrites = enumWrites[PayloadStatus]

  }

  object Platform extends BaseEnum {

    type Platform = Value
    val Web = Value("web")
    val Mac = Value("mac")
    val Win32 = Value("win32")
    val Win64 = Value("win64")
    val Win32_64 = Value("win32_64")
    val linux = Value("linux")

    implicit def strToPlatform(str: String): Platform = strToEnum(str)
    implicit val platformReads = enumReads[Platform]
    implicit val platformWrites = enumWrites[Platform]
  }

}
