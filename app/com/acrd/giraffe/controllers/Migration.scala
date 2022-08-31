package com.acrd.giraffe.controllers

import javax.inject.Inject
import com.acrd.giraffe.base.BaseController
import com.acrd.giraffe.common.exceptions._
import com.acrd.giraffe.common.implicits.Timestamps._
import com.acrd.giraffe.common.validators._
import com.acrd.giraffe.dao._
import com.acrd.giraffe.models.{CommentBody, AppBody}
import com.acrd.giraffe.models.gen.Models
import Models.RatingRow
import com.acrd.giraffe.common.CustomActions._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import rapture.data.ForcedConversion
import scala.concurrent.Future
import scala.util._
import play.api.libs.json._
import Json._
import com.acrd.giraffe.common.utils.Util._

@deprecated("Data Migration is done, it should not be used!", "1.0")
class Migration @Inject()(migrationDao: MigrationDAO, previewAppDAO: PreviewAppDAO, appDao: AppDAO) extends BaseController{

  @deprecated("Data Migration is done, it should not be used!", "1.0")
  def insertApp(live: Option[Boolean]) = JsonActionAsync[AppBody] {

    ValidateAsync(_){ app =>

      val insertion = if (live.getOrElse(false)) migrationDao.insertLiveApp _ else migrationDao.insertApp _
      insertion(app).map{
        case Failure(e) => onError(e)
        case Success(result) => Ok(toJson(result))
      }
    }(AppBodyValidator)
  }

  @deprecated("Data Migration is done, it should not be used!", "1.0")
  def updateApp(id: Long) = Action.async { request =>

    val body = request.body.asJson.get.toString

    def merge(str: String, origin: AppBody): String = {

      val original: AppBody = origin.copy(payloads = origin.payloads.map(_.map(_.copy(attachments = None, attachmentSetId = None))))
      import rapture.core.modes.returnOption._
      import rapture.json.{Json => RJson, _}
      import RJson._
      import rapture.json.jsonBackends.jawn._
      import jawn.ast._

      val tuple = original.toTuple
      val prices = tuple._3
      val payloads = tuple._4.map(_.map(_.copy(attachmentSetId = None, attachments = None)))

      val timestamp = timestamp2UtcString(now)
      val jsChange = JsonBuffer.parse(str).get
      val payloadStr = RJson.format(jsChange.payloads)
      jsChange.payloads = json"[]"

      val jsOrigin = JsonBuffer.parse(toJson(original).toString).get
      Seq("facets", "pricePlans").foreach({ prop =>
        val value = jsChange \ prop
        if (value.toString != "undefined") {
          jsOrigin.$root.value.asInstanceOf[JObject].remove(prop)
        }
      })

      val merged = mergeJson(jsChange, jsOrigin)

      def forcedConversion(long: Long) = ForcedConversion(LongNum(long), nothing = false)
      // ensure header
      merged.app.id = forcedConversion(id)
      merged.app.updateAt = timestamp

      // ensure payload
      val payloadsJs = JsonBuffer.parse(payloadStr).get
      val payloadArray = payloadsJs.as[JArray]
      val payloadKeySet = payloads.map(_.map(p => (p.lang.code, p.os.toString)).toSet).get
      if (payloadArray.isDefined && payloadArray.get.vs.nonEmpty){
        payloadArray.get.vs.foreach(p => {
          val key = (p.get("lang").asString, p.get("os").asString)
          if (payloadKeySet.contains(key)){
            val index = merged.as[JObject].get.get("payloads").asInstanceOf[JArray].vs.
                indexWhere(p =>
                  p.get("lang").asString == key._1 &&
                      p.get("os").asString == key._2
                )
            p.set("id", JNum(id))
            p.set("updateAt", JString(timestamp))
            val toBeMerged = merged.payloads(index)
            merged.payloads(index) = toBeMerged ++ RJson.parse(p.toString).get
          } else {
            val newPayload = JsonBuffer.parse(p.toString).get
            newPayload.id = forcedConversion(id)
            newPayload.createAt = timestamp
            newPayload.updateAt = timestamp
            merged.payloads += newPayload
          }
        })
      }

      RJson.format(merged)
    }

    val action = for {
      original <-  previewAppDAO.get(id)
      validated <- if (original.isDefined) Future(AppDAO.validateBody[AppBody](merge(body, original.get)))
                   else Future.failed(new IdNotExistsException(id))
      update <- migrationDao.updateApp(validated.get)
    } yield (validated, update)

    action.map{
      case (Success(a), result) if result.isSuccess => Ok(toJson(a))
      case (_, result) if result.isFailure => onError(result.failed.get)
      case _ => onError(new UncheckedException(s"Update Failed. Id $id, body $body"))
    }
  }

  @deprecated("Data Migration is done, it should not be used!", "1.0")
  def migrateComment = JsonActionAsync[CommentBody] {

    ValidateAsync(_){ comment =>
      migrationDao.migrateComment(comment).map{
        case Some(c) => Ok(toJson(c))
        case _ => BadRequest
      }
    }(CommentBodyValidator)

  }

  @deprecated("Data Migration is done, it should not be used!", "1.0")
  def migrateRating = JsonActionAsync[RatingRow] { rating =>

      migrationDao.upsertRating(rating).map{
        case 1 => Ok
        case _ => InternalServerError
      }
  }

  @deprecated("Data Migration is done, it should not be used!", "1.0")
  def howManyIdDoYouWant = Action{ request =>

      def getId(count: Int): Seq[Long] = {
        import com.acrd.giraffe.common.utils.IdGenerator.getUID
        1 to count map(_ => getUID)
      }
      Try(request.rawQueryString.toInt) match {
        case Success(i) if i > 0 => Ok(toJson(getId(i)))
        case _ => throw new InvalidParameterException(request.rawQueryString + " must be a positive integer")
      }

  }

}
