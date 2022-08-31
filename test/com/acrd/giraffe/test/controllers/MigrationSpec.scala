package com.acrd.giraffe.test.controllers

import com.acrd.giraffe.base.{BaseContext, TestingContext}
import com.acrd.giraffe.models.AppBody
import com.acrd.giraffe.models.gen.Models.{CommentRow, RatingRow}
import com.acrd.giraffe.test.init.TestingData.App._
import com.acrd.giraffe.test.init.TestingData.Comments
import org.junit.runner._
import org.specs2.runner._
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, JsArray}
import play.api.libs.json.Json.toJson
import play.api.test._
import com.acrd.giraffe.test.init.{Initializer => Init, AppsInitializer => I}


@RunWith(classOf[JUnitRunner])
class MigrationSpec extends PlaySpecification {


  "Migration controller" should {

    def app = new GuiceApplicationBuilder()
        .overrides(bind[BaseContext].to[TestingContext])


    "get id on demand" in new WithApplication(app.build){

      var res = route(FakeRequest(GET, "/migration/howManyIdDoYouWant?2")).get
      status(res) must equalTo(OK)
      val json =  contentAsJson(res)
      json.validate[JsArray].get.value must have size 2

    }

    "migrate a comment" in new WithApplication(app.build) {
      Init[I]
      val insertResult = route(FakeRequest(POST, "/migration/comment").withJsonBody(Json.toJson(Comments.comment1))).get
      status(insertResult) must equalTo(OK)
      val result = contentAsJson(insertResult).as[CommentRow]
      result.id must greaterThan(0L)
      result.createAt mustEqual Comments.comment1.createAt.get
      result.updateAt mustEqual Comments.comment1.updateAt.get
    }

    "insert rating record" in new WithApplication(app.build) {
      Init[I]
      val rating = RatingRow(123L,0,0,0,0,0,0,0.0)
      var res = route(FakeRequest(POST, "/migration/rating").withJsonBody(toJson(rating))).get
      status(res) must equalTo(OK)

    }

    "migrate a preview app" in new WithApplication(app.build){

      Init[I]

      val post = route(FakeRequest(POST, s"/migration/apps").withJsonBody(toJson(app4))).get
      status(post) mustEqual OK

      val get = route(FakeRequest(GET, s"/apps/${app4.id.get}?live=false")).get
      status(get) mustEqual OK
    }

    "migrate a Live app" in new WithApplication(app.build){

      Init[I]

      val post = route(FakeRequest(POST, s"/migration/apps?live=true").withJsonBody(toJson(app4))).get
      val a = contentAsJson(post)

      status(post) mustEqual OK

      val get = route(FakeRequest(GET, s"/apps/${app4.id.get}")).get
      status(get) mustEqual OK
    }

    "call put app api, to update an app content" in new WithApplication(app.build){

      Init[I]

      val nonExistId = 123123123
      var put = route(FakeRequest(PUT, s"/migration/apps/$nonExistId").withJsonBody(toJson(changedApp1))).get
      status(put) must equalTo(NOT_FOUND)

      put = route(FakeRequest(PUT, "/migration/apps/123").withJsonBody(toJson(changedApp1))).get
      val a = contentAsJson(put)
      status(put) must equalTo(OK)
      val body = contentAsJson(put).validate[AppBody].get
      body.payloads.get.head.url must beEqualTo(changedApp1.payloads.get.head.url)

      var getAppById = route(FakeRequest(GET, "/apps/123?live=false")).get
      status(getAppById) must equalTo(OK)
      val json =  contentAsJson(getAppById)
      val parse = json.validate[AppBody]
      parse.isSuccess must beTrue
      parse.get.payloads.get.head.url must beEqualTo(changedApp1.payloads.get.head.url)

    }

  }
}
