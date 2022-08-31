package com.acrd.giraffe.modules

import javax.inject._
import com.acrd.giraffe.common.Logging
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent._

class ErrorHandler @Inject()(
        env: Environment,
        config: Configuration,
        sourceMapper: OptionalSourceMapper,
        router: Provider[Router]
      ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with Logging {


  def onProdServerError(request: RequestHeader, e: Throwable) = {
    error("Server Error Occurred", e)
    Future.successful(InternalServerError)
  }

}