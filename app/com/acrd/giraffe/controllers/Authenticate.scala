package com.acrd.giraffe.controllers

import com.acrd.giraffe.common.auth.AuthConfigImpl
import jp.t2v.lab.play2.auth.LoginLogout
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Simple authenticate controller to provide a stub login method
 * By default, every user, even anonymous, is logged in through this as a general account
 * Authorities for special resources are checked during runtime
 */
class Authenticate extends Controller with LoginLogout with AuthConfigImpl {

  def login = Action.async { implicit request =>
    gotoLoginSucceeded(request.headers.get("x-appstore-identity").getOrElse("whatever"))
  }

}
