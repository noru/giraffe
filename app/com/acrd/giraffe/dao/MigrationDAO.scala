package com.acrd.giraffe.dao

import javax.inject.{Inject, _}
import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.common.registers._
import com.acrd.giraffe.models.gen.Models._
import com.acrd.giraffe.models.{CommentBody, AppBody, PayloadBody}
import com.acrd.giraffe.dao.PreviewAppDAO._
import scala.concurrent.{ExecutionContext => EC, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class MigrationDAO @Inject()(val context: BaseContext) extends BaseDAO(context) {

  import tables._
  import profile.api._
  import Actions._

  val idMatchPredict: (Long) => AppPartialPredict = { id => { case a => a.id === id }}

  /** upsert a Rating Record for an published App
    */
  def upsertRating(r: RatingRow)(implicit ec: EC) = {
    db.run(Rating insertOrUpdate r)
  }

  /** Create a complete app content in preview mode
    */
  def insertApp(app: AppBody)(implicit ec: EC): Future[Try[AppBody]] = {

    val regApp = app & migrationCreateAppStamper

    val tuple = regApp.toTuple
    db.run(
      insertAction(tuple._1, tuple._2, tuple._3, tuple._4.get).transactionally
    ).map(_ => Success(regApp)).recover{ case e => Failure(e)}

  }

  /** Update an App content in preview mode
    */
  def updateApp(app: AppBody, force: Boolean = false)(implicit ec: EC): Future[Try[AppBody]] = {

    val regApp = app & migrationUpdateAppStamper

    val tuple = regApp.toTuple
    val id = app.id.get
    // delete -> insert strategy
    def deleteInsert() = deleteAction(idMatchPredict(id)).flatMap{
      case 0 if !force => DBIO.failed(new IdNotExistsException(id))
      case _ => insertAction(tuple._1, tuple._2, tuple._3, tuple._4.get)
    }.transactionally

    //  inline update strategy
    //  def inline = ???

    val updateAction = deleteInsert
    db.run(updateAction).map(_ => Success(regApp)).recover{ case e => Failure(e) }

  }

  /** Create a complete app content in live mode
    */
  def insertLiveApp(app: AppBody)(implicit ec: EC): Future[Try[AppBody]] = {

    val regApp = app & migrationCreateLiveAppStamper

    def insert(app: AppBody) = {

      val (header, facets, prices, payloads) = regApp.toTuple
      val latestPayloads = payloads.map(
        _.groupBy(p => (p.lang, p.os)).map{ case (key, p) => p.sortBy(_.updateAt.get.getTime).last }.toSeq
      )
      db.run(insertLiveAction(header, facets, prices, latestPayloads.get).transactionally)
    }

    def createHistory(app: AppBody) = {
      val (header, _, prices, payloads) = regApp.toTuple
      db.run(insertHistoryAction(header, prices, payloads.get).transactionally)
    }

    for {
      _ <- cleanUp(app.id.get)
      _ <- insert(regApp)
      _ <- createHistory(regApp)
    } yield Success(regApp)

  }

  /** migrate a comment
    */
  def migrateComment(comment: CommentBody)(implicit ec: EC): Future[Option[CommentRow]] = {

    val row: CommentRow = comment.toCommentRow.copy(id = getUID)
    db.run(Comment += row).map {
      case 1 => Some(row)
      case _ => None
    }
  }

  def cleanUp(id: Long) = db.run(cleanUpAction(id).transactionally)

  object Actions {

    def insertAction(app: AppRow,
                     facets: Seq[FacetRow],
                     prices: Seq[PricePlanRow],
                     payloads: Seq[PayloadBody]) = {

      val p = payloads.map(_.toPayloadRowAndAttachmentRow)
      (App += app) andThen
      Facet.filter(_.id === app.id).delete andThen
      (Facet ++= facets) andThen
      (PricePlan ++= prices) andThen
      (Attachment ++= p.flatMap(_._2).flatMap(m => m)) andThen
      (Payload ++= p.map(_._1))
    }

    def insertLiveAction(app: AppRow,
                     facets: Seq[FacetRow],
                     prices: Seq[PricePlanRow],
                     payloads: Seq[PayloadBody]) = {

      val p = payloads.map(_.toPayloadRowAndAttachmentRow)
      (AppLive += app) andThen
      FacetLive.filter(_.id === app.id).delete andThen
      (FacetLive ++= facets) andThen
      (PricePlanLive ++= prices) andThen
      (AttachmentLive ++= p.flatMap(_._2).flatMap(m => m)) andThen
      (PayloadLive ++= p.map(_._1))
    }

    def insertHistoryAction(app: AppRow,
                         prices: Seq[PricePlanRow],
                         payloads: Seq[PayloadBody]) = {

      val p = payloads.map(_.toPayloadRowAndAttachmentRow._1).map(p => p.copy(createAt = p.updateAt))
      (AppHistory += app) andThen
      (PricePlanHistory ++= prices) andThen
      (PayloadHistory ++= p)

    }

    def cleanUpAction(id: Long) = {
      DBIO.seq(
        Facet.filter(_.id === id).delete,
        FacetLive.filter(_.id === id).delete,
        PricePlan.filter(_.appId === id).delete,
        PricePlanLive.filter(_.appId === id).delete,
        PricePlanHistory.filter(_.appId === id).delete,
        Payload.filter(_.id === id).delete,
        PayloadLive.filter(_.id === id).delete,
        PayloadHistory.filter(_.id === id).delete,
        App.filter(_.id === id).delete,
        AppLive.filter(_.id === id).delete,
        Rating.filter(_.appId === id).delete,
        Comment.filter(_.appId === id).delete,
        Attachment.filter(_.appId === id).delete,
        AttachmentLive.filter(_.appId === id).delete,
        AppHistory.filter(_.id === id).delete,
        ActionLog.filter(_.parentId === id).delete
      )
    }

    def simpleQueryAction(predict: AppPredict) = App.filter(predict)

    def deleteAction(predict: AppPredict)(implicit ec: EC) = simpleQueryAction(predict).delete

  }

}
