package com.acrd.giraffe.dao

import AppDAO.SortOption.SortOption
import AppDAO._
import com.acrd.giraffe.models.{AppBody, PayloadBody, AppStoreTables}
import com.acrd.giraffe.models.gen.Models._
import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.google.inject.{Inject, Singleton}
import com.acrd.giraffe.common.{registers, Consts}
import com.acrd.giraffe.common.registers._
import com.acrd.giraffe.common.exceptions.{IdNotExistsException, PayloadIdNotExistsException}
import AppStoreTables.PayloadStatus._
import AppStoreTables.Platform.Platform
import play.api.i18n.Lang
import scala.concurrent.{ExecutionContext => EC, Future}
import scala.language.{postfixOps, implicitConversions}
import scala.async.Async._
import scala.util._
import com.acrd.giraffe.common.utils.{Filter => F, SqlBuilder}

@Singleton
class PreviewAppDAO @Inject()(val context: BaseContext, val mediaDAO: MediaSetDAO) extends BaseDAO(context){

  import Actions._
  import tables._
  import tables.profile.api._

  def get(id: Long)(implicit ed: EC): Future[Option[AppBody]] = {
    queryByIdRun(id).map{ results ⇒ {
        val squashed = AppDAO.construct(results).headOption
        squashed.map(AppBody.fromTuple(_, live = false))
      }
    }.recover{ case e => error(s"Error when query app by Id: $id", e); None }
  }

  def get(skip: Int, top: Int, orderBy: SortOption, ascending: Boolean, filters: Seq[F])(implicit ec: EC) = {

    import com.acrd.giraffe.common.implicits.Options._
    val sb = new SqlBuilder()(false)
        .withParameters(skip, topLimit(top), Some(orderBy), ascending)
        .withFilters(filters: _*)

    val  result = for {
      count ← db.run(sql"#${sb.count}".as[Int].head)
      apps ← if (count > 0) db.run(sql"#${sb.q}".as[AppRow]) else Future(Seq.empty[AppRow])
      (rating, facets, prices, payloads, attachments) ← {
        val idSet = apps.map(_.id).toSet
        if (idSet.nonEmpty) queryAppComponents(idSet) else Future(EmptyComponent)
      }
    } yield (count, (apps, rating, facets, prices, payloads, attachments))

    result.map{case (count, rows) ⇒ {
      val apps = AppDAO.construct(rows).map(AppBody.fromTuple(_, live = false))
      (count, apps)
    }}
  }
  
  def delete(id: Long)(implicit ec: EC) = {
    db.run(Actions.deleteById(id)).map{
      case i => Success(i)}.recover{ case e => Failure(e)}
  }

  def createAppHeader(app: AppBody)(implicit ec: EC): Future[Try[AppBody]] = {

    val regApp = app & CreateAppHeaderStamper
    val (appRow, facets, prices, _) = regApp.toTuple

    db.run(insert(appRow, facets, prices))
        .map(_ ⇒ Success(regApp)).recover{ case e ⇒ Failure(e)}
  }

  def updateAppHeader(app: AppBody)(implicit ec: EC): Future[Try[AppBody]] = {

    var regApp = app & UpdateAppHeaderStamper

    val q = for {
      liveExists ← liveExists(app.id.getOrElse(Consts.DummyId)).result
      _ ← {
        if (liveExists) regApp = regApp.copy(id = app.id) // keep old id if there is a published version
        val (appRow, facets, prices, _) = regApp.toTuple
        update(appRow, facets, prices)
      }
    } yield ()

    db.run(q).map(_ ⇒ Success(regApp)).recover{ case e ⇒ Failure(e)}

  }

  def getPayload(id: Long, lang: Lang, os: Platform)(implicit ec: EC): Future[Option[PayloadBody]] = {
    db.run(payloadQueryByIdWithAttachments(id, lang, os).result).map{
      case result if result.nonEmpty ⇒ {
        val payloadRow = result.head._1
        val multiMediaRows = result.map(_._2).filter(_.isDefined).map(_.get).sortBy(_.order)
        val body = PayloadBody.fromPayloadRowAndMediaRow(payloadRow, multiMediaRows)
        Some(body)
      }
      case _ ⇒ None
    }
  }

  def createPayload(payload: PayloadBody)(implicit ec: EC): Future[Try[PayloadBody]] = {

    val appId = payload.id.get
    val regPayload = payload & CreatePayloadStamper
    val (pl, attach) = regPayload.toPayloadRowAndAttachmentRow

    val q = for {
      exists ← headerExists(appId).result
      _ ← if (exists) DummyDBIO else throw new IdNotExistsException(appId)
      _ ← if (attach.isDefined) mediaDAO.Actions.createSet(attach.get) else DBIO.successful(Consts.DummyId)
      _ ← insertPayload(pl)
    } yield ()

    db.run(q.transactionally).map(_ ⇒ Success(regPayload)).recover{ case e ⇒ Failure(e)}

  }

  def updatePayload(payload: PayloadBody)(implicit ec: EC): Future[Try[PayloadBody]] = {
    val regPayload = payload & UpdatePayloadStamper
    val (body, attachments) = regPayload.toPayloadRowAndAttachmentRow
    val q = for {
      _ ← Payload insertOrUpdate body
      _ ← if (payload.attachments.isDefined)
              // delete old set and add new ones
              DBIO.seq(
                Attachment.filter(_.setId === payload.attachmentSetId).delete,
                mediaDAO.Actions.createSet(attachments.get))
           else DummyDBIO
    } yield ()

    db.run(q.transactionally).map(_ ⇒ Success(regPayload)).recover{ case e ⇒ Failure(e)}

  }

