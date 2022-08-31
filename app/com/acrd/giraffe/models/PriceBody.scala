package com.acrd.giraffe.models

import java.sql.Timestamp
import com.acrd.giraffe.common.Consts.DummyId
import com.acrd.giraffe.models.gen.Models.PricePlanRow
import play.api.libs.json.Json
import scala.language.implicitConversions
import scala.math.BigDecimal
import com.acrd.giraffe.common.implicits.JsonFormats.timestampFormat

/** A case class to project PricePlanRow, which can hide some of the fields(id, timestamp) during json de-/serialization
  */
case class PriceBody(id: Option[Long],
                     createAt: Option[Timestamp],
                     appId: Option[Long],
                     `type`: String = "one_time",
                     gateway: Option[String] = None,
                     totalPrice: BigDecimal = BigDecimal(0.00),
                     unitPrice: BigDecimal = BigDecimal(0.00),
                     discount: Option[BigDecimal] = Some(BigDecimal(0.00)),
                     currency: String = "USD",
                     restriction: Option[String] = None,
                     description: Option[String] = None) {
  def toPricePlanRow = PriceBody.toPricePlanRow(this)
}


object PriceBody {

  implicit val formatter = Json.format[PriceBody]
    // TODO, use shapeless to scrap the boilerplate!!!
  implicit def toPricePlanRow(body: PriceBody): PricePlanRow = {

    PricePlanRow(
      id = body.id.getOrElse(DummyId),
      createAt = body.createAt.orNull,
      appId = body.appId.getOrElse(DummyId),
      `type` = body.`type`,
      gateway = body.gateway,
      totalPrice = body.totalPrice,
      unitPrice = body.unitPrice,
      discount = body.discount,
      currency = body.currency,
      restriction = body.restriction,
      description = body.description
    )
  }

  implicit def fromPricePlanRow(row: PricePlanRow): PriceBody = {
    import com.acrd.giraffe.common.implicits.Options._
    PriceBody(
      id = row.id,
      createAt = row.createAt,
      appId = row.appId,
      `type` = row.`type`,
      gateway = row.gateway,
      totalPrice = row.totalPrice,
      unitPrice = row.unitPrice,
      discount = row.discount,
      currency = row.currency,
      restriction = row.restriction,
      description = row.description
    )
  }
}