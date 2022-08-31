package com.acrd.giraffe.services.cache

import akka.actor.{ActorSystem, Props}
import com.acrd.giraffe.base.AppCacheApi
import com.acrd.giraffe.common.utils.FilterHelper
import com.acrd.giraffe.common.{AppConfig, Logging}
import com.acrd.giraffe.services.cache.BookKeeper._
import play.api.cache.CacheApi
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.libs.Codecs
import play.api.mvc.RequestHeader
import redis.clients.jedis.exceptions.JedisConnectionException
import scala.collection.mutable
import scala.concurrent.duration.Duration._
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util._

object CacheProxy extends AppCacheApi with CacheApi with Logging with AppConfig{

  val INVALID_KEY = ""

  // Singleton, use Redis as sole api provider
  lazy val Singleton: CacheProxy = new CacheProxy(RedisApi)
  def Instance(implicit cacheApi: CacheApi) = new CacheProxy(cacheApi)

  def getVersionSegment(live: Option[Boolean]) = live match {
    case Some(false) => "prev-"
    case _ => "live-"
  }
  def getCacheKey(id: Long, live: Option[Boolean], rh: RequestHeader): String = SkipCache(rh) {
    s"app-${getVersionSegment(live)}$id"
  }
  def getCacheKey(skip: Option[Int],
                  top: Option[Int],
                  orderBy: Option[String],
                  ascending: Option[Boolean],
                  filter: Option[String],
                  live: Option[Boolean],
                  rh: RequestHeader): String = SkipCache(rh) {
    FilterHelper.parse(filter).find(_.name == "id") match {
      case Some(f) => s"collection-id-${getVersionSegment(live)}${f.value}-$skip$top$orderBy$ascending"
      case _ => INVALID_KEY
    }
  }
  def hash(key: String) = Codecs.sha1(key)


  /** Helpers
    */
  private def SkipCache(rh: RequestHeader)(f: => String) =
    if (rh.headers.get(CACHE_CONTROL).contains("no-cache")) INVALID_KEY else f

  /** Delegate CacheApi method statically for global usage
    */
  def set(key: String, value: Any, expiration: Duration): Unit = SafeAccess(Singleton.set _)
  def get[T](key: String)(implicit evidence$2: ClassTag[T]): Option[T] = Singleton.get(key)
  def getOrElse[A](key: String, expiration: Duration)(orElse: => A)(implicit e: ClassTag[A]): A =
    Singleton.getOrElse(key, expiration)(orElse)
  def remove(key: String): Unit = SafeAccess(Singleton.remove _)

  /** A wrapper for cache.get/remove to eliminate exceptions when cache service is not setup or malfunctioning
    */
  def SafeAccess(action: => Unit) = Try(action) match {
    case Failure(e: JedisConnectionException) =>
      if (IsProd || IsStaging) warn("Cache Server is unreachable.", e)
    case Failure(unknown) => error("Unknown Error when accessing cache.", unknown)
    case _ => ()
  }
}

/** A proxy that creates an ActorSystem to handle cache action in a non-blocking way
  */
class CacheProxy(val cacheApi: CacheApi) extends CacheApi with Logging with AppConfig{

  import CacheProxy._

  type Book = mutable.Map[String, mutable.Set[String]]
  val RegistrationBook: Book = mutable.Map.empty[String, mutable.Set[String]]
  val actorSystem = ActorSystem("GiraffeCacheSystem")

  def set(key: String, value: Any, ttl: Duration): Unit = CacheElement(key) match {
      case Valid(k, d) => {
        debug("write cache value")
        getAgent ! Register(k, d, value, ttl)
      }
      case _ => debug("value not set")// do nothing
    }

  def remove(key: String): Unit = getAgent ! Delete(key)

  def get[T](key: String)(implicit e: ClassTag[T]): Option[T] = {
    if (RegistrationBook.contains(key)){
      val cached = cacheApi.get[T](hash(key))
      if (cached.isEmpty) getAgent ! Delete(key)
      cached
    } else None
  }

  def getOrElse[A](key: String, exp: Duration = Inf)(orElse: => A)(implicit e: ClassTag[A]) = get(key).getOrElse(orElse)

  private def getAgent = {
    debug("Get an actor")
    actorSystem.actorOf(Props(classOf[BookKeeper], cacheApi, RegistrationBook))
  }
}
