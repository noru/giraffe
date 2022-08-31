package com.acrd.giraffe.dao

import com.acrd.giraffe.models.{AppBody, PayloadBody, AppStoreTables}
import com.acrd.giraffe.models.gen.Models
import Models._
import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.google.inject.{Inject, Singleton}
import com.acrd.giraffe.common.exceptions._
import com.acrd.giraffe.common.utils.Filter
import AppStoreTables.PayloadStatus._
import AppStoreTables.Platform._
import AppStoreTables.{BaseEnum, PayloadStatus}
import play.api.i18n.Lang
import play.api.libs.json._
import scala.concurrent.{ExecutionContext => EC, Future}
import scala.language.implicitConversions
import scala.util._
import com.acrd.giraffe.common.Consts._

/**
 * Companion object for AppDAO, hold static types, values, methods and all.
 */
object AppDAO{

  type DBResult = (Seq[AppRow], Seq[RatingRow], Seq[FacetRow], Seq[PricePlanRow], Seq[PayloadRow], Seq[AttachmentRow])
  val EmptyComponent = (Seq.empty[RatingRow], Seq.empty[FacetRow], Seq.empty[PricePlanRow], Seq.empty[PayloadRow], Seq.empty[AttachmentRow])

  def construct(apps: DBResult) = {

    val headers = apps._1
    val rating = apps._2.map(r => r.appId -> r)(collection.breakOut).toMap
    val facets = apps._3.groupBy(_.id)
    val prices = apps._4.groupBy(_.appId)
    val attachments = apps._6.groupBy(_.setId.getOrElse(DummyId))
    val payloads = apps._5.map(p => (p, attachments.get(p.attachmentSetId))).groupBy { case p => p._1.id }

    headers.map{ header => {
      val id = header.id
      (header, rating.get(id), facets.getOrElse(id, Seq.empty), prices.getOrElse(id, Seq.empty), payloads.getOrElse(id, Seq.empty))
    }}

  }

  /** Take a string as input, to validate if it can be parsed as AppBody correctly
    */
  def validateBody[T](str: String)(implicit reader: Reads[T]): Try[T] = {

    Json.parse(str).validate[T] match {
      case s: JsSuccess[T] => Success(s.get)
      case e: JsError => Failure(new InvalidFormatException(e.toString))
    }

  }

  def findPayloadWithLangOs(lang: Lang, os: Platform, payloads: Seq[PayloadBody]) = payloads.find(p => p.lang == lang && p.os == os)

  object SortOption extends BaseEnum {
    type SortOption = Value
    val CreateAt = Value("createAt")
    val UpdateAt = Value("updateAt")
    val AverageRating = Value("averageRating")
    // add other sort option if needed
    implicit def strToSortOption(str: String): SortOption = strToEnum(str)
  }

}

