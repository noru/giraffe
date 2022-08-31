package com.acrd.giraffe.controllers

import com.acrd.giraffe.base.BaseController
import com.acrd.giraffe.dao.AppDAO
import AppDAO.SortOption._
import com.acrd.giraffe.models._
import com.acrd.giraffe.services.cache.CacheProxy
import CacheProxy._
import com.google.inject.Inject
import com.wix.accord.{Failure => vFail}
import com.acrd.giraffe.common.Consts.{QueryLimit => Limit}
import com.acrd.giraffe.common.CustomActions._
import com.acrd.giraffe.common.exceptions._
import com.acrd.giraffe.common.utils.{Util, FilterHelper}
import com.acrd.giraffe.common.validators._
import com.acrd.giraffe.common.implicits
import com.acrd.giraffe.common.registers._
import AppStoreTables.PayloadStatus._
import AppStoreTables.Platform._
import com.acrd.giraffe.services.TextService._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json._
import scala.util.{Failure, Success}
import rapture.json.{Json => RJson}
import rapture.json.jsonBackends.jawn._
import com.acrd.giraffe.common.utils.Util._

class Apps @Inject()(appDAO: AppDAO) extends BaseController{

  import implicits.Timestamps._

  /**
   * Single app query by id
   */
  def getById(id: Long, live: Option[Boolean] = None) = Cached.status(rh => getCacheKey(id, live, rh)) {
    Action.async { request =>

      appDAO.getById(id, live.getOrElse(true)).map{
        case Some(a) => Ok(toJson(a))
        case _ => NotFound
      }
    }
  }

  /**
   * Dynamic query apps
   */
  def get(skip: Option[Int],
          top: Option[Int],
          orderBy: Option[String],
          ascending: Option[Boolean],
          filter: Option[String],
          live: Option[Boolean] = None) = Cached.status(rh => getCacheKey(skip, top, orderBy, ascending, filter, live, rh)){

    Action.async {
      appDAO.get(skip.getOrElse(0),
        top.getOrElse(Limit),
        orderBy.map(strToSortOption).getOrElse(CreateAt),
        ascending.getOrElse(false),
        FilterHelper.parse(filter),
        live.getOrElse(true))
        .map{ case (count, apps) => Ok(toJson(AppSeqBody(count, skip, Some(apps.size), apps)))}

    }
  }

  def getByCode(code: String) = Action.async { request =>
    appDAO.getByCode(code).map{
      case Some(a) => Ok(toJson(a))
      case _ => NotFound
    }
  }

  def getPayloadHistory(id: Long) = Action.async { r =>
    appDAO.getPayloadHistory(id).map{
      case his => his.map{
        case ((lang, os), payloads) => PayloadHistory(lang, os, payloads)
      }
    }.map(wrapped => Ok(toJson(wrapped)))
  }

  /** Create a new app without payloads as preview version
   */
  def createApp = JsonActionAsync[AppBody] {

      ValidateAsync(_){ app =>
        appDAO.createAppHeader(app).map{
          case Success(b) => Ok(toJson(b))
          case Failure(e) => onError(e)
        }

      }(AppBodyValidator)

  }

