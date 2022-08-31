package com.acrd.giraffe.dao

import javax.inject.{Inject, _}
import com.acrd.giraffe.base.{BaseDAO, BaseContext}
import com.acrd.giraffe.models.ActionLogBody
import scala.util._
import scala.concurrent.{ExecutionContext => EC}

@Singleton
class ActionLogDAO @Inject()(val context: BaseContext) extends BaseDAO(context) {

  import tables._
  import profile.api._
  import Actions._

  def insert(log: ActionLogBody)(implicit ec: EC) = {
    db.run(ActionLog += log.toActionLogRow).map( _ => Success(log)).recover{ case e => Failure(e) }
  }

  def getByParentId(id: Long)(implicit ec: EC) = {
    db.run(actionLogByParentId(id).result).map(_.map(ActionLogBody.fromActionLogRow))
  }

  def appExists(id: Long) = db.run(appExistsCompiled(id).result)

  def commentExists(id: Long) = db.run(commentExistsCompiled(id).result)

  object Actions {
    val actionLogByParentId = Compiled((id: ConstColumn[Long]) => ActionLog.filter(_.parentId === id).sortBy(_.timestamp))
    val appExistsCompiled = Compiled((id: ConstColumn[Long]) => App.filter(_.id === id).exists)
    val commentExistsCompiled = Compiled((id: ConstColumn[Long]) => Comment.filter(_.id === id).exists)

  }

}
