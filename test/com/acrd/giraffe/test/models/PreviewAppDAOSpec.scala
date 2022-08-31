package com.acrd.giraffe.test.models

import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.common.utils.Filter
import com.acrd.giraffe.models.{PayloadBody, AppStoreTables}
import com.acrd.giraffe.dao.AppDAO.SortOption._
import com.acrd.giraffe.dao.PreviewAppDAO
import com.acrd.giraffe.test.init.TestingData.App._
import com.acrd.giraffe.test.init.TestingData.Media._
import com.acrd.giraffe.test.init.TestingData.Payload._
import com.acrd.giraffe.test.init.{AppsInitializer => I, Initializer => Init}
import AppStoreTables.PayloadStatus
import org.junit.runner._
import org.specs2.runner._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test._
import com.acrd.giraffe.services.TextService

@RunWith(classOf[JUnitRunner])
class PreviewAppDAOSpec extends PlaySpecification {

  "PreviewAppsDAO" should {

    "get a preview App by Id" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      var result = await(dao.get(123L))
      result.isDefined must beTrue
      result.get.id must beEqualTo(app1.id)

    }

    "get preview App list" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      var result = await(dao.get(skip = 0, top = 0, orderBy = CreateAt, ascending = true, filters = Seq.empty))
      result._1 mustEqual 3
      result._2 must have size 3
      val createAt = result._2.map(_.createAt.get.getTime)
      (createAt, createAt.tail).zipped.forall( _ <= _ ) aka "list order" must beTrue

    }

    "get preview App list, paging" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      var result = await(dao.get(skip = 1, top = 1, orderBy = CreateAt, ascending = true, filters = Seq.empty))
      result._1 mustEqual 3
      result._2 must have size 1

    }

    "get preview App list, id filter" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      val id = app1.id.get
      var result = await(dao.get(skip = 0, top = 0, orderBy = CreateAt, ascending = true, filters = Seq(Filter("id", id.toString))))
      result._1 mustEqual 1
      result._2 must have size 1

    }

    "get preview App list, author filter" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      val author = Set(app1.author)
      var result = await(dao.get(skip = 0, top = 0, orderBy = CreateAt, ascending = true, filters = Seq(Filter("author_id", "'author'"))))
      result._1 mustEqual 1
      result._2 must have size 1

    }

    "create a App Header" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]
      var result = await(dao.createAppHeader(app4))
      result.isSuccess must beTrue

    }

    "update a App Header" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]
      var result = await(dao.updateAppHeader(app1.copy(supportContact = Some("anderson@xiu.com"))))
      result.isSuccess must beTrue
      val result2 = await(dao.get(app1.id.get))
      result2.get.supportContact mustEqual Some("anderson@xiu.com")

    }

    "create a Payload for an existing App" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      val body = PayloadBody.fromPayloadRowAndMediaRow(payload1.copy(lang = TextService.cs), attachmentSeq)
      var result = await(dao.createPayload(body))
      result.isSuccess must beTrue
    }

    "cannot create a duplicate Payload" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      val body = PayloadBody.fromPayloadRowAndMediaRow(payload1)
      var result = await(dao.createPayload(body))
      result.isFailure must beTrue
    }

    "update a existing Payload for an existing App" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      val id = app1.id.get
      val payload = app1.payloads.get.head.copy(url = "abc")
      var result = await(dao.updatePayload(payload))
      result.isSuccess must beTrue

      val result2 = await(dao.get(id))
      result2.get.payloads.get.find(p => p.lang == payload.lang && p.os == payload.os).get.url mustEqual "abc"
    }


    "update a payload's status" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      val id = app1.id.get
      val payload = app1.payloads.get.head
      var result = await(dao.updatePayloadStatus(id, payload.lang, payload.os, PayloadStatus.Submission))
      result.isSuccess must beTrue
    }

    "delete an existing Payload" in new WithApplication{
      Init[I]
      val dao = implicitApp.injector.instanceOf[PreviewAppDAO]

      val id = app1.id.get
      val payload = app1.payloads.get.head
      var result = await(dao.deletePayload(id, payload.lang, payload.os))
      result.isSuccess must beTrue
      result = await(dao.deletePayload(id, payload.lang, payload.os))
      result.isFailure must beTrue
      result.failed.get must beAnInstanceOf[IdNotExistsException]
    }

  }

}
