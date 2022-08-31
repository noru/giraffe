package com.acrd.giraffe.test.models

import com.acrd.giraffe.dao.ActionLogDAO
import com.acrd.giraffe.test.init.TestingData.App._
import com.acrd.giraffe.test.init.TestingData.ActionLogs._
import com.acrd.giraffe.test.init.{Initializer => Init, ActionLogInitializer => I}
import org.junit.runner._
import org.specs2.runner._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class ActionLogDAOSpec extends PlaySpecification {

  "ActionLog DAO" should {

    "create a log (for an app or a comment)" in new WithApplication{

      Init[I]
      val dao = implicitApp.injector.instanceOf[ActionLogDAO]

      val ins = await(dao.insert(log2))
      ins.isSuccess must beTrue

      val result = await(dao.getByParentId(app2.id.get))
      result.filter(_.id == ins.get.id) must have size 1

    }

    "get a collection of action logs by parent id" in new WithApplication{

      Init[I]
      val dao = implicitApp.injector.instanceOf[ActionLogDAO]

      var result = await(dao.getByParentId(123123L))
      result.isEmpty must beTrue

      result = await(dao.getByParentId(app1.id.get))
      result.nonEmpty must beTrue

    }



  }

}
