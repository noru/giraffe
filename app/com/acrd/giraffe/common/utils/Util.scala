package com.acrd.giraffe.common.utils

import jawn.ast.JObject
import rapture.json.{JsonBuffer, Json}
import rapture.json.jsonBackends.jawn._
import scala.annotation.tailrec
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{ExecutionContext, Await, Future}
import com.google.common.base.CaseFormat._
import scala.util.Try

object Util {

  /**
   * Short-hand method for Await.result with infinite duration
   */
  def await[T](future : Future[T]): T = {
    Await.result(future, Inf)
  }

  /**
   * like groupBy method, but preserve existing order of the input seq
   */
  def orderedGroupBy[T, P](seq: Seq[T])(f: T => P): Seq[(P, Seq[T])] = {
    @tailrec
    def accumulator(seq: Seq[T], f: T => P, res: List[(P, Seq[T])]): Seq[(P, Seq[T])] = seq.headOption match {
      case None => res.reverse
      case Some(h) => {
        val key = f(h)
        val subSeq = seq.takeWhile(f(_) == key)
        accumulator(seq.drop(subSeq.size), f, (key -> subSeq) :: res)
      }
    }
    accumulator(seq, f, Nil)
  }

  /**
   * Overload for mergeJson
   */
  def mergeJson(json1: String, json2: String): Json = {
    mergeJson(Json.parse(json1), Json.parse(json2))
  }

  /**
   * Overload for mergeJson, return raw string
   */
  def mergeJsonAsString(json1: String, json2: String): String = {
    mergeJson(json1, json2).toString
  }

  /**
   * Wrap merge feature from rapture.json, favors left side value if has any conflict
   */
  def mergeJson(json1: Json, json2: Json): Json = json2 ++ json1

  /**
   * like mergeJson, but use JsonBuffer instead of Json
   */
  def mergeJson(json1: JsonBuffer, json2: JsonBuffer): JsonBuffer = json2 ++ json1

  /** Remove properties of an target Json(rapture) by name, support deep path: "path.to.node"
    * Note: Not thread-safe!
    */
  def removeJsonProperties(target: Json, name: String*) = {

    val pathChar = '.'
    def remove(jObject: JObject, name: String): Unit = {
        val subNode = name.indexOf(pathChar)
        if (subNode < 0) {
          jObject.remove(name)
        } else remove(jObject.get(name.substring(0, subNode)).asInstanceOf[JObject], name.substring(subNode + 1))

    }
    val jo = target.$root.value.asInstanceOf[JObject]
    name.foreach(remove(jo, _))
  }

  /**
    * Flatten a tuple of whatever inner structure
    */
  object TupleFlatten {
    import shapeless._
    import syntax.std.tuple._
    import ops.tuple.FlatMapper

    trait LowPriorityFlatten extends Poly1 {
      implicit def default[T] = at[T](Tuple1(_))
    }
    object flatten extends LowPriorityFlatten {
      implicit def caseTuple[P <: Product](implicit fm: FlatMapper[P, flatten.type]) =
        at[P](_.flatMap(flatten))
    }
  }

  /**
    * Perform case class comparison with some properties ignored
    */
  implicit class CaseClassComparison[T <: Product with Serializable](val left: T) {

    var value: Array[_] = Array.empty
    var fields: Set[String] = Set.empty
    def ignore(field: String*): this.type = {
      if (field.isEmpty) return this
      fields = field.toSet
      value = getComparedValue(left)
      this
    }
    def === (right: T): Boolean = Try{
      value sameElements getComparedValue(right)
    }.getOrElse(false)

    def !== (right: T): Boolean = ! ===(right)

    private def getComparedValue(t: T): Array[_] =
      t.getClass.getDeclaredFields.filter(f => !fields.contains(f.getName)).map(f => {f.setAccessible(true); f.get(t)})
  }

  /** Provide a validation mechanism with Future
    */
  object ValidationAsync {

    /** All validators must return `true` to pass the validation
      */
    def all(validators: Future[Boolean]*)(implicit ec: ExecutionContext): Future[Boolean] = {
      Future.find(validators){ !_  }.map { _.isEmpty }
    }

    /** At least one of the validators returns `true`
      */
    def atLeast(validators: Future[Boolean]*)(implicit ec: ExecutionContext): Future[Boolean] = {
      Future.find(validators)( b => b ).map { _.nonEmpty }
    }

  }

  /** lowerCamelString -> lower_camel_string
    */
  def lc2lu(str: String) = LOWER_CAMEL.to(LOWER_UNDERSCORE, str).trim

  /** lower_camel_string -> lowerCamelString
    */
  def lu2lc(str: String) = LOWER_UNDERSCORE.to(LOWER_CAMEL, str).trim


  object StringSanitizer {
    implicit class StringSanitizer(str: String) {

      /** merge multi-space into one
        */
      def mergeSpaces(): String = str.replaceAll("\\s+", " ")

      /** remove spaces from separator (comma as default)
        */
      def removeSeparatorSpace(separator: String = ","): String = str.replaceAll("\\s*"+ separator +"\\s+", separator)

      def removeLineBreak(): String = str.replaceAll("\r", "").replaceAll("\n", "")

      def stripBrackets(pre: String = "(", end: String = ")"): String = str.stripPrefix(pre).stripSuffix(end)

    }
  }

}
