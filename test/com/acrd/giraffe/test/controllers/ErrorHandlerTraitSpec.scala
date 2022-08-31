package com.acrd.giraffe.test.controllers

import com.acrd.giraffe.base.ErrorHandler
import org.junit.runner._
import org.specs2.runner._
import play.api.test._
import com.acrd.giraffe.common.exceptions._
import play.api.mvc.Result
import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ErrorHandlerTraitSpec extends PlaySpecification {


  "ErrorHandler trait" should {

    lazy val handler = new ErrorHandler {}
    val e = new Exception("unchecked")
    val e2 = new CheckedException("underlined")

    "handle expected Exceptions" in new WithApplication {

      val result = handler.onError(e2)
      result must beAnInstanceOf[Result]
      result.header.status must be_==(400)
      contentAsString(Future.successful(result)) must contain("underlined")

    }

    "handle unexpected Exceptions" in new WithApplication{

      val result = handler.onError(e)
      result must beAnInstanceOf[Result]
      result.header.status must be_==(500)
      contentAsString(Future.successful(result)) must contain("Unknown Error")

    }

    "handle exception, with shadowing an underlined exception" in new WithApplication {

      val result = handler.onError(e2, e)
      result must beAnInstanceOf[Result]
      result.header.status must be_==(400)
      contentAsString(Future.successful(result)) must contain("underlined")

    }

    "async version" in new WithApplication{

      val e = new Exception("checked")
      val result = handler.onErrorAsync(e)
      result must beAnInstanceOf[Future[_]]
      result.value.get.get.header.status must be_==(500)

    }

    "async version, shadowing" in new WithApplication{

      val result = handler.onErrorAsync(e, e2)
      result must beAnInstanceOf[Future[_]]
      result.value.get.get.header.status must be_==(500)

    }
  }
}
