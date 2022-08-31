package com.acrd.giraffe.controllers

import javax.inject.Inject
import com.acrd.giraffe.base.BaseController
import com.acrd.giraffe.common.CustomActions._
import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.dao.SettingsDAO
import com.acrd.giraffe.models.Metadata
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json._
import scala.util._

class Settings @Inject()(settingsDAO: SettingsDAO) extends BaseController{

  def getAll(compact: Option[Boolean]) = Action.async {
     settingsDAO.getAll(compact.getOrElse(true))
          .map(result => Ok(toJson(result)))
  }

  def get(name: String, recursively: Option[Boolean]) = Action.async {
    settingsDAO.getById(name, recursively = recursively.getOrElse(true)).map{
      case Some(m) => Ok(toJson(m))
      case _ => onError(new IdNotExistsException(name, "Settings"))
    }
  }

  def create = JsonActionAsync[Metadata] { metadata =>
      settingsDAO.insert(metadata).map{
        case Failure(e) => onError(e)
        case _ => Ok(toJson(metadata))
      }
  }

  def update(id: String) = JsonActionAsync[Metadata] { metadata =>

      def action = metadata.children.isEmpty match {
        /** when content contains only one record, make the update on the single line, and only affect name, value, type */
        case true => settingsDAO.updateSingle(metadata)
        /** content is a hierarchy, update top node and its descendants as a whole (delete all, then insert) */
        case false => settingsDAO.updateHierarchy(metadata)
      }
      settingsDAO.getById(id).flatMap{
        case None => throw new IdNotExistsException(id, "Settings")
        case _ => action.map{
          case Failure(e) => onError(e)
          case _ => Ok
        }
      }
  }

  def delete(name: String) = Action.async {
      settingsDAO.delete(name).map{
        case Failure(e) => onError(e)
        case _ => Ok
      }
  }


}
