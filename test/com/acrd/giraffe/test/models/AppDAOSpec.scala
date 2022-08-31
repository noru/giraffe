package com.acrd.giraffe.test.models

import com.acrd.giraffe.models.gen.Models
import Models._
import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.common.implicits.Options
import com.acrd.giraffe.common.implicits.Options._
import com.acrd.giraffe.common.utils.IdGenerator
import com.acrd.giraffe.dao.{HistoryAppDAO, AppDAO}
import com.acrd.giraffe.test.init.TestingData.App._
import com.acrd.giraffe.test.init.{AppsInitializer => I, Initializer => Init}
import org.junit.runner._
import org.specs2.runner._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test._
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class AppDAOSpec extends PlaySpecification {

  "AppDAO static method" should {

    import AppDAO._

    def randomTimes = Random.nextInt(5) + 1
    "construct db rows to flat structure " in {

      val data: DBResult = {
        val headers = {
          var result: Seq[AppRow] = Seq.empty
          for (i <- 1 to 2000) {
            val id = IdGenerator.getIncrementUID
            val appRow = AppRow(id, null, null, "", null, "")
            result = result :+ appRow
          }
          result
        }
        val ratings: Seq[RatingRow] = Seq.empty
        val attachments: Seq[AttachmentRow] = Seq.empty
        var facets: Seq[FacetRow] = Seq.empty
        var prices: Seq[PricePlanRow] = Seq.empty
        var payloads: Seq[PayloadRow] = Seq.empty
        headers.foreach(h => {
          val id = h.id
          for(i <- 0 to randomTimes){
            facets = facets :+ FacetRow(id, "")
          }
          for(i <- 0 to randomTimes){
            prices = prices :+ PricePlanRow(0L, null, id, "test", None, 0, 0, None,"USD", None, None)
          }
          import com.acrd.giraffe.services.TextService._
          val langSeq = Seq(en, ja, pl, pt, zh_CN, zh_TW, de)
          for(i <- 0 to randomTimes){
            payloads = payloads :+ PayloadRow(id = id,
              lang = langSeq(i), os = null, createAt = null, updateAt = null,
              reviewStatus = None,
              url = "http://google.com/image.png", size = 123, mime = "image/png",
              attachmentSetId = 123L,
              title = "App1 Title",
              shortDescription = "Short Description",
              description = "Looooooooooooonnnnnnnnnnnggggggggggg description",
              instruction = "instruction",
              installation = "installation guide",
              additionalInfo = "blabla",
              knownIssue = "it has a lot bugs",
              supportInfo = "Don't call me, I'll call you",
              version = "",
              versionDescription = "",
              upgradeCode = "",
              productCode = ""
            )
          }
        })

        (headers, ratings, facets, prices, payloads, attachments)

      }

      val start = System.currentTimeMillis()
      val result = construct(data)
      val end = System.currentTimeMillis()
      val elapsed = end - start
      elapsed must be between(0, 200)   // around 80

      result must have size 2000

      result.flatMap(_._3).size mustEqual data._3.size
      result.flatMap(_._4).size mustEqual data._4.size
      result.flatMap(_._5).size mustEqual data._5.size

    }

  }


  "AppDAO" should {

    "get App(live) by ID" in new WithApplication{

      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]
      await(appDAO.getById(123L)).get.id mustEqual app1.id

    }

    "get App(live) by non-existing ID" in new WithApplication{

      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]
      await(appDAO.getById(123981827098748970L)).isEmpty must beTrue

    }

    "get App(preview) by ID" in new WithApplication{

      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]
      await(appDAO.getById(123L, live = false)).get.id mustEqual app1.id

    }

    "get App(preview) by non-existing ID" in new WithApplication{

      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]
      await(appDAO.getById(1298789654182737L, live = false)).isEmpty must beTrue

    }

    "delete App(Preview) by ID" in new WithApplication{

      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]
      await(appDAO.delete(123L)).get mustEqual 1
      await(appDAO.delete(124567L)).get mustEqual 0

    }

    "Publish an Payload" in new WithApplication{

      Init[I]

      val appDAO = implicitApp.injector.instanceOf[AppDAO]
      val hisDAO = implicitApp.injector.instanceOf[HistoryAppDAO]
      val payload = app3.payloads.get.head
      val result = await(appDAO.publish(payload.id, payload.lang, payload.os))
      result.isSuccess must beTrue

      val history = await(appDAO.getPayloadHistory(payload.id))

      history must have size 1

      val (key, payloads) = history.filter(_._1 == (payload.lang, payload.os)).head
      payloads must have size 1
      payloads.head.url mustEqual payload.url

    }

    "get payload history of a live app" in new WithApplication{
      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]

      val history = await(appDAO.getPayloadHistory(app1.id.get))
      val payload = app1.payloads.get.head

      history must have size 1

      val (key, payloads) = history.filter(_._1 == (payload.lang, payload.os)).head
      payloads must have size 1
      payloads.head.url mustEqual payload.url

    }


    "change status of a Payload (Sub-status)" in new WithApplication{
      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]

    }

    "change status of a non-existing Payload (Sub-status)" in new WithApplication{
      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]

    }

    "change status of an App (Unpublish)" in new WithApplication{
      Init[I]
      val appDAO = implicitApp.injector.instanceOf[AppDAO]

    }


  }

}
