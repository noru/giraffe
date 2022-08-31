package com.acrd.giraffe.models

import play.api.libs.json.Json

case class RatingBody(averageRating: BigDecimal = 0,
                      count1: Short = 0,
                      count2: Short = 0,
                      count3: Short = 0,
                      count4: Short = 0,
                      count5: Short = 0,
                      countSum: Short = 0) {
  def isValid = RatingBody.isValid(this)
}

object RatingBody {

  implicit val ratingFormat = Json.format[RatingBody]

  def isValid(r: RatingBody): Boolean = {
    val vec = Vector(r.count1, r.count2, r.count3, r.count4, r.count5)
    vec.sum == r.countSum &&
        (vec.zipWithIndex.aggregate(0)((a, b) => a + b._1 * (b._2 + 1), _ + _) / r.countSum) == r.averageRating.toDouble
  }

}