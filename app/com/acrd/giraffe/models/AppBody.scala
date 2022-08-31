package com.acrd.giraffe.models

import java.sql.Timestamp
import com.acrd.giraffe.common.implicits.JsonFormats.timestampFormat
import com.acrd.giraffe.models.gen.Models._
import play.api.libs.json._
import com.acrd.giraffe.common.Consts._

case class AppBody(id: Option[Long],
                   title: String,
                   author: String,
                   createAt: Option[Timestamp],
                   updateAt: Option[Timestamp],
                   supportContact: Option[String],
                   webServiceIdentification: Option[String],
                   productVersionMap: Option[String],
                   icon: String,
                   rating: Option[RatingBody],
                   facets: Seq[FacetBody],
                   pricePlans: Seq[PriceBody],
                   payloads: Option[Seq[PayloadBody]]) {
  def toTuple = AppBody.toTuple(this)
}

object AppBody {

  import com.acrd.giraffe.common.implicits.Options._
  implicit val formatter = Json.format[AppBody]

  def toTuple(body: AppBody) = {
    implicit val appId = body.id.getOrElse(DummyId)
    val header = AppRow(id = appId, body.createAt.orNull, body.updateAt.orNull, body.title, body.author, body.icon, body.supportContact, body.webServiceIdentification, body.productVersionMap)
    val prices = body.pricePlans.map(_.copy(appId = appId).toPricePlanRow)
    val payloads = body.payloads.map(_.map(_.copy(id = appId)))
    val facets = body.facets.map(FacetBody.toFacetRow).flatMap(f => f).map(_.copy(id = header.id))
    (header, facets, prices, payloads)
  }

  def fromTuple(tuple: (AppRow, Option[RatingRow], Seq[FacetRow], Seq[PricePlanRow], Seq[(PayloadRow, Option[Seq[AttachmentRow]])]),
                live: Boolean = true) = {
    val header = tuple._1
    val prices = tuple._4
    val payloads = tuple._5.map(p => PayloadBody.fromPayloadRowAndMediaRow(p._1, p._2.getOrElse(Seq.empty)))
    val ratingBody = (live, tuple._2) match {
      case (true, Some(r)) => RatingRow.unapply(r).map(t => RatingBody(t._8, t._2, t._3, t._4, t._5, t._6, t._7))
      case _ => None
    }
    val facetBody = if (tuple._3.nonEmpty) FacetBody.fromFacetRows(tuple._3) else Seq.empty
    AppBody(id = header.id, title = header.title, author = header.authorId, createAt = header.createAt, updateAt = header.updateAt,
      supportContact = header.supportContact, webServiceIdentification = header.webServiceIdentification,
      productVersionMap = header.prodVerMap,
      icon = header.icon,
      rating = ratingBody,
      facets = facetBody,
      pricePlans = prices.map(PriceBody.fromPricePlanRow),
      payloads = Some(payloads))
  }

}

