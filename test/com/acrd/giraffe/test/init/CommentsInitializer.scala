package com.acrd.giraffe.test.init

import com.acrd.giraffe.dao.{ActionLogDAO, CommentDAO}
import TestingData.Comments._
import TestingData.ActionLogs._
import com.acrd.giraffe.common.utils.Util.await
import play.api.libs.concurrent.Execution.Implicits._


class CommentsInitializer extends Initializer {

  val commentDAO = injector.instanceOf[CommentDAO]
  val logDAO = injector.instanceOf[ActionLogDAO]

  def ensureSchema = {
    Initializer[AppsInitializer]
  }

  def setupData = {
    val id = await(commentDAO.insert(comment1)).get.id
    await(commentDAO.insert(comment2.copy(replyTo = Some(id))))
    await(logDAO.insert(log1))
    await(logDAO.insert(log1.copy(id = Some(123L), parentId = Some(id))))
  }
}
