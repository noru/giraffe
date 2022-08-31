package com.acrd.giraffe.dao

import javax.inject._
import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.acrd.giraffe.common.exceptions.{InvalidParameterException, UncheckedException, IdNotExistsException}
import com.acrd.giraffe.common.{registers, Logging}
import com.acrd.giraffe.common.registers._
import com.acrd.giraffe.common.Consts._
import com.acrd.giraffe.models.{CommentSeqBody, CommentBody, AppStoreTables}
import com.acrd.giraffe.models.gen.{Models, Tables}
import Models.{RatingRow, CommentRow}
import AppStoreTables.CommentStatus._
import com.acrd.giraffe.services.cache.{CacheElement, CacheProxy}
import play.api.i18n.Lang
import play.api.libs.json.Json
import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext => EC, Future}
import scala.language.postfixOps
import scala.util._

object CommentDAO extends Logging{
  
  /** recalculate the rating data based on the new rating, and return the calculated row
    * negative rating (as retract an existing rating) is supported as well
    */
  @tailrec
  def sum(rating: RatingRow, newRatings: Int*)(implicit m: Manifest[RatingRow]): RatingRow = {

    if (newRatings.isEmpty) return rating

    debug(s"Calculate new rating for $rating")

    val newRow = try {
      val newRating = newRatings.head
      (rating.countSum, newRating) match {
        case (_, 0) => rating
        case (1, i) if i < 0 => {
          if (rating.averageRating != -i ) warn("Rating number not match")
          getNewRatingRow(rating.appId)
        }
        case _ => {
          val model = newRating.abs.ensuring(i => i >= 1 && i <= 5)
          val tally = newRating / model // -1/+1
          val constructor = m.runtimeClass.getDeclaredConstructors.head
          val total = (rating.countSum + tally)
              .ensuring(_ == rating.count1 + rating.count2 + rating.count3 + rating.count4 + rating.count5 + tally)
          if (total == 0) return getNewRatingRow(rating.appId)
          val vec = Vector(rating.count1, rating.count2, rating.count3, rating.count4, rating.count5).zipWithIndex
          val average: BigDecimal = {(vec.aggregate(0)((a, b) => a + b._1 * (b._2 + 1), _ + _) + newRating).toDouble / total}
              .ensuring( ave => ave >= 0 && ave <= 5)

          debug(s"Construct new rating row: appId=${rating.appId}, total=$total, average=$average")

          val p = constructor.getParameters.indices.map(idx => idx match {
            case 0 => new java.lang.Long(rating.appId)
            case 6 => new java.lang.Short(total.toShort)
            case 7 => average
            case i if i == model => {
              val newShort = (vec(i - 1)._1 + tally).ensuring(_ >= 0).toShort
              new java.lang.Short(newShort) // +1/-1 on 'count?' tally
            }
            case i => new java.lang.Short(vec(i -1)._1)
          })
          constructor.newInstance(p: _*).asInstanceOf[RatingRow]
        }
      }
    } catch {
      case e: AssertionError =>
        throw new InvalidParameterException(Json.toJson(rating).toString())
      case e: Throwable => {
        error("Error update rating", e)
        error("Rating Row: " + Json.toJson(rating))
        throw new UncheckedException("Update Rating Record Failed")
      }
    }
    val tail = if (newRatings.isEmpty) Seq.empty else newRatings.tail
    sum(newRow, tail: _*)
  }

  
  /** if the app hasn't been rated previously, create a initial rating row for it
    */
  def getNewRatingRow(id: Long)(implicit m: Manifest[RatingRow]) = {

    debug(s"Get new rating stub for app $id")
    val constructor = m.runtimeClass.getDeclaredConstructors.head
    val p: Array[_ <: Object] = constructor.getParameters.zipWithIndex.map(p => p._2 match {
      case 0 => new java.lang.Long(id)
      case 7 => BigDecimal(0)
      case _ => new java.lang.Short(0.toShort)
    })
    constructor.newInstance(p: _*).asInstanceOf[RatingRow]
  }

}

@Singleton
class CommentDAO @Inject()(val context: BaseContext) extends BaseDAO(context) {

  import Tables.mapStringCommentStatus
  import tables.profile.api._
  import tables._
  import CommentDAO._

  def all(implicit ec: EC) = db.run(Comment.result)

  def get(id: Option[Long] = None,
          appId: Option[Long] = None,
          replyTo: Option[Long] = None,
          status: Option[CommentStatus] = None,
          author: Option[String] = None,
          store: Option[String] = None,
          lang: Option[Lang] = None,
          skip: Int = 0, top: Int = QueryLimit)(implicit ec: EC): Future[CommentSeqBody] = {

    val q = wrappedQuery(id, appId, replyTo, status, author, store, lang)

    db.run(for {
      count <- q.length.result
      comments <- q.drop(skip).take(topLimit(top)).result
    } yield (count, comments)).map {
      case (count, c) => CommentSeqBody(count, c)
    }
  }

