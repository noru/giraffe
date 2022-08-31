package com.acrd.giraffe.common

import com.acrd.giraffe.models._
import com.acrd.giraffe.models.gen.Models
import Models.UserRow
import com.wix.accord.dsl._

object validators {

  implicit val IdValidator = validator[Long] { id =>
    id as "Id" is between(1, Long.MaxValue)
  }

  implicit val IdOptionValidator = validator[Option[Long]] { id =>
    if (id.isDefined) id.get is valid(IdValidator)
  }

  implicit val PayloadValidator = validator[PayloadBody] { p =>
    // stub
  }

  implicit val UserValidator = validator[UserRow] { u =>
    // stub
  }

  implicit val PricePlanValidator = validator[PriceBody] { p =>

  }

  implicit val AppBodyValidator = validator[AppBody] { app =>
    app.icon as "App Icon Url" is notNull
    // comment below validation. since web server still have legacy behaviors, it is no good at current stage
//    FacetBody.RequiredFacets.subsetOf(app.facets.map(_.name).toSet) as s"${FacetBody.RequiredFacets.mkString(", ")} as facets are required" is true
    app.pricePlans have size > 0
    app.pricePlans.each is valid(PricePlanValidator)
    app.payloads.getOrElse(Seq.empty).each is valid(PayloadValidator)
  }

  implicit val CommentBodyValidator = validator[CommentBody] { comment =>

    (comment.rating.isDefined ||
     comment.text.isDefined ||
     comment.status.isDefined ||
     comment.title.isDefined ||
     comment.storeId.isDefined ||
     comment.isVerifiedDownload.isDefined ||
     comment.replyTo.isDefined) as "Nothing to update" is true

    (comment.rating.isDefined && comment.replyTo.isDefined) as "Reply comment cannot have rating" is false

    comment.rating.getOrElse(1) as "Rating is in range 0 ~ 5" is between(0, 5)

  }



}
