package com.acrd.giraffe.models

import com.acrd.giraffe.models.gen.Models.FacetRow
import play.api.libs.json.Json
import com.acrd.giraffe.common.Consts.DummyId
import scala.annotation.implicitNotFound

case class FacetBody(name: String, values: Seq[String], appId: Option[Long] = None) {
  def toFacetRow = FacetBody.toFacetRow(this)(appId.getOrElse(DummyId))
}

object FacetBody {

  val Separator = "::"
  val RequiredFacets = Set("version", "category", "productLine")
  val SupportedFacets = RequiredFacets ++ Set("tag", "certification") // todo, not used. apply for facet check

  implicit val formatter = Json.format[FacetBody]

  @implicitNotFound("AppId")
  def toFacetRow(f: FacetBody)(implicit appId: Long): Seq[FacetRow] = {
    val name = f.name
    f.values.map(f => FacetRow(appId, name + Separator + f))
  }

  @implicitNotFound("AppId")
  def toFacetRow(f: Seq[FacetBody])(implicit appId: Long): Seq[FacetRow] = {
    f.map(this.toFacetRow).flatMap(a => a)
  }

  def fromFacetRows(r: Seq[FacetRow]) = {
    r.map(_.id).toSet.ensuring(_.size == 1) // do not support multiple app id
    r.groupBy(_.facet.split(Separator).head).map{
      case (name, facets) => FacetBody(name, facets.map(_.facet.stripPrefix(name + Separator)))
    }.toSeq
  }

}