  def getById(id: Long)(implicit ec: EC): Future[Option[CommentRow]] = db.run(queryByIdAction(id).result).map{
    case results if results.nonEmpty => results.headOption
    case _ => None
  }

  /** Create a comment
    */
  def insert(comment: CommentBody)(implicit ec: EC): Future[Option[CommentRow]] = {

    val row: CommentRow = comment.toCommentRow & CreateCommentStamper

    val q = for {
      i <- Comment += row
      _ <- if (row.status == Live && row.rating > 0) updateRatingAction(row.appId, row.rating) else DummyDBIO
    } yield i

    db.run(q.transactionally).map {
      case 1 => Some(row)
      case _ => None
    }
  }

  /** Delete a comment by its ID
   */
  def delete(id: Long)(implicit ec: EC) : Future[Try[Int]] = {

    val q = for {
      _ <- Comment.filter(_.replyTo === id).delete
      i <- Comment.filter(_.id === id).delete
    } yield i

    db.run(q.transactionally).map(Success(_)).recover{ case e => Failure(e) }
  }

  def update(id: Long, comment: CommentBody)(implicit ec: EC) = {
  
    /** merge requested changes into original comment data
      */
    def merge(request: CommentBody, original: CommentBody): CommentRow = {
      
      import com.acrd.giraffe.common.utils.Util._
      import rapture.json.{Json => RJson}
      import play.api.libs.json.Json._
      import rapture.json.jsonBackends.jawn._

      val requestJson = RJson.parse(toJson(request).toString)

      removeJsonProperties(requestJson, "id", "replyTo", "appId", "author", "createAt", "updateAt")

      val mergedStr = mergeJsonAsString(requestJson.toString, toJson(original).toString)

      debug(s"Merge result: ${mergedStr}")

      parse(mergedStr).validate[CommentBody].get.toCommentRow & UpdateCommentStamper

    }
  
    /** update rating data if the update action: 1. has rating, 2. status change to Live
      */
    def updateRating(comment: CommentBody, original: CommentRow) ={

      val appId = comment.appId.get
      val originalStatus = original.status
      (comment.rating, comment.status) match {
        // preview -> live
        case (Some(rating), Some(Live)) if originalStatus != Live =>
          updateRatingAction(appId, rating)
        // live -> live
        case (Some(rating), Some(Live)) if originalStatus == Live && rating != original.rating =>
          updateRatingAction(appId, -original.rating, rating)
        // live -> unpublish
        case (Some(rating), Some(status)) if status != Live  && originalStatus == Live =>
          updateRatingAction(appId, -original.rating)
        case _ => DBIO.successful(())
      }
    }
  
    val q = queryByIdAction(id)
    val action = for {
      original <- q.result.headOption
      merged <- if(original.isDefined) DBIO.successful(merge(comment, original.get))
                else DBIO.failed(new IdNotExistsException(id))
      _ <- q.update(merged)
      _ <- updateRating(merged, original.get)
    } yield CommentBody.fromCommentRow(merged)

    db.run(action.transactionally)
  }

  // For unit test only, check the update rating result, remove if not needed
  def getAppRating(id: Long): Future[Option[RatingRow]] = db.run(Rating.filter(_.appId === id).result.headOption)

  def updateRatingAction(appId: Long, ratings: Int*)(implicit eC: EC) = {

    debug(s"Update rating for app ${appId}: ${ratings}")

    // invalid live app cache
    CacheProxy.remove(CacheElement.PREFIX_SINGLE_LIVE + appId)

    for {
      oldRating <- RatingQuery(appId).result.headOption
      updatedRecord <- {
        val record = oldRating.getOrElse(getNewRatingRow(appId))
        DBIO.successful(sum(record, ratings: _*))
      }
      writeToDB <- Rating insertOrUpdate updatedRecord
    } yield ()
  }

  val queryByIdAction = Compiled((id: ConstColumn[Long]) => Comment.filter(_.id === id))
  val RatingQuery = Compiled((id: ConstColumn[Long]) => Rating.filter(_.appId === id))

  private def wrappedQuery(id: Option[Long],
                           appId: Option[Long],
                           repliedTo: Option[Long],
                           status: Option[CommentStatus],
                           author: Option[String],
                           store: Option[String],
                           lang: Option[Lang],
                           skip: Int = 0,
                           top: Int = QueryLimit)(implicit ec: EC) = for {
    c <- Comment.filter{ c =>
          // non-option column
          List(
            id.map(c.id === _),
            appId.map(c.appId === _),
            status.map(c.status === _),
            author.map(c.author === _)
          ).collect({case Some(b)  => b}).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])

        }.filter{ c =>
          // option column
          List(
           repliedTo.map(c.replyTo === _),
           store.map(c.storeId === _),
           lang.map(c.lang === _)
          ).collect({case Some(b) => b.getOrElse(false)}).reduceLeftOption(_ && _).getOrElse(true: Rep[Boolean])

        }.sortBy(_.updateAt.desc).drop(skip).take(topLimit(top))} yield c

}


