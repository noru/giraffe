package com.acrd.giraffe.modules

import com.google.inject.{Provider, Inject, AbstractModule}
import com.google.inject.name.Names
import com.typesafe.play.redis.RedisCacheApi
import org.sedis.Pool
import play.api.cache.CacheApi
import play.api.{Configuration, Environment}
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

class CacheModule(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure(): Unit = {

    bind(classOf[ClassLoader])
        .toInstance(environment.classLoader)


    if (configuration.getString("app.mode").contains("test")) {

      val cacheApiStub = new CacheApi {
        def set(key: String, value: Any, expiration: Duration): Unit = ()
        def get[T](key: String)(implicit evidence$2: ClassTag[T]): Option[T] = None
        def getOrElse[A](key: String, expiration: Duration)(orElse: => A)(implicit evidence$1: ClassTag[A]): A = orElse
        def remove(key: String): Unit = ()
      }
      bind(classOf[CacheApi])
        .annotatedWith(Names.named("redis"))
        .toInstance(cacheApiStub)

    } else {

      bind(classOf[CacheApi])
          .annotatedWith(Names.named("redis"))
          .toProvider(classOf[RedisProvider]).asEagerSingleton()

    }

  }
}
class RedisProvider @Inject()(pool: Pool, classLoader: ClassLoader) extends Provider[CacheApi] {
  override def get(): CacheApi = new RedisCacheApi("giraffe", pool, classLoader)
}


