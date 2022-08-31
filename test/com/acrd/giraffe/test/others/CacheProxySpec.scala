package com.acrd.giraffe.test.others

import com.acrd.giraffe.services.cache.CacheProxy
import org.junit.runner._
import org.mockito.Matchers.{eq => meq}
import org.specs2.mock._
import org.specs2.runner._
import play.api.cache.CacheApi
import play.api.test._
import scala.concurrent.duration._
import CacheProxy.hash

@RunWith(classOf[JUnitRunner])
class CacheProxySpec extends PlaySpecification with Mockito {

  "CacheProxy" should {

    implicit val mockCacheApi = mock[CacheApi]
    mockCacheApi.get(anyString)(any) returns None
    val mockValue = "any serializable object"

    "cache an incoming Live App query" in new WithApplication{

      val cacheService = CacheProxy.Instance
      val key = "app-live-123"
      cacheService.set(key, mockValue)
      there were after(20.millis).one(mockCacheApi).set(meq(hash(key)), meq(mockValue), any[Duration])

    }

    "cache an incoming Preview App query" in new WithApplication{

      val cacheService = CacheProxy.Instance

      val key = "app-prev-123"
      cacheService.set(key, mockValue)
      there were after(20.millis).one(mockCacheApi).set(meq(hash(key)), meq(mockValue), any[Duration])

    }

//    "cache an incoming Live App Collection query" in new WithApplication{
//
//      val cacheService = CacheProxy.Instance
//
//      val key = "collection-id-live-(123,456)-"
//      cacheService.set(key, mockValue)
//      there were after(20.millis).one(mockCacheApi).set(meq(hash(key)), meq(mockValue), any[Duration])
//
//    }
//
//    "cache an incoming Preview App Collection query" in new WithApplication{
//
//      val cacheService = CacheProxy.Instance
//
//      val key = "collection-id-prev-(123,456)-"
//      cacheService.set(key, mockValue)
//      there were after(20.millis).one(mockCacheApi).set(meq(hash(key)), meq(mockValue), any[Duration])
//
//    }

    "not cache unknown query" in new WithApplication{

      val cacheService = CacheProxy.Instance

      val key = "any other key pattern"
      cacheService.set(key, mockValue)

      there were no(mockCacheApi).set(meq(hash(key)), meq(mockValue), any)

    }

    "get by key" in new WithApplication {

      val cacheService = CacheProxy.Instance

      val nonRegKey = "non registered key"

      cacheService.get[String](nonRegKey) must beNone
      there were no(mockCacheApi).get[String](nonRegKey)

      val key = "app-live-1234"
      cacheService.set(key, mockValue)
      Thread.sleep(100)
      cacheService.get[String](key)

      there were after(20.millis).one(mockCacheApi).get[String](hash(key))
    }

    "remove by key" in new WithApplication{

      val cacheService = CacheProxy.Instance

      val key = "any key"
      cacheService.remove(key)

      there were after(20.millis).one(mockCacheApi).remove(hash(key))

    }

//    "correctly put key and its dependents to Register book" in new WithApplication{
//
//      val cacheService = CacheProxy.Instance
//
//      val key = "collection-id-live-(123,456)-"
//      cacheService.set(key, mockValue)
//
//      cacheService.RegistrationBook.contains(key) must beTrue
//      cacheService.RegistrationBook.contains("app-live-123") must beTrue
//      cacheService.RegistrationBook.contains("app-live-456") must beTrue
//      val dependencies = cacheService.RegistrationBook.get("app-live-123").get
//      dependencies must have size 1
//      dependencies must contain(key)
//      cacheService.RegistrationBook.get("app-live-456").get must contain(key)
//
//    }
//
//    "correctly remove key and its subordinates to Register book" in new WithApplication{
//
//      val cacheService = CacheProxy.Instance
//
//      val subordinates = "collection-id-live-(123,456)-"
//      cacheService.set(subordinates, mockValue)
//
//      val key = "app-live-123"
//      cacheService.remove(key)
//      cacheService.RegistrationBook.contains(key) must beFalse
//      cacheService.RegistrationBook.contains("collection-id-live-(123,456)-") must beFalse
//
//      there were after(20.millis).one(mockCacheApi).remove(hash(key))
//      there were after(20.millis).one(mockCacheApi).remove(hash("collection-id-live-(123,456)-"))
//    }

  }
}