  /** Update an app info
    */
  def updateApp(id: Long) = Action.async { request =>

      val bodyAsStr = request.body.asJson.get.toString

      def merge(change: String, origin: AppBody): AppBody = {

        val jsChange = RJson.parse(change)
        val jsOrigin = RJson.parse(toJson(origin).toString)
        removeJsonProperties(jsChange, "id", "payloads", "createAt", "updateAt", "payloads")

        // for the update of array properties, use "all or none" strategy:
        // The original collection will be overwritten or stay intact according to the presence of these properties
        // within the change request
        Seq("facets", "pricePlans").foreach({ prop =>
            val value = jsChange \ prop
          if (value.toString != "undefined") {
            removeJsonProperties(jsOrigin, prop)
          }
        })

        val mergedResult = mergeJson(jsChange, jsOrigin).toString

        debug(s"merge result: $mergedResult")
        import com.wix.accord._

        val app = AppDAO.validateBody[AppBody](mergedResult).get

        validate(app)(AppBodyValidator) match {
          case r if r.isSuccess => app & UpdateAppHeaderStamper
          case vFail(e) => throw new ValidationFailedException(e)
        }

      }

      val update = for {
        origin <- appDAO.getAppHeader(id)
        updated <- if (origin.isDefined){
                    val app = merge(bodyAsStr, origin.get)
                    appDAO.updateAppHeader(app)
                  } else throw new IdNotExistsException(id)
      } yield updated

      update.map{
        case Success(app) => Ok(toJson(app))
        case Failure(e) => onError(e)
      }

  }
  /** Delete an app by id
   */
  def deleteApp(id: Long) = Action.async {

    appDAO.delete(id).map{
      case Success(i) => if (i == 1) Ok else NotFound
      case Failure(e) => onError(new UncheckedException(s"Delete App Failed, id $id"), e)
    }

  }
  def createPayload(id: Long) = JsonActionAsync[PayloadBody] {

    ValidateAsync(_) { payload =>
      appDAO.createPayload(payload.copy(id = Some(id))).map {
        case Success(p) => Ok(toJson(p))
        case Failure(e) => onError(e)
      }
    }(PayloadValidator)

  }

  def deletePayload(id: Long, lang: String, os: String) = Action.async {
    appDAO.deletePayload(id, lang, os).map {
      case Success(_) => Ok
      case Failure(e) => onError(e)
    }
  }

  def updatePayload(id: Long, lang: String, os: Platform, status: Option[String]) = Action.async { request =>

    status match {
      case Some(s) => updatePayloadStatus(id, lang, os, strToPayloadStatus(Util.lc2lu(s)))
      case _ => updatePayloadContent(id, lang, os, request.body.asJson.get.toString)
    }

  }

  def updatePayloadContent(id: Long, lang: String, os: Platform, change: String) = {

    def merge(change: String, origin: PayloadBody): PayloadBody = {

      val jsChange = RJson.parse(change)
      val jsOrigin = RJson.parse(toJson(origin).toString)
      removeJsonProperties(jsChange, "id", "lang", "os", "createAt", "updateAt", "attachmentSetId")

      // for the update of array properties, use "all or none" strategy:
      // The original collection will be overwritten or stay intact according to the presence of these properties
      // within the change request
      Seq("attachments").foreach({ prop =>
        val value = jsChange \ prop
        if (value.toString != "undefined") {
          removeJsonProperties(jsOrigin, prop)
        }
      })

      val mergedResult = mergeJson(jsChange, jsOrigin).toString

      debug(s"merge result: $mergedResult")

      import com.wix.accord._
      import com.acrd.giraffe.common.validators.PayloadValidator

      val payload = AppDAO.validateBody[PayloadBody](mergedResult).get

      validate(payload)(PayloadValidator) match {
        case r if r.isSuccess => payload
        case vFail(e) => throw new ValidationFailedException(e)
      }

    }

    val action = for {
      original <- appDAO.getPayload(id, lang, os, live = false)
      updated <- if (original.isDefined) appDAO.updatePayload(merge(change, original.get))
      else throw new PayloadIdNotExistsException(id, lang, os)
    } yield updated

    action.map{
      case Success(p) => Ok(toJson(p))
      case Failure(e) => onError(e)
    }

  }

  def updatePayloadStatus(id: Long, lang: String, os: Platform, status: PayloadStatus) = {
    val updateMethod = status match {
      case Live => appDAO.publish _
      case Unpublished => appDAO.unpublish _
      case _ => appDAO.updateSubStatus(status) _
    }
    updateMethod(id, lang, os).map{
      case Failure(e) => onError(e)
      case _ => Ok
    }
  }

}
