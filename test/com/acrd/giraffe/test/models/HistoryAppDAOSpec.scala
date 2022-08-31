package com.acrd.giraffe.test.models

import com.acrd.giraffe.dao.HistoryAppDAO
import com.acrd.giraffe.test.init.TestingData.App._
import com.acrd.giraffe.test.init.{AppsInitializer => I, Initializer => Init }
import org.junit.runner._
import org.specs2.runner._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class HistoryAppDAOSpec extends PlaySpecification {

  "HistoryAppDAO" should {

    "get a history record of an app by id" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[HistoryAppDAO]

      val history = await(dao.getPayloadHistory(app1.id.get))
      val payload = app1.payloads.get.head

      history must have size 1

      history.head.url mustEqual app1.payloads.get.head.url

    }

  }

}
