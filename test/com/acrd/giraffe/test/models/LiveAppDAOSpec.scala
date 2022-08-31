package com.acrd.giraffe.test.models

import com.acrd.giraffe.common.utils.Filter
import com.acrd.giraffe.dao.AppDAO.SortOption._
import com.acrd.giraffe.dao.LiveAppDAO
import com.acrd.giraffe.test.init.TestingData.App._
import com.acrd.giraffe.test.init.{AppsInitializer => I, Initializer => Init}
import org.junit.runner._
import org.specs2.runner._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class LiveAppDAOSpec extends PlaySpecification {

  "LiveAppsDAO" should {

    "get by upgrade code" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[LiveAppDAO]

      var result = await(dao.getByCode(app1.payloads.get.head.upgradeCode))

      result.isDefined must beTrue
      result.get.id mustEqual app1.id

    }

    "get live App list" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[LiveAppDAO]

      var result = await(dao.get(skip = 0, top = 0, orderBy = CreateAt, ascending = true, filters = Seq.empty))
      result._1 mustEqual 2
      result._2 must have size 2
      val createAt = result._2.map(_.createAt.get.getTime)
      (createAt, createAt.tail).zipped.forall( _ <= _ ) aka "list order" must beTrue

    }

    "get live App list, paging" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[LiveAppDAO]

      var result = await(dao.get(skip = 1, top = 1, orderBy = CreateAt, ascending = true, filters = Seq.empty))
      result._1 mustEqual 2
      result._2 must have size 1

    }

    "get live App list, id filter" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[LiveAppDAO]

      val id = app1.id.get
      var result = await(dao.get(skip = 0, top = 0, orderBy = CreateAt, ascending = true, filters = Seq(Filter("id", id.toString))))
      result._1 mustEqual 1
      result._2 must have size 1

    }

    "get live App list, author filter" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[LiveAppDAO]

      val author = Set(app1.author)
      var result = await(dao.get(skip = 0, top = 0, orderBy = CreateAt, ascending = true,  filters = Seq(Filter("author_id", "'author'"))))
      result._1 mustEqual 1
      result._2 must have size 1

    }

  }

}