  def updatePayloadStatus(id: Long,
                          lang: Lang,
                          os: Platform,
                          status: PayloadStatus)(implicit ec: EC): Future[Try[Unit]] = {

      db.run(payloadUpdateStatus(id, lang, os, status)).map( _ ⇒ Success(()) ).recover{ case e ⇒ Failure(e)}
  }

  def deletePayload(id: Long, lang: Lang, os: Platform)(implicit ec: EC) = {
    val q = for {
      p ← payloadQueryById(id, lang, os).result.headOption
      _ ← if (p.isDefined) DummyDBIO else throw new PayloadIdNotExistsException(id, lang, os)
      _ ← payloadQueryById(id, lang, os).delete
      _ ← mediaDAO.Actions.deleteSet(p.get.attachmentSetId)
    } yield ()
    db.run(q.transactionally).map{ _ ⇒ Success(())}.recover{ case e ⇒ Failure(e)}
  }

  object Actions {

    def insert(app: AppRow,
               facets: Seq[FacetRow],
               prices: Seq[PricePlanRow]) = ((App += app) andThen
                                             (Facet ++= facets) andThen
                                             (PricePlan ++= prices)).transactionally

    def update(app: AppRow,
               facets: Seq[FacetRow],
               prices: Seq[PricePlanRow]) = ((App insertOrUpdate app) andThen
                                            Facet.filter(_.id === app.id).delete andThen
                                            PricePlan.filter(_.appId === app.id).delete andThen
                                            (Facet ++= facets) andThen
                                            (PricePlan ++= prices)).transactionally

    def queryAppComponents(idSet: Set[Long])(implicit ec: EC) = {
      require(idSet.nonEmpty)
      import SqlBuilder.InSetQuery
      import com.acrd.giraffe.common.implicits.PlainSQLGetResult._

      val q = for {
        facets <- sql"#${InSetQuery("facet", "id", idSet)}".as[FacetRow]
        prices <- sql"#${InSetQuery("price_plan", "app_id", idSet)}".as[PricePlanRow]
        payloads <- sql"#${InSetQuery("payload", "id", idSet)}".as[PayloadRow]
        attachments <- sql"#${InSetQuery("attachment", "id", idSet)}".as[AttachmentRow]
      } yield (Seq.empty[RatingRow], facets, prices, payloads, attachments)
      db.run(q)

    }

    def queryByIdRun(id: Long)(implicit ec: EC) = {

      import com.acrd.giraffe.common.implicits.PlainSQLGetResult._
      async {
        val header = db.run(sql"SELECT * FROM app WHERE id = $id".as[AppRow])
        val facets = db.run(sql"SELECT * FROM facet WHERE id = $id".as[FacetRow])
        val prices = db.run(sql"SELECT * FROM price_plan WHERE app_id = $id".as[PricePlanRow])
        val payloads = db.run(sql"SELECT * FROM payload WHERE id = $id".as[PayloadRow])
        val attachments = db.run(sql"SELECT * FROM attachment WHERE app_id = $id".as[AttachmentRow])
        (await(header), Seq.empty[RatingRow], await(facets), await(prices), await(payloads), await(attachments))
      }
    }

    def deleteById(id: Long)(implicit ec: EC) = (for {
      i <- App.filter(_.id === id).delete
      _ <- Attachment.filter(_.appId === id).delete
      _ <- Facet.filter(_.id === id).delete
    } yield i).transactionally

    def insertPayload(payload: PayloadRow) = Payload += payload

    val headerExists = Compiled{ (id: ConstColumn[Long]) ⇒ App.filter(_.id === id).exists}
    val liveExists = Compiled{ (id: ConstColumn[Long]) => AppLive.filter(_.id === id).exists}

    val payloadQueryByIdWithAttachments = Compiled{
      (id: ConstColumn[Long], lang: ConstColumn[Lang], os: ConstColumn[Platform]) ⇒
      Payload.filter(p ⇒ p.id === id && p.lang === lang && p.os === os) joinLeft Attachment on (_.attachmentSetId === _.setId)
    }

    val payloadQueryById = Compiled{
      (id: ConstColumn[Long], lang: ConstColumn[Lang], os: ConstColumn[Platform]) ⇒
        Payload.filter(p ⇒ p.id === id && p.lang === lang && p.os === os)
    }

    val updatePayloadStatus = Compiled{
      (id: ConstColumn[Long], lang: ConstColumn[Lang], os: ConstColumn[Platform]) ⇒
        Payload.filter(p ⇒ p.id === id && p.lang === lang && p.os === os).map(p ⇒ (p.reviewStatus, p.updateAt))
    }
    def payloadUpdateStatus(id: Long, lang: Lang, os: Platform, status: PayloadStatus) =
      updatePayloadStatus(id, lang, os).update(Some(status), now)

    /** Gotcha: When unpublish an app, need to set a 'unpublish' flag on Previewed payload, however if the previewed
      * payload is updated ever since published, leave it as it is.
      * FIXME: consider it through!!!
      */
    val markUnpublishPayload = Compiled{
      (id: ConstColumn[Long], lang: ConstColumn[Lang], os: ConstColumn[Platform]) ⇒
        Payload.filter(p ⇒ p.id === id && p.lang === lang && p.os === os && p.reviewStatus === Live).map(p ⇒ (p.reviewStatus, p.updateAt))
    }
    def markUnpublishPayload(id: Long, lang: Lang, os: Platform) =
      updatePayloadStatus(id, lang, os).update(Some(Unpublished), now)

  }
}

object PreviewAppDAO{

  import slick.lifted._
  type A = AppStoreTables#App
  type AppPredict = A ⇒ Rep[Boolean]
  type AppPartialPredict = PartialFunction[A, Rep[Boolean]]

}

