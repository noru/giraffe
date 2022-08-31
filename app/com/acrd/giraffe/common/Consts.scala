package com.acrd.giraffe.common

import com.acrd.giraffe.models.AppStoreTables.BaseEnum
import scala.language.implicitConversions

object Consts {

  val DummyId = 0L
  val QueryLimit = 5000

  object Mode {
    val Prod = "prod"
    val Dev = "dev"
    val Staging = "stage"
  }

  object LogicOperand extends BaseEnum {
    type LogicOperand = Value
    val AND = Value(" and ")
    val OR = Value(" or ")
    val EQ = Value(" eq ")
    val NQ = Value(" nq ")
    val IN = Value(" in ")
    val HAS = Value(" has ")

    implicit def strToLogicOperand(str: String): LogicOperand = strToEnum(str)

  }

}
