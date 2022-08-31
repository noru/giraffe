package com.acrd.giraffe.test.others

import com.acrd.giraffe.common.AppConfig
import org.junit.runner._
import org.specs2.runner._
import play.api.test.{PlaySpecification, WithApplication}

@RunWith(classOf[JUnitRunner])
class AppConfigSpec extends PlaySpecification {


  "AppConfig Trait" should {

    "all method of AppConfig Trait should work" in new WithApplication {

      val config = new AppConfig{}

      config.hasConfig("blabla") must beFalse

      config.hasConfig("test") must beTrue

      config.getConfig[String]("test.string") must beAnInstanceOf[Option[String]]

      config.getConfig[Int]("test.int") must beAnInstanceOf[Option[Int]]

      config.getConfig[Boolean]("test.bool") must beAnInstanceOf[Option[Boolean]]

    }

  }
}
