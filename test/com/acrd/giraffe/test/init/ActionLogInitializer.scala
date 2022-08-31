package com.acrd.giraffe.test.init

import com.acrd.giraffe.common.utils.Util.await
import com.acrd.giraffe.dao._
import play.api.libs.concurrent.Execution.Implicits._

class ActionLogInitializer extends Initializer{

  import TestingData.ActionLogs._

  val actionLogDao = injector.instanceOf[ActionLogDAO]

  def ensureSchema = {
    await(actionLogDao.createAllSchema)
  }

  def setupData = {
    val result = await(actionLogDao.insert(log1))
    val sd =123
  }

}