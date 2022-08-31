package com.acrd.giraffe.test

import org.junit.runner._
import org.specs2.runner._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends PlaySpecification {

  "Application" should {

    "render the health page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "application/xml")
      contentAsString(home) must contain ("<health service=\"giraffe\"")
    }

  }
}
