package com.acrd.giraffe.dao

import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.common.utils.{Filter, SqlBuilder}
import com.acrd.giraffe.dao.AppDAO.SortOption._
import com.acrd.giraffe.models.AppStoreTables.Platform.Platform
import com.acrd.giraffe.models.gen.Models._
import com.acrd.giraffe.models.{AppBody, PayloadBody}
import com.google.inject.{Inject, Singleton}
import play.api.i18n.Lang
import slick.dbio.Effect.Write
import scala.async.Async._
import scala.concurrent.{ExecutionContext => EC, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

@Singleton
class LiveAppDAO @Inject()(val context: BaseContext) extends BaseDAO(context){

  import Actions._
  import AppDAO._
  import tables._
  import tables.profile.api._

  def get(id: Long)(implicit ed: EC): Future[Option[AppBody]] = {

    queryByIdRun(id).map{ results => {
        val squashed = AppDAO.construct(results).headOption
        squashed.map(AppBody.fromTuple(_))
      }
    }.recover{ case e => error(s"Error when query app by Id: $id", e); None }
  }

  def get(skip: Int, top: Int, orderBy: SortOption, ascending: Boolean, filters: Seq[Filter])(implicit ec: EC) = {

    import com.acrd.giraffe.common.implicits.Options._
    val sb = new SqlBuilder()
        .withParameters(skip, topLimit(top), Some(orderBy), ascending)
        .withFilters(filters: _*)

    val  result = for {
      count <- db.run(sql"#${sb.count}".as[Int].head)
      apps <- if (count > 0) db.run(sql"#${sb.q}".as[AppRow]) else Future(Seq.empty[AppRow])
      (ratings, facets, prices, payloads, attachments) <- {
        val idSet = apps.map(_.id).toSet
        if (idSet.nonEmpty) queryAppComponents(idSet) else Future(EmptyComponent)
      }
    } yield (count, (apps, ratings, facets, prices, payloads, attachments))

    result.map{case (count, rows) => {
      val apps = AppDAO.construct(rows).map(AppBody.fromTuple(_))
      (count, apps)
    }}
  }

  def getByCode(code: String)(implicit ed: EC) = {
    for {
      id <- db.run(payloadQueryByCode(code).result.headOption)
      app <- if (id.isDefined) get(id.get) else throw new IdNotExistsException(code)
    } yield app
  }
  
  def delete(id: Long)(implicit ec: EC) = {
    db.run(Actions.deleteById(id).transactionally).map{ case i => Success(i)}.recover{ case e => Failure(e)}
  }

  def createAppHeader(app: AppRow,
                      facets: Seq[FacetRow],
                      prices: Seq[PricePlanRow])(implicit ec: EC): Future[Try[Unit]] ={

    val q = insert(app, prices)
    db.run(q).map( _ => Success(()) ).recover{ case e => Failure(e)}

  }

  def getPayload(id: Long, lang: Lang, os: Platform)(implicit ec: EC): Future[Option[PayloadBody]] = {
    db.run(payloadQueryByIdWithAttachments(id, lang, os).result).map{
      case result if result.nonEmpty => {
        val payloadRow = result.head._1
        val multiMediaRows = result.map(_._2).filter(_.isDefined).map(_.get)
        val body = PayloadBody.fromPayloadRowAndMediaRow(payloadRow, multiMediaRows)
        Some(body)
      }
      case _ => None
    }
  }

  object Actions {

    def insert(app: AppRow,
               priceSeq: Seq[PricePlanRow])(implicit ec: EC) = for {
          _ <- AppLive += app
          _ <- PricePlanLive ++= priceSeq
    } yield ()

    def upsert(app: AppRow,
               facets: Seq[FacetRow],
               prices: Seq[PricePlanRow])(implicit ec: EC) = {
      val q = for {
        _ <- AppLive insertOrUpdate app
        _ <- FacetLive.filter(_.id === app.id).delete
        _ <- FacetLive ++= facets
        _ <- PricePlanLive.filter(_.appId === app.id).delete
        _ <- PricePlanLive ++= prices
      } yield ()
      q.map(_ => AppBody.fromTuple((app, None, Seq.empty, prices, Seq.empty)))
    }

    def upsert(app: AppBody)(implicit ec: EC): DBIOAction[AppBody, NoStream, Write] = {
      val (header, facets, prices, _) = app.toTuple
      upsert(header, facets, prices)
    }

    def queryByIdRun(id: Long)(implicit ec: EC) = {

      import com.acrd.giraffe.common.implicits.PlainSQLGetResult._
      async {
        val header = db.run(sql"SELECT * FROM app_live WHERE id = $id".as[AppRow])
        val rating = db.run(sql"SELECT * FROM rating WHERE app_id = $id".as[RatingRow])
        val facets = db.run(sql"SELECT * FROM facet_live WHERE id = $id".as[FacetRow])
        val prices = db.run(sql"SELECT * FROM price_plan_live WHERE app_id = $id".as[PricePlanRow])
        val payloads = db.run(sql"SELECT * FROM payload_live WHERE id = $id".as[PayloadRow])
        val attachments = db.run(sql"SELECT * FROM attachment_live WHERE app_id = $id".as[AttachmentRow])
        (await(header), await(rating), await(facets), await(prices), await(payloads), await(attachments))
      }
    }

    def queryAppComponents(idSet: Set[Long])(implicit ec: EC) = {

      require(idSet.nonEmpty)
      import SqlBuilder.InSetQuery
      import com.acrd.giraffe.common.implicits.PlainSQLGetResult._

      val q = for {
        ratings <- sql"#${InSetQuery("rating", "app_id", idSet)}".as[RatingRow]
        facets <- sql"#${InSetQuery("facet_live", "id", idSet)}".as[FacetRow]
        prices <- sql"#${InSetQuery("price_plan_live", "app_id", idSet)}".as[PricePlanRow]
        payloads <- sql"#${InSetQuery("payload_live", "id", idSet)}".as[PayloadRow]
        attachments <- sql"#${InSetQuery("attachment_live", "app_id", idSet)}".as[AttachmentRow]
      } yield (ratings, facets, prices, payloads, attachments)

      db.run(q)

    }

    def deleteById(id: Long)(implicit ec: EC) = for {
      i <- AppLive.filter(_.id === id).delete
      _ <- AttachmentLive.filter(_.appId === id).delete
      _ <- FacetLive.filter(_.id === id).delete
      _ <- PayloadLive.filter(_.id === id).delete
    } yield i

    def insertPayload(p: PayloadRow) = PayloadLive += p

    def upsertPayload(pl: PayloadBody) = {
      val (p, a) = pl.toPayloadRowAndAttachmentRow
      DBIO.seq(
        PayloadLive insertOrUpdate p,
        if (a.isDefined && a.get.nonEmpty) AttachmentLive ++= a.get else DummyDBIO
      )
    }

    def deletePayload(p: PayloadRow)(implicit ec: EC) = payloadQueryById(p.id, p.lang, p.os).delete

    val headerExists = Compiled{ (id: ConstColumn[Long]) => AppLive.filter(_.id === id).exists}

    val payloadQueryById = Compiled{
      (id: ConstColumn[Long], lang: ConstColumn[Lang], os: ConstColumn[Platform]) =>
        PayloadLive.filter(p => p.id === id && p.lang === lang && p.os === os)
    }

    val payloadQueryByIdWithAttachments = Compiled{
      (id: ConstColumn[Long], lang: ConstColumn[Lang], os: ConstColumn[Platform]) =>
        PayloadLive.filter(p => p.id === id && p.lang === lang && p.os === os) joinLeft Attachment on (_.attachmentSetId === _.setId)
    }

    val payloadQueryByCode = Compiled((code: ConstColumn[String]) => PayloadLive.filter(_.upgradeCode === code).map(_.id))

  }
}
