package com.acrd.giraffe.test.controllers

import com.acrd.giraffe.base.{BaseContext, TestingContext}
import com.acrd.giraffe.models.Metadata
import com.acrd.giraffe.test.init.{SettingsInitializer => SI, Initializer => Init}
import com.acrd.giraffe.test.init.TestingData.Settings._
import org.junit.runner._
import org.specs2.runner._
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test._

@RunWith(classOf[JUnitRunner])
class SettingsSpec extends PlaySpecification {


  /******************************* NOTE *****************************
    * Since some of the native sql is not supported by H2, query/delete cannot be tested
    */

  "Settings controller" should {

    def app = new GuiceApplicationBuilder()
        .overrides(bind[BaseContext].to[TestingContext])


    "call insert setting api" in new WithApplication(app.build){

      Init[SI]

      root.children = Some(Seq(leaf1, leaf2, illegalLeaf))
      var response = route(FakeRequest(POST, "/settings")
        .withJsonBody(Json.toJson(root))
        ).get

      status(response) aka "result code of post illegal content" mustEqual BAD_REQUEST

      root.children = Some(Seq(leaf1, leaf2))
      response = route(FakeRequest(POST, "/settings")
          .withJsonBody(Json.toJson(root))
      ).get

      status(response) aka "result code of normal post" mustEqual OK

    }

    "call getAll setting api" in new WithApplication(app.build) {

      Init[SI]
      var response = route(FakeRequest(GET, "/settings")).get
      status(response) mustEqual OK
      var result = contentAsJson(response).validate[Seq[Metadata]].get
      result must have size 1
      response = route(FakeRequest(GET, "/settings?compact=true")).get
      status(response) mustEqual OK
      result = contentAsJson(response).validate[Seq[Metadata]].get
      result.filter(_.value.isDefined) must have size 0

    }
    "call get single setting api" in new WithApplication(app.build) {

      Init[SI]
      var response = route(FakeRequest(GET, "/settings/root2?recursively=false")).get
      status(response) mustEqual OK
      var result = contentAsJson(response).validate[Metadata].get
      result.value mustEqual root2.value

    }
  }
}
