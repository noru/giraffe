package com.acrd.giraffe.test.others

import com.acrd.giraffe.models.AppBody
import com.acrd.giraffe.test.init.TestingData.App._
import org.junit.runner._
import org.specs2.runner._
import play.api.test.PlaySpecification
import rapture.json.JsonBuffer
import scala.concurrent.Future
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class UtilsSpec extends PlaySpecification {

  import com.acrd.giraffe.common.implicits.Options._

  "Util class" should {
    import com.acrd.giraffe.common.utils.Util.{await => utilAwait, _}
    import rapture.json.jsonBackends.jawn._
    val str1 = "{\"b\":1,\"a\":\"str\"}"
    val str2 = "{\"b\":2,\"c\":2}"
    val buffer1 = JsonBuffer.parse(str1)
    val buffer2 = JsonBuffer.parse(str2)

    "case class ignoring fields comparison" in {

      case class Test(a: Int, b: String, c: Long, d: AppBody)
      val A = Test(1, "b", 123L, app1)
      val B = Test(1, "b", 1234L, app1)
      val C = Test(1, "b", 123L, app1.copy(id = 123123123L))
      (A === B) must beFalse
      (A !== B) must beTrue

      A.ignore("c") === B must beTrue

      A.ignore("d") === C must beTrue

      A.ignore("a") === C must beFalse

      A.ignore("a", "c") === B must beTrue

      A.ignore("a", "b", "d") === B must beFalse

    }

    "get GUID as Long" in {
      import com.acrd.giraffe.common.utils.IdGenerator.{getIncrementUID, getUID}
      var uid: Long = getUID

      uid must be_>(0L)

      uid = getIncrementUID
      uid must be_>(0L)
      val uid2 = getIncrementUID
      uid2 - 1L must be_==(uid)
    }

    "remove rapture.Json property" in {

      import rapture.json.{Json => RJson, _}
      import RJson._
      import com.acrd.giraffe.common.utils.Util.removeJsonProperties

      val json = json"""{"a": 1, "b": { "c": 3, "e": { "f": "heiheihei" }, "g": "I'm safe" } }"""

      (json \ "b" \ "c").as[Int] mustEqual 3
      (json \ "b" \ "e" \ "f").as[String] mustEqual "heiheihei"
      removeJsonProperties(json, "b.c")
      (json \ "b" \ "c").toString mustEqual "undefined"
      removeJsonProperties(json, "b.e.f")
      (json \ "b" \ "e" \ "f").toString mustEqual "undefined"
      (json \ "a").as[Int] mustEqual 1
      (json \ "b" \ "g").as[String] mustEqual "I'm safe"
    }

    "flatten a tuple" in {
      import TupleFlatten.flatten
      val t1 = (1, ((2, 3.0), 4))

      val f1 = flatten(t1)
      f1 must beAnInstanceOf[(Int, Int, Double, Int)]

      val t2 = (23, ((true, 2.0, "foo"), "bar"), (13, false))
      val f2 = flatten(t2)
      f2 must beAnInstanceOf[(Int, Boolean, Double, String, String, Int, Boolean)]
    }

    "merge json as string" in {
      val str = mergeJsonAsString(str1, str2)
      str must beEqualTo("{\"b\":1,\"a\":\"str\",\"c\":2}")
    }

    "merge json buffer" in {
      val str = mergeJson(buffer1, buffer2).toString
      str must beEqualTo("{\"b\":1,\"a\":\"str\",\"c\":2}")
    }

    "async validator" in  {

      def resultGenerator(b: Boolean): Future[Boolean] = {
        val sleep = Random.nextInt(1000)
        Future.successful{
          Thread.sleep(sleep)
          b
        }
      }

      import ValidationAsync.{all, atLeast => AL}

      var result = await(all(
        resultGenerator(true),
        resultGenerator(false),
        resultGenerator(true)
      ))

      result must beFalse

      result = await(AL(
        resultGenerator(false),
        resultGenerator(false),
        resultGenerator(true)
      ))

      result must beTrue

    }
  }
}
