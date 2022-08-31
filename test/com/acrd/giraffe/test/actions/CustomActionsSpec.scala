package com.acrd.giraffe.test.actions

import com.acrd.giraffe.models.AppBody
import com.acrd.giraffe.test.init.TestingData.App._
import play.api.libs.json.Json
import play.api.mvc._
import com.acrd.giraffe.common.CustomActions._
import play.api.test.{WithApplication, FakeRequest, PlaySpecification}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.wix.accord.dsl._

class CustomActionsSpec extends PlaySpecification {

  val action: AppBody => Result = { a =>
    if (a == app1) Ok else InternalServerError
  }

  val actionAsync: AppBody => Future[Result] = { a =>
    if (a == app1) Future(Ok) else Future(InternalServerError)
  }

  implicit val validatorForTest = validator[AppBody]{ a =>
    a.id == app1.id as "App Id matched" is true
  }

  "A com.acrd.giraffe.base action" should {
    "can filter the content body" in new WithApplication {

      val base = BaseAction{ Ok }

      val goodRequest = FakeRequest(POST, "/").withJsonBody(Json.parse("""{ "field": "value" }"""))
      var result = call(base, goodRequest)
      status(result) aka "OK: good json body" mustEqual OK

      val goodRequest2 = FakeRequest(POST, "/").withHeaders((CONTENT_TYPE, "")).withBody(new Array[Byte](0))
      result = call(base, goodRequest2)
      status(result) aka "OK: raw type but length = 0" mustEqual OK

      val goodRequest3 = FakeRequest(POST, "/")
      result = call(base, goodRequest3)
      status(result) aka "OK: no body, no content type" mustEqual OK

      val badRequest = FakeRequest(POST, "/").withBody("123")
      result = call(base, badRequest)
      status(result) aka "BAD: text content" mustEqual NOT_ACCEPTABLE

      val badRequest2 = FakeRequest(POST, "/").withHeaders(("Content-Type", "Not/Json"))
      result = call(base, badRequest2)
      status(result) aka "BAD: content other than application/json" mustEqual NOT_ACCEPTABLE

      val badRequest3 = FakeRequest(POST, "/").withHeaders(("Content-Type", "application/json")).withBody("invalidJson*&%!(*@")
      result = call(base, badRequest3)
      status(result) aka "BAD: invalid json" mustEqual BAD_REQUEST

    }
  }

  "A json action" should {
    "can validate a JSON body" in new WithApplication {

      val jsonAction = JsonAction[AppBody](action)

      val badRequest = FakeRequest(POST, "/").withJsonBody(Json.parse("""{ "field": "value" }"""))
      var result = call(jsonAction, badRequest)
      status(result) mustEqual BAD_REQUEST

      val goodRequest = FakeRequest(POST, "/").withJsonBody(Json.toJson(app1))
      result = call(jsonAction, goodRequest)
      status(result) mustEqual OK

    }
  }

  "A json async action" should new WithApplication {
    "can validate a JSON body" in {

      val jsonAction = JsonActionAsync[AppBody](actionAsync)

      val badRequest = FakeRequest(POST, "/").withJsonBody(Json.parse("""{ "field": "value" }"""))
      var result = call(jsonAction, badRequest)
      status(result) mustEqual BAD_REQUEST

      val goodRequest = FakeRequest(POST, "/").withJsonBody(Json.toJson(app1))
      result = call(jsonAction, goodRequest)
      status(result) mustEqual OK

    }
  }

  "A custom validator" should {
    "can validate a model" in {

      var result = Validate[AppBody](app1)(action)
      result.header.status mustEqual OK

      result = Validate[AppBody](app2)(action)
      result.header.status mustEqual BAD_REQUEST
      contentAsString(Future(result)).contains("App Id matched") mustEqual true

    }
  }

  "A custom validator (async)" should {
    "can validate a model" in {

      var result = await(ValidateAsync[AppBody](app1)(actionAsync))
      result.header.status mustEqual OK

      result = await(ValidateAsync[AppBody](app2)(actionAsync))
      result.header.status mustEqual BAD_REQUEST
      contentAsString(Future(result)).contains("App Id matched") mustEqual true

    }
  }

  "Timeout Action" should {

    "return an error if time limit is reached" in new WithApplication {

      val base = BaseAction{ Thread.sleep(7000); Ok }
      val request = FakeRequest(GET, "/")
      val result = call(base, request)
      status(result) aka "Timeout response" mustEqual INTERNAL_SERVER_ERROR

    }

    "don't block normal request" in new WithApplication {

      val base = BaseAction{ Ok }
      val request = FakeRequest(GET, "/")
      val result = call(base, request)
      status(result) aka "Normal response" mustEqual OK

    }
  }

}