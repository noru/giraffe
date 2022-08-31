package com.acrd.giraffe.modules

import javax.inject.Inject
import com.acrd.giraffe.common.Logging
import play.api.mvc._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.http.HttpFilters
import play.api.mvc.Filter
import play.filters.gzip.GzipFilter

class Filters @Inject()(gzipFilter: GzipFilter, appHeader: AppHeaderFilter) extends HttpFilters {
  def filters = Seq(appHeader, gzipFilter) // append any other filters
}

class AppHeaderFilter extends Filter with Logging{

  import play.api.http.HeaderNames.EXPIRES
  val RESPONSE_TIME = "X-Response-Time"

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis

    nextFilter(requestHeader).map { result =>
      val endTime = System.currentTimeMillis
      val response = (endTime - startTime).toString
      info(s"X-Response-Time: $response, URI: ${requestHeader.uri}")
      result.withHeaders(RESPONSE_TIME -> response, EXPIRES -> "-1")
    }
  }
}
