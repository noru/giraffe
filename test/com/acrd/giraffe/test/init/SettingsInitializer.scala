package com.acrd.giraffe.test.init

import com.acrd.giraffe.base.TestingContext
import com.acrd.giraffe.common.utils.Util.await
import TestingData.Settings._
import com.acrd.giraffe.dao.SettingsDAO
import play.api.libs.concurrent.Execution.Implicits._

class SettingsInitializer extends Initializer{

  val dao = new SettingsDAO(new TestingContext)

  def ensureSchema = {
    await(dao.createSchema)
  }

  def setupData = {
    await(dao.insert(root2))
  }

}