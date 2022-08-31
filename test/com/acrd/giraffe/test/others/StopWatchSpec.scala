package com.acrd.giraffe.test.others

import com.acrd.giraffe.common.utils.StopWatch
import org.junit.runner._
import org.specs2.runner._
import play.api.test.PlaySpecification

@RunWith(classOf[JUnitRunner])
class StopWatchSpec extends PlaySpecification {

  "Stop watch" should {

    "start, stop" in {

      val watch = new StopWatch
      watch.start
      Thread.sleep(10)
      val result = watch.stop
      result must beBetween(10L,13L)

    }

    "start, pause, resume, stop" in {

      val watch = new StopWatch
      watch.start
      Thread.sleep(10)
      watch.pause
      Thread.sleep(10)
      watch.resume
      val result = watch.stop
      result must beBetween(10L,13L)

    }

    "start, laps, stop" in {

      val watch = new StopWatch
      watch.start
      Thread.sleep(10)
      watch.lap
      Thread.sleep(20)
      watch.lap
      Thread.sleep(30)
      watch.lap
      val result = watch.stop
      result must beBetween(60L,75L)
      watch.max must beBetween(30L, 40L)
      watch.min must beBetween(10L, 15L)

    }

    "start, lap with resume, stop" in {

      val watch = new StopWatch
      watch.start
      Thread.sleep(10)
      watch.pause
      Thread.sleep(10)
      watch.resume
      watch.lap
      val result = watch.stop
      result must beBetween(10L,15L)
      watch.max must beBetween(10L, 15L)
      watch.min must beBetween(10L, 15L)
    }

  }
}
