package com.acrd.giraffe.controllers

import javax.inject.Inject
import com.acrd.giraffe.base.BaseController
import com.acrd.giraffe.common.CustomActions._
import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.dao.CommentDAO
import com.acrd.giraffe.models.{AppStoreTables, CommentBody}
import AppStoreTables.CommentStatus._
import play.api.i18n.Lang
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.toJson
import com.acrd.giraffe.common.validators.CommentBodyValidator
import scala.util.Success

class Comments @Inject()(commentsDao: CommentDAO) extends BaseController {

  def get(id: Option[Long],
          appId: Option[Long],
          replyTo: Option[Long],
          status: Option[String],
          author: Option[String],
          store: Option[String],
          lang: Option[String],
          skip: Option[Int], top: Option[Int]) = Action.async {

    val statusEnum = status.map(strToCommentStatus)
    val l = lang.map(Lang(_))
    commentsDao.get(id,
                    appId,
                    replyTo,
                    statusEnum,
                    author,
                    store,
                    l,
                    skip.getOrElse(0), top.getOrElse(0))
        .map(result => Ok(toJson(result)))

  }

  def getById(id: Long) = Action.async{
    commentsDao.getById(id).map{
      case Some(s) => Ok(toJson(s))
      case _ => NotFound
    }
  }

  def insert = JsonActionAsync[CommentBody] {

      ValidateAsync(_){ comment =>
        commentsDao.insert(comment).map{
          case Some(c) => Ok(toJson(c))
          case _ => BadRequest
        }
      }(CommentBodyValidator)

  }

  def update(id: Long) = JsonActionAsync[CommentBody] {

      ValidateAsync(_){ comment =>
        commentsDao.update(id, comment).map {
          case c => Ok(toJson(c))
        }.recover{ case e => onError(e) }
      }(CommentBodyValidator)

  }

  def delete(id: Long) = Action.async {
    commentsDao.delete(id).map {
        case Success(i) if i > 0 => Ok
        case _ => throw new IdNotExistsException(id, "Comment")
    }
  }
}