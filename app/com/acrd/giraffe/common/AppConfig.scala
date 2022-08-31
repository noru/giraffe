package com.acrd.giraffe.common

import play.api.Play.{configuration, current}

trait AppConfig {
  val config = configuration

  abstract class Getter[T] {
    def get(path: String): Option[T]
  }

  object Getter {
    implicit val stringGetter = new Getter[String] {
      override def get(path: String): Option[String] = config.getString(path)
    }
    implicit val intGetter = new Getter[Int] {
      override def get(path: String): Option[Int] = config.getInt(path)
    }
    implicit val booleanGetter = new Getter[Boolean] {
      override def get(path: String): Option[Boolean] = config.getBoolean(path)
    }
    // implement other getter for other type of value
  }

  def hasConfig(path: String) = config.getConfig(path).isDefined

  def getConfig[T](path: String)(implicit getter: Getter[T]): Option[T] = getter.get(path)

  lazy val IsProd = getConfig[String]("app.mode").contains(Consts.Mode.Prod)
  lazy val IsDev = getConfig[String]("app.mode").contains(Consts.Mode.Dev)
  lazy val IsStaging = getConfig[String]("app.mode").contains(Consts.Mode.Staging)

}
