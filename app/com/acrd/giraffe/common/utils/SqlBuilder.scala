package com.acrd.giraffe.common.utils

import com.acrd.giraffe.common.{Logging, Consts}
import com.acrd.giraffe.common.utils.Util.lc2lu
import com.acrd.giraffe.common.Consts.LogicOperand._
import com.acrd.giraffe.dao.AppDAO.SortOption.SortOption
import scala.language.postfixOps
import com.acrd.giraffe.common.utils.Util.StringSanitizer.StringSanitizer
import SqlBuilder._

trait SqlConvertible {
  def q: String
  def count: String = throw new NotImplementedError
}

abstract class Condition extends SqlConvertible

object SqlBuilder{

  val TextAttribute = Set("title")
  val RootAttributes = Set("author_id", "title", "id")
  val SubAttributes = Map(
    "review_status" -> "payload",
    "total_price" -> "price_plan",
    "lang" -> "payload"
  )

  val TableIdMap = Map(
    "price_plan" -> "app_id"
  )
  val DefaultSkip = 0
  val DefaultTop = Consts.QueryLimit
  val DefaultOrder = "update_at"

  val SqlAND = " and "
  val SqlON = " on "
  val SqlEQ = " = "
  val SqlInnerJoin = " inner join "

  val HeaderTable = "app"
  val FacetTable = "facet"
  val LiveTableSuffix = "_live"
  val FacetIdColumn = "id"
  val RelevantPrefix = "relevance_"

  def tableSuffix(implicit live: Boolean) = if (live) LiveTableSuffix else ""

  def OperandString(op: LogicOperand) = op match {
    case NQ => "!="
    case IN => "in"
    case HAS => "in"
    case _ => "="
  }

  /** Since slick does not support pre-compiled inSet query, and normal Query is relatively slow, so
    * create a method to generate plain sql.
    * Note: Due to the nature of "in" operator of SQL, there is a threshold value where the results pass this value,
    * the inSet query is slower than the whole table query. Under this circumstance better off with a whole table select
    */
  def InSetQuery(table: String, on: String, set: Set[Long], fields: Set[String] = Set.empty) ={

    require(set.nonEmpty, "'in' Condition cannot be empty")
    val threshold = 1000 // most of the cases are < 100. Adjust it if needed

    val f = if (fields.nonEmpty) fields.mkString(",") else "*"
    val base = s"select $f from $table"
    val where = if (set.size > threshold) "" else {
      val cond = set.mkString("(",",",")")
      s" where $on in $cond"
    }
    base + where
  }
}

class SqlBuilder(implicit val live: Boolean = true) {

  /**
    * Complete SQL sample:
    *
    *   select * from app
    * where
    * id in (
    * select facet_query.id from (
    * select id from facet
    * where
    * facet in ('version', 'productLine')
    * group by id
    * having count(id) = 4
    * ) as facet_query
    * inner join (
    * select id from facet where facet in ('tag') group by id having count(id) = 1
    * ) as sub_facet on sub_facet.id = facet_query.id
    * inner join (
    * select payload.id from payload
    * inner join price_plan on price_plan.app_id = payload.id
    * inner join attachment on attachment.app_id = payload.id
    * where payload.review_status = 'draft' and price_plan.discount = 0.0
    * ) as sub on sub.id = facet_query.id
    * )
    * and
    * match (title) against ('old pass')
    * and
    * author_id = '1'
    * order by create_at limit 10 offset 0;
    */

  private var baseSqlWrapper = new SqlWrapper

  def withFilters(filters: Filter*): SqlBuilder = {
    baseSqlWrapper = baseSqlWrapper.copy(filters = filters)
    this
  }

  def withParameters(skip: Option[Int] = None,
                     top: Option[Int] = None,
                     orderBy: Option[SortOption] = None,
                     ascending: Option[Boolean] = None) =  {
    baseSqlWrapper = baseSqlWrapper.copy(skip = skip, top = top, orderBy = orderBy, ascending = ascending)
    this
  }

  lazy val querySql = baseSqlWrapper.q
  lazy val countSql = baseSqlWrapper.countSql
  def q = querySql
  def count = countSql

}

