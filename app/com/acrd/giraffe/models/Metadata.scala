package com.acrd.giraffe.models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._

case class Metadata(id: String, parent: Option[String] = None, name: Option[String] = None, `type`: Option[String] = None, value: Option[String] = None) {
  var children: Option[Seq[Metadata]] = None
}


object Metadata {

  implicit val formats: Format[Metadata] = (
        (__ \ "id").format[String] and
        (__ \ "parent").formatNullable[String] and
        (__ \ "name").formatNullable[String] and
        (__ \ "type").formatNullable[String] and
        (__ \ "value").formatNullable[String] and
        (__ \ "children").lazyFormatNullable(Reads.seq(formats), Writes.seq(formats)) // lazy methods for recursive types
      )(jsonApply, unlift(jsonUnapply))

  /**
   * jsonApply/jsonUnapply:
   * Translate individual values to model(like apply), and vise versa(like unapply). Since children is not a case class field, apply/unapply is
   * not suitable here.
   */
  def jsonApply(id: String, parent: Option[String], name:Option[String], `type`: Option[String], value: Option[String],
           children: Option[Seq[Metadata]]): Metadata = {
    val meta = Metadata(id, parent, name, `type`, value)
    meta.children = children
    meta
  }
  def jsonUnapply(meta: Metadata) = {
    Option((meta.id, meta.parent, meta.name, meta.`type`, meta.value, meta.children))
  }

}