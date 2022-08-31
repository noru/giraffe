package com.acrd.giraffe.controllers

import javax.inject.Inject
import com.acrd.giraffe.base.BaseController
import com.acrd.giraffe.common.auth._
import com.acrd.giraffe.dao.PreviewAppDAO
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.BodyParsers.parse.json
import com.acrd.giraffe.services.AccountService
import scala.concurrent.{Await, Future}

class Application @Inject()(val previewAppDAO: PreviewAppDAO) extends BaseController {

  def stub(opts: Option[String]*) = StackAction(AuthorityKey -> AuthCheck) { implicit request =>
    Ok("index")
  }

  private def AuthCheck(account: Account): Future[Boolean] = {
    Future(new AccountService(StubAccountRepo).hasRole(account, Role.normal))
  }

  def test = Action.async(json) { request =>
    Future(Ok)
  }


}