case class SqlWrapper(skip: Option[Int] = None,
                      top: Option[Int] = None,
                      orderBy: Option[SortOption] = None,
                      ascending: Option[Boolean] = None,
                      filters: Seq[Filter] = Seq.empty
                     )(implicit live: Boolean) extends SqlConvertible with Logging{

  val textFilters = filters.filter(f ⇒ TextAttribute.contains(f.name))
  val table = HeaderTable + tableSuffix
  val whereWrapper = WhereClauseWrapper(filters)
  val relevanceClause = textFilters.nonEmpty match {
    case true ⇒ textFilters.map(f ⇒ TextCondition(f).q + s" as $RelevantPrefix${f.name}").mkString(",", ",", "")
    case _ ⇒ ""
  }
  val relevanceOrder = textFilters.nonEmpty match {
    case true ⇒ s"${textFilters.map(RelevantPrefix + _.name).mkString(" + ")} desc,"
    case _ ⇒ ""
  }
  val order = ascending match {
    case Some(false) => "desc"
    case _ => "asc"
  }

  val orderClause = (skip, top, orderBy) match {
    case (Some(0), Some(i), None) if i <= 0 || i >= 5000 => ""  // when not requiring pagination and no explicit order specified, no order clause
    case _ => s"order by $relevanceOrder ${orderBy.map(o => lc2lu(o.toString)).getOrElse(DefaultOrder)} $order"
  }

  def countSql = {
    val sql = s"""select count(*) from $table
        |${whereWrapper.q}""".stripMargin.mergeSpaces.removeLineBreak
    debug(s"Sql generated: $sql")
    sql
  }
  def q: String = {
    val sql = s"""select * $relevanceClause from $table
        |${whereWrapper.q}
        |$orderClause
        |limit ${top.getOrElse(DefaultTop)}
        |offset ${skip.getOrElse(DefaultSkip)}""".stripMargin.mergeSpaces.removeLineBreak
    debug(s"Sql generated: $sql")
    sql
  }

}

case class WhereClauseWrapper(filters: Seq[Filter] = Seq.empty)(implicit live: Boolean) extends SqlConvertible{


  val rootFilters = filters.filter( f => RootAttributes.contains(f.name))
  val subFilters = filters.filter( f => SubAttributes.contains(f.name)).map(f =>{
    val table = SubAttributes.getOrElse(f.name, f.name) + tableSuffix
    val idColumn = TableIdMap.get(table)
    SubFilter(f, table, idColumn)
  })
  val facetFilters = filters.filter( f => f.name.startsWith("$") )
  val derived = if (facetFilters.nonEmpty) "f0" else "sub"

  private def hasIdClause = facetFilters.nonEmpty || subFilters.nonEmpty

  def rootClause = toWhere(rootFilters)

  def idClause = hasIdClause match {
    case true => s"id in (select $derived.id from " + idSubQuery + ")"
    case false => ""
  }

  private def toWhere(filters: Seq[Filter]) = filters.map{
    case f if TextAttribute.contains(f.name) => TextCondition(f)
    case f => NormalCondition(f)
  }.map(_.q)

  private def idSubQuery = {
    facetFilters.zipWithIndex.map{
      case (f, i) => {
        val derivedName = "f" + i
        "(" + FacetCondition(f).q + s") as $derivedName" + (if (i > 0) s" on $derivedName.id = f0.id" else "")
      }
    }.mkString(SqlInnerJoin) + subFilterQuery

  }

  private def subFilterQuery = if (subFilters.isEmpty) "" else {

    val head = subFilters.head
    val first = s"select ${head.absoluteIdName} from ${head.table} "

    // inner join once for each table
    val tail = subFilters.dropWhile(_.table == head.table).groupBy(_.table)
    val others = tail.map{
      case (table, f) => SqlInnerJoin + table + SqlON + f.head.absoluteIdName + SqlEQ + head.absoluteIdName
    }.mkString(" "," "," ")

    val conditions = subFilters.map(_.filter)
    val whereClause = s"where ${toWhere(conditions).mkString(SqlAND)}"
    val on = if (facetFilters.nonEmpty) s"on sub.${head.appIdFieldName.getOrElse("id")} = f0.id" else ""
    val pre = if (facetFilters.nonEmpty) SqlInnerJoin else ""

    pre + " (" + first + others + whereClause + ") as sub " + on
  }

  def q: String = {

    val whereConditions = Seq(rootClause.mkString(SqlAND), idClause).filter(_ != "")

    if (filters.nonEmpty) "where " + whereConditions.mkString(SqlAND) else ""
  }
}

case class TextCondition(f: Filter) extends Condition {
  override def q: String = s"match (${f.name}) against (${f.value.stripBrackets()} in boolean mode)"
}

case class NormalCondition(f: Filter) extends Condition {
  override def q: String = {
    s"${f.name} ${OperandString(f.op)} ${f.value}"
  }
}

case class FacetCondition(f: Filter)(implicit live: Boolean) extends Condition with Logging {

  val facetTable = FacetTable + tableSuffix

  val name = f.name.stripPrefix("$")
  val values = f.value.stripPrefix("(").stripSuffix(")").split(",").map("'" + name + "::" + _.stripPrefix("'"))
  val countCondition = f.op match {
    case HAS => "= " + values.length
    case _ => "> 0"
  }
   def q: String = {
     val sql = s"""
        |select $FacetIdColumn
        |from $facetTable
        |where
        |facet in ${values.mkString("(", ",", ")")}
        |group by $FacetIdColumn
        |having count($FacetIdColumn) $countCondition
        |""".stripMargin.mergeSpaces.removeLineBreak
     debug(s"Sql generated: $sql")
     sql
   }

}

