package com.acrd.giraffe.common

import com.acrd.giraffe.base.{AppCacheProxy, ErrorHandler}
import com.acrd.giraffe.common.exceptions.TimeoutException
import com.acrd.giraffe.common.utils.TimeoutPromise
import com.acrd.giraffe.services.cache.CacheElement
import com.wix.accord.Violation
import com.wix.accord.transform.ValidationTransform.{TransformedValidator => TV}
import play.api.data.validation.ValidationError
import play.api.http.HttpVerbs
import play.api.http.{ Status => StatusConsts }
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import scala.util.{Success => Succ}

object CustomActions extends Results with BodyParsers with ErrorHandler{

  val Action = BaseAction

  /**
   * Base action for the project, compose other custom actions based on it if needed
   * Accept only json content
   */
  object BaseAction extends ActionRefiner[Request, Request] with ActionBuilder[Request] {

    override def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = {

      def isContentValid(request: Request[_]) =
          request.body match {
            case b: AnyContentAsRaw if b.raw.size == 0 && request.contentType.isEmpty => true
            case AnyContentAsEmpty => true
            case _: AnyContentAsJson => true
            case _: JsValue => true
            case _ => false
          }

      if (isContentValid(request)) Future(Right(request))
      else Future(Left(NotAcceptable))
    }

    override protected def composeAction[A](action: Action[A]): Action[A] = {
      Seq(
        OnTimeoutAction[A](_),
        LoggingAction[A](_),
        OnErrorAction[A](_),
        InvalidCacheAction[A](_)
        // add other actions
      ).foldLeft(action)( (prevAction, nextAction) => { nextAction.apply(prevAction)})
    }

  }

  /**
   * A timeout handler action
   */
  object OnTimeoutAction extends AppConfig {
    val default = 15000 // millisecond
    val timeout = getConfig[Int]("app.timeout").map(i => if (i > default) default else i).getOrElse(default)
    val timeoutException = new TimeoutException(timeout)
    def timeoutPromise = TimeoutPromise(timeoutException, timeout)
  }
  case class OnTimeoutAction[A](action: Action[A]) extends Action[A] {
    import OnTimeoutAction._
    def apply(request: Request[A]): Future[Result] = {
      val guardian = timeoutPromise
      Future.firstCompletedOf(Seq(action(request), guardian.future)).map{
        case e: TimeoutException => {
          error(s"Request Timeout in $timeout milliseconds. Path: ${request.path}. Query: ${request.queryString}. Headers: ${request.headers}")
          onError(e)
        }
        case result: Result => guardian.cancel; result
      }
    }
    lazy val parser = action.parser
  }

  /**
    * A custom Action wrapped with an error handling method, to catch any unexpected exceptions
    */
  case class OnErrorAction[A](action: Action[A]) extends Action[A] {
    def apply(request: Request[A]): Future[Result] = action(request).recover{ case e => onError(e) }
    lazy val parser = action.parser
  }


  /**
    * To invalid cache after app update action(POST, PUT, DELETE...)
    */
  case class InvalidCacheAction[A](action:Action[A]) extends Action[A] with HttpVerbs with StatusConsts with AppCacheProxy {

    lazy val parser = action.parser
    lazy val updateMethods = Set(POST, PUT, DELETE)
    lazy val AppPath = "^/apps/(\\d+).*".r

    def apply(request: Request[A]): Future[Result] = {

      val result = action(request)
      result.onComplete {
        // if it is an update action on an App entity, invalid related cache
        case Succ(r) if r.header.status == OK && updateMethods.contains(request.method) => {
          request.path match {
            case AppPath(id) => {
              debug(s"Invalid cache by key: $id")
              CacheProxy.remove(CacheElement.PREFIX_SINGLE_PREVIEW + id)
              CacheProxy.remove(CacheElement.PREFIX_SINGLE_LIVE + id) // TODO, live app should be invalid only w/ publish/unpublish action
            }
            case _ => // do nothing
          }
        }
        case _ => // do nothing
      }
      result
    }
  }

  /** Logging interface for incoming requests
    */
  case class LoggingAction[A](action: Action[A]) extends Action[A] {
    def apply(request: Request[A]): Future[Result] = {
      info(s"Received Request: METHOD-${request.method} PATH-${request.path} URI-${request.uri}")
      action(request)
    }
    lazy val parser = action.parser
  }

  /**
   * Simplifies handling incoming JSON body by wrapping validation and returning BadRequest(400) if it fails
   */
  def JsonAction[A](action: A => Result)(implicit reader: Reads[A]): EssentialAction = {
      Action(parse.json) { implicit request =>
        request.body.validate[A].fold(
          valid = { json => action(json) },
          invalid = e => badJson(e)
        )
      }
  }

  /**
   * Async version of JsonAction
   */
  def JsonActionAsync[A](action: A => Future[Result])(implicit reader: Reads[A]): EssentialAction = {
    Action.async(parse.json) { implicit request =>
        request.body.validate[A].fold(
          valid = { json => action(json) },
          invalid = e => Future(badJson(e))
        )
    }
  }

  /**
   * Validation action as simplified procedure for every action that requires customized validation
   */
  def Validate[A](data: A)(action: A => Result)(implicit validator: TV[A]): Result = {
    import com.wix.accord._
    validate(data) match {
      case Success => action(data)
      case Failure(e) => invalidData(e)
    }
  }

  /**
   * Async version of Validate method
   */
  def ValidateAsync[A](data: A)(action: A => Future[Result])(implicit validator: TV[A]): Future[Result] = {
    import com.wix.accord._
    validate(data) match {
      case Success => action(data)
      case Failure(e) => Future(invalidData(e))
    }
  }

  private def badJson(e: Seq[(JsPath, Seq[ValidationError])]) =
    BadRequest(message("Invalid Json Content", JsError.toJson(e)))

  private def invalidData(e: Set[Violation]) =
    BadRequest(message("Invalid Data", Json.toJson({
      e.map(violation => violation.description.getOrElse("Unknown") + " " + violation.constraint)
    })))

}