@Singleton
class AppDAO @Inject()(val context: BaseContext,
                       val previewAppDAO: PreviewAppDAO,
                       val liveAppDAO: LiveAppDAO,
                       val historyAppDAO: HistoryAppDAO) extends BaseDAO(context) {

  import AppDAO._
  import SortOption._
  import tables.profile.api._
  import liveAppDAO.{Actions => AAct}
  import previewAppDAO.{Actions => PAct}
  import historyAppDAO.{Actions => HAct}

  /** Get App content by its ID
   */
  def getById(id: Long, live: Boolean = true)(implicit ed: EC): Future[Option[AppBody]] = {
    if (live) liveAppDAO.get(id) else previewAppDAO.get(id)
  }

  /** Dynamic query of Apps, with paging and sort(by updateAt/createAt) support
   */
  def get(skip: Int,
          top: Int,
          orderBy: SortOption,
          ascending: Boolean,
          filter: Seq[Filter],
          live: Boolean = true)(implicit ec: EC) = {
    if (live) liveAppDAO.get(skip, top, orderBy, ascending, filter)
    else previewAppDAO.get(skip, top, orderBy, ascending, filter)
  }

  def getPayloadHistory(id: Long)(implicit eC: EC) = {
    import com.acrd.giraffe.common.utils.Util.orderedGroupBy
    historyAppDAO.getPayloadHistory(id).map( result =>
      orderedGroupBy(result)(p => (p.lang, p.os))
    )
  }

  /** Special requirement for installer version check
    */
  def getByCode(code: String)(implicit ec: EC) = liveAppDAO.getByCode(code)

  /**
    * Delete an App(Preview) by its ID
    * Note: in play we delete only the AppRow, other entities(icon, prices, payloads..) are delete by
    * foreign key restrictions
    */
  def delete(id: Long)(implicit ec: EC):Future[Try[Int]] = previewAppDAO.delete(id)


  def getAppHeader(id: Long)(implicit ec: EC) = previewAppDAO.get(id)

  def createAppHeader(app: AppBody)(implicit ec: EC) = previewAppDAO.createAppHeader(app)

  def updateAppHeader(app: AppBody)(implicit ec: EC) = previewAppDAO.updateAppHeader(app)

  def getPayload(id: Long, lang: Lang, os: Platform, live: Boolean = true)(implicit ec: EC) = {
    if (live) liveAppDAO.getPayload(id, lang, os) else previewAppDAO.getPayload(id, lang, os)
  }

  def createPayload(payload: PayloadBody)(implicit ec: EC): Future[Try[PayloadBody]] = previewAppDAO.createPayload(payload)

  def updatePayload(payload: PayloadBody)(implicit ec: EC): Future[Try[PayloadBody]] = previewAppDAO.updatePayload(payload)

  def deletePayload(id: Long, lang: Lang, os: Platform)(implicit ec: EC): Future[Try[Unit]] = {
    previewAppDAO.deletePayload(id, lang, os)
  }

  def publish(id: Long, lang: Lang, os: Platform)(implicit ec: EC): Future[Try[Unit]] = {

    findPayload(id, lang, os).map {
      case (Some(a), Some(p), _) =>
        val pl = p.copy(reviewStatus = Some(PayloadStatus.Live))
        for {
          header <- AAct.upsert(a)
           _ <- AAct.upsertPayload(pl)
           _ <- PAct.payloadUpdateStatus(id, lang, os, PayloadStatus.Live)
           _ <- HAct.insert(a.copy(payloads = Some(Seq(pl))))
        } yield ()
      case _ => throw new PayloadIdNotExistsException(id, lang, os)
    }.flatMap(q => db.run(q.transactionally)).map(_ => Success(())).recover { case e => Failure(e) }

  }

  def unpublish(id: Long, lang: Lang, os: Platform)(implicit ec: EC): Future[Try[Unit]] = {

    findPayload(id, lang, os, live = true).map {
      case (_, Some(p), hasSibling) => DBIO.seq(
        PAct.markUnpublishPayload(id, lang, os),
        if (hasSibling) AAct.deletePayload(p.toPayloadRowAndAttachmentRow._1) else AAct.deleteById(id)
      )
      case _ => throw new PayloadIdNotExistsException(id, lang, os)
    }.flatMap(q => db.run(q.transactionally)).map(_ => Success(())).recover { case e => Failure(e) }

  }

  def updateSubStatus(status: PayloadStatus)(id: Long, lang: Lang, os: Platform)(implicit ec: EC): Future[Try[Unit]] = {

    findPayload(id, lang, os).map {
      case (_, Some(p), _) => PAct.payloadUpdateStatus(id, lang, os, status)
      case _ => throw new PayloadIdNotExistsException(id, lang, os)
    }.flatMap(db.run(_)).map(_ => Success(())).recover { case e => Failure(e) }

  }

  def findPayload(id: Long, lang: Lang, os: Platform, live: Boolean = false)(implicit ec: EC) = {
    val app = if (live) liveAppDAO.get(id) else previewAppDAO.get(id)

    app.map {
      case a if a.isDefined => {
        val payloads = a.get.payloads.getOrElse(Seq.empty)
        val specificPayload = findPayloadWithLangOs(lang, os, payloads)
        if (specificPayload.isEmpty) throw new PayloadIdNotExistsException(id, lang, os)
        val hasSibling = payloads.size > 1

        (a, specificPayload, hasSibling)
      }
      case _ => throw new IdNotExistsException(id)
    }
  }

}

