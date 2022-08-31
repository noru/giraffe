package com.acrd.giraffe.controllers

import javax.inject.Inject
import com.acrd.giraffe.base.BaseController
import com.acrd.giraffe.common.exceptions.InvalidParameterException
import com.acrd.giraffe.dao.FacetDAO
import com.acrd.giraffe.models.FacetBody.Separator
import com.acrd.giraffe.models.gen.Models.FacetRow
import play.api.libs.concurrent.Execution.Implicits._
import scala.util.{Failure, Success}

class Facets @Inject()(facetDao: FacetDAO) extends BaseController {

  def create(appId: Long, name: Option[String], value: Option[String]) = Action.async {
    val facet = name.getOrElse("").trim + Separator + value.getOrElse("").trim
    if (facet == Separator) {
      throw new InvalidParameterException("name & value cannot be both null")
    }
    facetDao.insert(FacetRow(appId, facet)).map{
      case Success(_) => Ok
      case Failure(e) => onError(e)
    }
  }

  def delete(appId: Long, name: Option[String], value: Option[String]) = Action.async {
    facetDao.delete(FacetRow(appId, name.getOrElse("") + Separator + value.getOrElse(""))).map{
      case Success(_) => Ok
      case Failure(e) => onError(e)
    }
  }

}
