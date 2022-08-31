package com.acrd.giraffe.base

import java.sql.SQLException
import com.acrd.giraffe.common.CustomActions.BaseAction
import com.acrd.giraffe.common.exceptions.{DuplicateEntryException, IdNotExistsException, CheckedException}
import com.acrd.giraffe.common.auth.AuthConfigImpl
import com.acrd.giraffe.common.{AppConfig, Logging, Message}
import jp.t2v.lab.play2.auth.AuthElement
import play.api.cache.{Cached, Cache, CacheApi}
import play.api.inject.BindingKey
import play.api.mvc._
import scala.concurrent.Future
import scala.language.{implicitConversions, postfixOps}
import scala.util.Try
import com.acrd.giraffe.services.cache.{CacheProxy => CProxy}

class BaseController extends Controller
                        with AuthElement
                        with AuthConfigImpl
                        with ErrorHandler
                        with AppCacheApi
                        with AppConfig {

  // Customized action wrapper
  val Action = BaseAction

  // Customized Cached.class (from Play!), see Play! document or source code
  val Cached = new Cached(CProxy.Singleton) {
    val DefaultTTL = getConfig[Int]("app.cache.defaultTTL").getOrElse(86400)
    def status(key: RequestHeader => String) = super.status(key, 200, DefaultTTL)
  }

}

trait AppCacheApi {

  // default cache api provided by Play! framework
  lazy val EhCacheApi = Cache

  // Redis api, injected. see CacheModule.scala
  lazy val RedisApi: CacheApi =
    play.api.Play.current.injector.instanceOf(BindingKey(classOf[CacheApi], None).qualifiedWith("redis"))

}

trait AppCacheProxy {
  // Giraffe customized cache api proxy with key management
  lazy val CacheProxy = CProxy.Singleton

}

/**
  * Comment exception handler for controllers, provide handling result for general exceptions
  * Use it when no specified exception were thrown within self-context
  */
trait ErrorHandler extends Logging with Message with Results{

  def onError(e: Throwable): Result = {
    e match {
      case _: IdNotExistsException => NotFound(message("Not Found", e.getMessage, e))
      case _: CheckedException => BadRequest(message("Error", e.getMessage, e))
      case _: SQLException if e.getMessage.startsWith("Duplicate entry") => {
          val dee = new DuplicateEntryException(Try(e.getMessage.split("'")(1)).getOrElse(""))
          BadRequest(message("Duplicate entry", dee.getMessage, dee))
      }
      case _: SQLException => {
        error("SQL execution error", e)
        BadRequest(message("DB Action Error", e.getMessage, e))
      }
      case _  => {
        error("On Error", e)
        InternalServerError(message("Unknown Error", e.getMessage, e))
      }
    }
  }

  /** Hide exception that should not expose to consumer, log it, and reply with a cover exception
    */
  def onError(facade: Throwable, underline: Throwable): Result = {
    if (underline != null) error("On Error Underlined", underline)
    onError(facade)

  }

  /** Async version
    */
  def onErrorAsync(e: Throwable): Future[Result] = Future.successful(onError(e))
  def onErrorAsync(e: Throwable, underline: Throwable): Future[Result] = Future.successful(onError(e, underline))

}