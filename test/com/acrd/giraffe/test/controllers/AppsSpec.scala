package com.acrd.giraffe.test.controllers

import com.acrd.giraffe.base.{BaseContext, TestingContext}
import com.acrd.giraffe.models.{PayloadHistory, ActionLogBody, AppSeqBody, AppBody}
import com.acrd.giraffe.test.init.TestingData.{ActionLogs, App}
import com.acrd.giraffe.test.init.{Initializer => Init, AppsInitializer => I}
import org.junit.runner._
import org.specs2.runner._
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import play.api.test._
import com.acrd.giraffe.services.TextService

@RunWith(classOf[JUnitRunner])
class AppsSpec extends PlaySpecification {

  import com.acrd.giraffe.test.init.TestingData.App._

  "Apps controller" should {

    def app = new GuiceApplicationBuilder()
        .overrides(bind[BaseContext].to[TestingContext])


    "query an app(preview) by its ID" in new WithApplication(app.build){

      Init[I]
      var getAppById = route(FakeRequest(GET, "/apps/123?live=false")).get

      status(getAppById) must equalTo(OK)
      val json =  contentAsJson(getAppById)
      val parse = json.validate[AppBody]
      parse.isSuccess must beTrue
      parse.get.id must beEqualTo(app1.id)
      parse.get.icon == app1.icon must beTrue
    }

    "query an app(live) by its ID" in new WithApplication(app.build){

      Init[I]
      var getAppById = route(FakeRequest(GET, "/apps/123")).get

      status(getAppById) must equalTo(OK)
      val json =  contentAsJson(getAppById)
      val parse = json.validate[AppBody]
      parse.isSuccess must beTrue
      parse.get.id must beEqualTo(app1.id)
      parse.get.icon == app1.icon must beTrue
    }

    "get all apps(preview)" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?live=false")).get

      status(getApps) must equalTo(OK)
      val result = contentAsJson(getApps).validate[AppSeqBody].get
      result.count must equalTo(3)
      result.apps must have size 3

    }

    "get all apps(live)" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps")).get

