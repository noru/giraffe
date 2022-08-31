package com.acrd.giraffe.controllers

import javax.inject.Inject
import com.acrd.giraffe.base.BaseController
import com.acrd.giraffe.common.CustomActions._
import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.dao.ActionLogDAO
import com.acrd.giraffe.models.ActionLogBody
import play.api.libs.json.Json.toJson
import play.api.libs.concurrent.Execution.Implicits._
import scala.util._
import com.acrd.giraffe.common.registers.Register
import com.acrd.giraffe.common.registers.{CreateActionLogStamper => alStamper}
import com.acrd.giraffe.common.Consts._

class ActionLogs @Inject()(val logDAO: ActionLogDAO) extends BaseController {

  def getByParentId(id: Long) = Action.async {
    logDAO.getByParentId(id).map{ logs => Ok(toJson(logs))}
  }

  def insertAppLog(id: Long) = JsonActionAsync[ActionLogBody]{ log =>

    val result = for {
      exists <- logDAO.appExists(id)
      l <- if (exists) insert(log, id) else throw new IdNotExistsException(id, "App")
    } yield l

    result.map{
      case Success(l) => Ok(toJson(l))
      case Failure(e) => onError(e)
    }
  }

  def insertCommentLog(id: Long) = JsonActionAsync[ActionLogBody] { log =>

    val result = for {
      exists <- logDAO.commentExists(id)
      log <- if (exists) insert(log, id) else throw new IdNotExistsException(id, "Comment")
    } yield log

    result.map{
      case Success(l) => Ok(toJson(l))
      case Failure(e) => onError(e)
    }

  }

  private def insert(log: ActionLogBody, id: Long = DummyId) = {
    val regLog = log & alStamper(id)
    logDAO.insert(regLog)
  }

}

