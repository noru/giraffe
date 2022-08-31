package com.acrd.giraffe.test.others

import com.acrd.giraffe.common.Logging
import org.junit.runner._
import org.specs2.runner._
import play.api.test.{PlaySpecification, WithApplication}

@RunWith(classOf[JUnitRunner])
class LoggingSpec extends PlaySpecification {

  val msg = "Just a string message"
  val exception = new Exception("Just a exception")
  val logger = new Logging{}

  "Logging Trait's methods" should {

    "all types of logging method should work" in new WithApplication {

      try{
        logger.trace(msg)
        logger.trace(msg, exception)
        logger.info(msg)
        logger.info(msg, exception)
        logger.debug(msg)
        logger.debug(msg, exception)
        logger.warn(msg)
        logger.warn(msg, exception)
        logger.error(msg)
        logger.error(msg, exception)
      } catch {
        case _: Throwable => false must beTrue
      }

    }

  }
}