      status(getApps) must equalTo(OK)
      val result = contentAsJson(getApps).validate[AppSeqBody].get
      result.count must equalTo(2)
      result.apps must have size 2

    }

    "call get app api with pagination, get app(preview) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?skip=1&top=1&live=false")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val appBodySeq = result.get
      appBodySeq.count must beEqualTo(3)
      appBodySeq.apps must have size 1

    }
    "call get app api with orderBy/descending, get app(preview) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?orderBy=createAt&ascending=false&live=false")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val apps= result.get.apps
      apps must have size 3
      apps.head.createAt.get.compareTo(apps.last.createAt.get) must greaterThan(0)

    }


    "call get app api with orderBy/ascending, get app(preview) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?orderBy=updateAt&live=false")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val apps= result.get.apps
      apps must have size 3
      apps.head.updateAt.get.compareTo(apps.last.updateAt.get) must greaterThan(0)

    }
    "call get app api with id filter, get app(preview) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?orderBy=updateAt&live=false&filter=id in (123, 1234)")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val apps= result.get.apps
      apps must have size 2

    }

    "call get app api with author filter, get app(preview) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?orderBy=updateAt&live=false&filter=authorId eq 'author'")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val apps = result.get.apps
      apps must have size 1

    }

    "call get app api with pagination, get app(live) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?skip=1&top=1")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val appBodySeq = result.get
      appBodySeq.count must beEqualTo(2)
      appBodySeq.apps must have size 1

    }

    "call get app api with orderBy/descending, get app(live) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?orderBy=createAt&ascending=false")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val apps = result.get.apps
      apps must have size 2
      apps.head.createAt.get.compareTo(apps.last.createAt.get) must greaterThanOrEqualTo(0)

    }

    "call get app api with orderBy/ascending, get app(live) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?orderBy=updateAt")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val apps = result.get.apps
      apps must have size 2
      apps.head.updateAt.get.compareTo(apps.last.updateAt.get) must greaterThanOrEqualTo(0)

    }

    "call get app api with id filter, get app(live) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?orderBy=updateAt&filter=id in (123,1234)")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val apps = result.get.apps
      apps must have size 2

    }

    "call get app api with author filter, get app(live) list" in new WithApplication(app.build){

      Init[I]
      var getApps = route(FakeRequest(GET, "/apps?orderBy=updateAt&filter=authorId eq 'author'")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppSeqBody]
      result.isSuccess must beTrue
      val apps = result.get.apps
      apps must have size 1

    }

    "get app by its upgrade code" in new WithApplication(app.build){

      Init[I]
      val code = app1.payloads.get.head.upgradeCode
      var getApps = route(FakeRequest(GET, s"/apps/code/$code")).get

      status(getApps) must equalTo(OK)

      val result = contentAsJson(getApps).validate[AppBody]
      result.isSuccess must beTrue
      result.get.id must beEqualTo(app1.id)

    }

    "get a app's payload history" in new WithApplication(app.build) {
      Init[I]
      val id = App.app1.id.get
      val logs = route(FakeRequest(GET, s"/apps/$id/payloadHistory")).get
      status(logs) mustEqual OK
      contentAsJson(logs).as[Seq[PayloadHistory]] must have size 1
    }

    "get a app's log" in new WithApplication(app.build) {
      Init[I]
      val id = App.app1.id.get
      val logs = route(FakeRequest(GET, s"/apps/$id/actionLog")).get
      status(logs) mustEqual OK
      contentAsJson(logs).as[Seq[ActionLogBody]] must have size 1
    }

    "post a action log for an app" in new WithApplication(app.build) {
      Init[I]

      val id = App.app1.id.get
      var log = route(FakeRequest(POST, s"/apps/$id/actionLog").withJsonBody(Json.toJson(ActionLogs.log1))).get
      status(log) mustEqual OK

      log = route(FakeRequest(POST, s"/apps/348934897/actionLog").withJsonBody(Json.toJson(ActionLogs.log1))).get
      status(log) mustEqual NOT_FOUND

    }

    "create facet for an app" in new WithApplication(app.build) {
      Init[I]

      val id = App.app1.id.get
      val create = route(FakeRequest(POST, s"/apps/$id/facets?name=name&value=value").withJsonBody(Json.toJson(ActionLogs.log1))).get
      status(create) mustEqual OK

      val a = route(FakeRequest(GET, s"/apps/$id?live=false")).get
      contentAsJson(a).as[AppBody].facets.filter(f => f.name == "name" && f.values.contains("value")) must have size 1

    }

    "cannot create facet for non-exist app" in new WithApplication(app.build) {
      Init[I]
      val create = route(FakeRequest(POST, s"/apps/9082475629876/facets?name=name&value=value").withJsonBody(Json.toJson(ActionLogs.log1))).get
      status(create) mustEqual NOT_FOUND
    }

    "delete facet of an app" in new WithApplication(app.build) {
      Init[I]

      val id = App.app1.id.get
      val name = App.app1.facets.head.name
      val value = App.app1.facets.head.values.head
      var del = route(FakeRequest(DELETE, s"/apps/$id/facets?name=$name&value=$value").withJsonBody(Json.toJson(ActionLogs.log1))).get
      status(del) mustEqual OK

      val a = route(FakeRequest(GET, s"/apps/$id?live=false")).get
      contentAsJson(a).as[AppBody].facets.filter(f => f.name == name && f.values.contains(value)) must have size 0

      del = route(FakeRequest(DELETE, s"/apps/$id/facets?name=$name&value=$value").withJsonBody(Json.toJson(ActionLogs.log1))).get
      status(del) mustEqual NOT_FOUND

    }

    "create a new payload for an App(Preview)" in new WithApplication(app.build){

      Init[I]

      var post = route(FakeRequest(POST, "/apps/123/payloads").withJsonBody(toJson(app1.payloads.get.head.copy(lang = TextService.fr))))
          .get

      status(post) must equalTo(OK)
    }

    "create a new payload for an App(Preview)" in new WithApplication(app.build){

      Init[I]

      var post = route(FakeRequest(POST, "/apps/123/payloads").withJsonBody(toJson(app1.payloads.get.head.copy(lang = TextService.fr))))
        .get

      status(post) must equalTo(OK)
    }

    "update a payload" in new WithApplication(app.build){

      Init[I]

      val id = app1.id.get
      val payload = app1.payloads.get.head
      val lang = payload.lang.code
      val os = payload.os
      var put = route(FakeRequest(PUT, s"/apps/$id/payloads/$lang/$os").withJsonBody(toJson(app1.payloads.get.head.copy(title = "changed"))))
        .get
      status(put) must equalTo(OK)

      val get = route(FakeRequest(GET, s"/apps/${app1.id.get}?live=false")).get
      val json =  contentAsJson(get)
      val parse = json.validate[AppBody]
      parse.isSuccess must beTrue
      parse.get.payloads.get.head.title must beEqualTo("changed")



    }

    "delete a payload of an App(Preview)" in new WithApplication(app.build){

      Init[I]

      var post = route(FakeRequest(DELETE, "/apps/123/payloads/en/mac")).get

      status(post) must equalTo(OK)
    }

    "publish a payload for an preview App" in new WithApplication(app.build){

      Init[I]
      var post = route(FakeRequest(PUT, "/apps/12345/payloads/en/mac?status=live")).get
      status(post) must equalTo(OK)

      var getAppById = route(FakeRequest(GET, "/apps/12345")).get
      status(getAppById) mustEqual OK
      contentAsJson(getAppById).validate[AppBody].get.payloads.get.exists(_.lang == TextService.en) must beTrue

    }

    "unpublish a payload of an Live App" in new WithApplication(app.build){

      Init[I]
      var post = route(FakeRequest(PUT, "/apps/123/payloads/en/mac?status=unpublished")).get
      status(post) must equalTo(OK)
    }

    "change a payload's sub status" in new WithApplication(app.build){

      Init[I]
      route(FakeRequest(POST, "/apps/123/payloads").withJsonBody(toJson(app1.payloads.get.head.copy(lang = TextService.fr))))
      Thread.sleep(50)
      var post = route(FakeRequest(PUT, "/apps/123/payloads/fr/mac?status=resubmission")).get
      status(post) must equalTo(OK)
    }

    "call post app api, to create an app content" in new WithApplication(app.build){

      Init[I]

      var post = route(FakeRequest(POST, "/apps").withJsonBody(toJson(app2)))
          .get
      status(post) must equalTo(OK)
      val json =  contentAsJson(post)
      val parse = json.validate[AppBody]
      parse.isSuccess must beTrue

    }

    "call put app api, to update an app content" in new WithApplication(app.build){

      Init[I]

      val post = route(FakeRequest(PUT, s"/apps/${app1.id.get}").withJsonBody(toJson(app1.copy(title = "changed"))))
        .get
      status(post) must equalTo(OK)
      var json =  contentAsJson(post)
      var parse = json.validate[AppBody]
      parse.isSuccess must beTrue

      val get = route(FakeRequest(GET, s"/apps/${app1.id.get}?live=false")).get
      json =  contentAsJson(get)
      parse = json.validate[AppBody]
      parse.isSuccess must beTrue
      parse.get.title must beEqualTo("changed")

    }


    "call delete app api, to delete an app by its ID" in new WithApplication(app.build){

      Init[I]

      var deleteById = route(FakeRequest(DELETE, "/apps/123")).get

      status(deleteById) must equalTo(OK)

      var getAppById = route(FakeRequest(GET, "/apps/123?live=false")).get

      status(getAppById) must equalTo(NOT_FOUND)

      deleteById = route(FakeRequest(DELETE, "/apps/123")).get

      status(deleteById) must equalTo(NOT_FOUND)

    }

  }
}
