package com.acrd.giraffe.base

import com.acrd.giraffe.models.AppStoreTables
import com.google.inject.Singleton
import play.api.Play.current
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

trait BaseContext{

  def dbName: String

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](dbName)
  val db = dbConfig.db
  val profile = dbConfig.driver
  val tables = new AppStoreTables {
    override val profile: JdbcProfile = dbConfig.driver
  }

}

@Singleton
class AppContext extends BaseContext{

  def dbName = "mysql"
}

@Singleton
class TestingContext extends BaseContext{

  def dbName = "h2"

}
