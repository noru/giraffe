package com.acrd.giraffe.common.utils

import com.acrd.giraffe.common.Consts.LogicOperand._
import com.acrd.giraffe.common.utils.Util.lc2lu
import com.acrd.giraffe.common.utils.Util.StringSanitizer.StringSanitizer

case class Filter(name: String, value: String, op: LogicOperand = EQ)

case class SubFilter(filter: Filter, table: String, appIdFieldName: Option[String] = None) {
  def absoluteIdName = table + "." + appIdFieldName.getOrElse("id")
}

object FilterHelper {

  val separator = Seq(AND, OR) // currently do not support OR
  val operand = Seq(EQ, NQ, HAS, IN)

  def parse(raw: Option[String]): Seq[Filter] = {

    val sanitized = raw.map(_.mergeSpaces.removeSeparatorSpace())
    implicit val toReg: TraversableOnce[_] => String =  _.mkString("(", "|", ")")

    // do not convert facet name
    def filterFacetName(str: String) = if (str.startsWith("$")) str else lc2lu(str)

    sanitized match {
      case Some(str) => str.split(separator).map{ facetStr =>
          val regStr: String = operand
          facetStr.split(regStr) match {
            case i if i.length == 2 => Some(Filter(filterFacetName(i(0)), i(1).trim, facetStr.trim.stripPrefix(i(0)).stripSuffix(i(1))))
            case _ => None
          }
      }.withFilter(_.isDefined).map(_.get)
      case _ => Seq.empty
    }

  }

}


