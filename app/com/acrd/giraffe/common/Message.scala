package com.acrd.giraffe.common

import play.api.libs.json.{Json, JsString, JsValue}
import scala.language.implicitConversions

trait Message extends AppConfig{

  /**
   * Return a writable JsValue as a message container
   */
  def message[T <: JsValue](status: String, message: T, e: Throwable) = {

    implicit def str2JsValue(s: String): JsString = JsString(s)

    val base = Map[String, JsValue]("status" -> status, "message" -> message)

    val msgMap = e match {
      case ex if e != null && !IsProd =>
        base + ("exception" -> Json.toJson(Map("type" -> ex.getClass.getName,
          "message" -> ex.getMessage,
          "trace" -> ex.getStackTrace.mkString("\n"))))
      case _ => base
    }
    Json.toJson(msgMap)
  }
  /**
   * Overloaded method
   */
  def message(status: => String, msg: => String, e: => Throwable = null): JsValue =
    message(status, JsString(msg), e)
  /**
   * Overloaded method
   */
  def message[T <: JsValue](status: => String, msg: => T): JsValue =
    message(status, msg, null)


}
