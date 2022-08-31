package com.acrd.giraffe.test.others

import com.acrd.giraffe.common.Message
import org.junit.runner._
import org.specs2.runner._
import play.api.libs.json._
import play.api.test.{WithApplication, PlaySpecification}

@RunWith(classOf[JUnitRunner])
class MessageSpec extends PlaySpecification {

  val exception = new Exception("just an exception")

  lazy val messenger = new Message {}

  "Message Trait's message method" should {

    "report a simple message" in new WithApplication{
      val json = messenger.message("statas", "massaga")
      (json \ "status").as[String] mustEqual "statas"
      (json \ "message").as[String] mustEqual "massaga"
    }

    "report a null message" in new WithApplication{
      val json = messenger.message(null, null)
      (json \ "status").get mustEqual JsString(null)
      (json \ "message").get mustEqual null
    }

    "report a simple message with an exception" in new WithApplication {
      val json = messenger.message("statas", "massaga", exception)
      (json \ "exception" \ "type").as[String] mustEqual "java.lang.Exception"
      (json \ "exception" \ "message").as[String] mustEqual "just an exception"
      (json \ "exception" \ "trace").as[String].length must greaterThan(0)
    }

    "report a message with JsValue" in new WithApplication{
      val jsObj = Json.parse("{\"a\":1}")
      val json = messenger.message("bla", jsObj)
      (json \ "message").get mustEqual jsObj
    }

  }
}
