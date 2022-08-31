package com.acrd.giraffe.common

import com.acrd.giraffe.models.{ActionLogBody, AppBody, PayloadBody}
import com.acrd.giraffe.models.gen.Models
import Models.CommentRow
import utils.IdGenerator
import com.acrd.giraffe.common.implicits.Timestamps._
import IdGenerator.getUID
import scala.annotation.implicitNotFound
import com.acrd.giraffe.common.implicits.Options._
import Consts.DummyId
import scala.language.implicitConversions



object registers {

  implicit class Register[T](o: T) {

    // implicit version
    def registered(implicit stamper: Stamper[T]): T = stamper.stamp(o)

    // explicit and shorthand version
    def registeredBy(stamper: Stamper[T]) = registered(stamper)
    val & = registeredBy _

  }

  class Stamper[T](val stamp: T ⇒ T)

  implicit val CreateCommentStamper = new Stamper[CommentRow]({ comment ⇒
      val timestamp = now
      comment.copy(id = getUID, createAt = timestamp, updateAt = timestamp)
  })
  implicit val UpdateCommentStamper = new Stamper[CommentRow]({ comment ⇒ comment.copy(updateAt = now) })

  implicit val CreatePayloadStamper = new Stamper[PayloadBody]({ payload ⇒
    val timestamp = now
    val mediaId = if (payload.attachmentSetId.exists(_.nonEmpty)) getUID else DummyId
    payload.copy(createAt = timestamp, updateAt = timestamp,
      attachmentSetId = mediaId,
      attachments = payload.attachments.map(_.map(_.copy(id = getUID, setId = mediaId))))

  })
  implicit val UpdatePayloadStamper = new Stamper[PayloadBody]({ payload ⇒
      // set updateAt, create ID for every media
      import com.acrd.giraffe.models.AppStoreTables.PayloadStatus._
      // do not allow modify to Live/Unpublish status
      val status = if (payload.reviewStatus.contains(Live) || payload.reviewStatus.contains(Unpublished)) Some(Draft)
                   else payload.reviewStatus
      val setId = if (payload.attachments.getOrElse(Seq.empty).isEmpty) DummyId else getUID
      payload.copy(updateAt = now,
        reviewStatus = status,
        attachmentSetId = setId,
        attachments = payload.attachments.map(_.map(_.copy(id = getUID, setId = setId))))
  })

  implicit val CreateAppHeaderStamper = new Stamper[AppBody]({ app ⇒

      val timestamp = now
      val appId = getUID
      val prices = app.pricePlans.map(_.copy(id = getUID, appId = appId, createAt = timestamp))
      app.copy(id = appId,
        createAt = timestamp,
        updateAt = timestamp,
        pricePlans = prices, payloads = None)

  })
  implicit val UpdateAppHeaderStamper = new Stamper[AppBody]({ app ⇒

    app.copy(updateAt = now, pricePlans = app.pricePlans.map(_.copy(id = getUID)), payloads = None)

  })

  implicit val CreateHistoryRecordStamper = new Stamper[AppBody]({ app ⇒
      val timestamp = now
      app.copy(createAt = timestamp, updateAt = timestamp,
        pricePlans = app.pricePlans.map(_.copy(createAt = timestamp)),
        payloads = app.payloads.map(_.map(_.copy(createAt = timestamp, updateAt = timestamp))))
  })


  implicit def CreateActionLogStamper(id: Long = DummyId): Stamper[ActionLogBody] = new Stamper[ActionLogBody]({ log ⇒

    log.copy(id = getUID, timestamp = now, parentId = if (id == DummyId) log.parentId else id)
  })

  @deprecated("Data Migration is done, it should not be used!", "1.0")
  @implicitNotFound("(appId, attachmentSetId)")
  implicit def migrationCreatePayloadBodyStamper(implicit e: (Long, Long)) = {
    val func: PayloadBody ⇒ PayloadBody = e match {
      case (appId, mediaSetId) ⇒ { payload ⇒
        val mediaSet = payload.attachments.map(_.map(_.copy(id = getUID, setId = mediaSetId)))
        payload.copy(id = appId, attachmentSetId = mediaSetId, attachments = mediaSet)
      }
      case _ ⇒ payload ⇒ payload
    }
    new Stamper[PayloadBody](func)
  }
  @deprecated("Data Migration is done, it should not be used!", "1.0")
  implicit val migrationCreateAppStamper = new Stamper[AppBody]({ app ⇒

      val appId = app.id.get
      val prices = app.pricePlans.map(_.copy(id = getUID, appId = appId))
      val payloads = app.payloads.map(_.map( p ⇒ {
        val attachmentSetId = if (p.attachments.isEmpty || p.attachments.get.isEmpty) DummyId else getUID
        val id = (appId, attachmentSetId)
        p & migrationCreatePayloadBodyStamper(id)
      }))
      app.copy(pricePlans = prices, payloads = payloads)

  })
  @deprecated("Data Migration is done, it should not be used!", "1.0")
  implicit val migrationCreateLiveAppStamper = new Stamper[AppBody]({ app ⇒

    val appId = app.id.get
    val prices = app.pricePlans.map(_.copy(id = getUID, appId = appId))
    val payloads = app.payloads.map(_.groupBy(p ⇒ (p.lang, p.os)).map{ case (key, p) ⇒ {
      val attachmentSetId = if (p.head.attachments.isEmpty || p.head.attachments.get.isEmpty) DummyId else getUID
      val id = (appId, attachmentSetId)
      p.map(_ & migrationCreatePayloadBodyStamper(id))
    }}.flatMap(p ⇒ p).toSeq)

    app.copy(pricePlans = prices, payloads = payloads)

  })
  
  @deprecated("Data Migration is done, it should not be used!", "1.0")
  implicit val migrationUpdateAppStamper = new Stamper[AppBody]({ app ⇒

      val appId = app.id.get
      val attachmentSetId = getUID
      val prices = app.pricePlans.map(p ⇒ p.copy(id = if(p.id.contains(0)) getUID else p.id, appId = appId))
      val payloads = app.payloads.map(_.map(p ⇒
          p.copy(attachments = p.attachments.map(_.map(_.copy(id = getUID, setId = attachmentSetId))), attachmentSetId = attachmentSetId)))

      app.copy(pricePlans = prices, payloads = payloads)

  })
  


}




