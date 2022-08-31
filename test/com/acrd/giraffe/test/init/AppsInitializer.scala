package com.acrd.giraffe.test.init

import com.acrd.giraffe.common.utils.Util.await
import com.acrd.giraffe.dao._
import play.api.libs.concurrent.Execution.Implicits._

class AppsInitializer extends Initializer{

  import TestingData.App._
  import TestingData.ActionLogs._

  val prevDao = injector.instanceOf[PreviewAppDAO]
  val liveDao = injector.instanceOf[LiveAppDAO]
  val appDao = injector.instanceOf[AppDAO]
  val migrationDao = injector.instanceOf[MigrationDAO]
  val logDao = injector.instanceOf[ActionLogDAO]

  def ensureSchema = {
    await(appDao.createAllSchema)
  }

  def setupData = {
    await(migrationDao.insertApp(app1))
    await(migrationDao.insertApp(app2))
    await(migrationDao.insertApp(app3))
    await(logDao.insert(log1))
    await(appDao.publish(app1.id.get, app1.payloads.get.head.lang, app1.payloads.get.head.os))
    await(appDao.publish(app2.id.get, app2.payloads.get.head.lang, app2.payloads.get.head.os))
  }

}