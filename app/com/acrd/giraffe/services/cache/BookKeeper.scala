package com.acrd.giraffe.services.cache

import akka.actor.Actor
import com.acrd.giraffe.common.Logging
import play.api.cache.CacheApi
import  scala.collection.mutable
import scala.concurrent.duration.Duration
import CacheProxy.SafeAccess


object BookKeeper {
  case class Delete(key: String)
  case class Register(key: String, depends: Vector[String], value: Any, ttl: Duration)
}

class BookKeeper(cacheApi: CacheApi, book: mutable.Map[String, mutable.Set[String]]) extends Actor with Logging{
  import BookKeeper._
  import CacheProxy.hash

  def suicide = {
    debug("actor suicide")
    context.stop(self)
  }
  def receive = {
    case Delete(k) => delete(k)
    case Register(k, d, v, ttl) => register(k, d, v, ttl)
  }

  def delete(key: String): Unit = {
    debug(s"delete key: $key" )
    val hashed = hash(key)
    SafeAccess({
      cacheApi.remove(hashed)
      cacheApi.remove(s"$hashed-etag")
    })
    book.remove(key) match {
      case Some(d) if d.nonEmpty => d.foreach(delete)  // todo, avoid recursion
      case _ => suicide
    }
  }

  def register(key: String, dependencies: Vector[String], v: Any, ttl: Duration): Unit = {

    debug(s"register key: $key, dependencies: $dependencies")

    val record = book.get(key)
    if (record.isEmpty) {
      book.put(key, mutable.Set.empty)
    }

    dependencies.foreach(k => book.get(k) match{
      case Some(sub) if sub != null => sub.add(key)
      case _ => book.put(k, mutable.Set(key))
    })
    SafeAccess(cacheApi.set(hash(key), v, ttl))
    suicide
  }

}
