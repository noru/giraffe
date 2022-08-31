package com.acrd.giraffe.test.models

import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.dao.{LiveAppDAO, PreviewAppDAO, MigrationDAO}
import com.acrd.giraffe.models.CommentBody._
import com.acrd.giraffe.test.init.TestingData.App._
import com.acrd.giraffe.test.init.TestingData.Comments._
import com.acrd.giraffe.test.init.{Initializer => Init, AppsInitializer => I}
import org.junit.runner._
import org.specs2.runner._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class MigrationDAOSpec extends PlaySpecification {

  "Migration DAO" should {


        "post a content as preview app" in new WithApplication{

          Init[I]
          val dao = implicitApp.injector.instanceOf[MigrationDAO]
          val prevDao = implicitApp.injector.instanceOf[PreviewAppDAO]

          val result = await(dao.insertApp(app4))
          result.isSuccess must beTrue
          await(prevDao.delete(app4.id.get)).get mustEqual 1

        }

        "post a content as live app" in new WithApplication{

          Init[I]
          val dao = implicitApp.injector.instanceOf[MigrationDAO]
          val liveDao = implicitApp.injector.instanceOf[LiveAppDAO]

          val result = await(dao.insertLiveApp(app4))
          result.isSuccess must beTrue
          await(liveDao.delete(app4.id.get)).get mustEqual 1

        }

        "update a preview App" in new WithApplication{

          Init[I]
          val dao = implicitApp.injector.instanceOf[MigrationDAO]
          val prevDao = implicitApp.injector.instanceOf[PreviewAppDAO]

          var result = await(dao.updateApp(app4))
          result.isSuccess must beFalse
          result.failed.get must beAnInstanceOf[IdNotExistsException]

          result = await(dao.updateApp(changedApp1))
          result.isSuccess must beTrue
          val changed = await(prevDao.get(app1.id.get)).get
          changed.payloads.get.head.url mustEqual changedApp1.payloads.get.head.url
          await(prevDao.delete(app1.id.get)).get mustEqual 1
        }

        "force update (upsert) an App" in new WithApplication{

          Init[I]
          val dao = implicitApp.injector.instanceOf[MigrationDAO]
          val prevDao = implicitApp.injector.instanceOf[PreviewAppDAO]
          val result = await(dao.updateApp(app3, force = true))
          result.isSuccess must beTrue
          await(prevDao.get(app3.id.get)).get.id mustEqual app3.id
          await(prevDao.delete(app3.id.get)).get mustEqual 1

        }

        "migrate a comment" in new WithApplication{

          Init[I]
          val dao = implicitApp.injector.instanceOf[MigrationDAO]

          var body = await(dao.migrateComment(comment2))
          body.isDefined must beTrue
          isEqual(comment2, body.get) must beTrue

        }

  }

}
