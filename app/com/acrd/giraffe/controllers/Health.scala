package com.acrd.giraffe.controllers

import com.acrd.giraffe.base.BaseController
import com.acrd.giraffe.common.AppConfig
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.duration._

class Health extends BaseController{
  import Health._

  def index = Action {

    val dependencies = Seq(
      checkDB,
//      checkEhCache,
      checkRedis
    )

    val overallStatus = dependencies.sortBy(_.status).head.status

    Ok(response(overallStatus, dependencies))
  }

  private def checkDB(): HealthStatus = {

    check("MySql", () => {
      Await.result(DB.run(MTable.getTables), Inf)
    })

  }

  private def checkEhCache(): HealthStatus = {

    check("EhCache", () => {
      EhCacheApi.set("eh", "eh", 1)
      EhCacheApi.getAs[String]("eh").ensuring(_.get == "eh")
    })

  }

  private def checkRedis(): HealthStatus = {

    check("Redis", () => {
      RedisApi.set("redis", "redis", 1.second)
      RedisApi.get[String]("redis").ensuring(_.get == "redis")
    })

  }

  private def check(name: String, f: () => Unit): HealthStatus ={
    val statusAndReport = {
      try {
        f.apply()
        (ok, None)
      } catch {
        case e: Throwable => (err, Some(e.getMessage))
      }
    }
    HealthStatus(name, statusAndReport._1, statusAndReport._2)
  }

}

object Health extends AppConfig{

  val ok = StatusLevel("Good")
  val warn = StatusLevel("Warning")
  val err = StatusLevel("Error")
  val seq = Seq(err, warn, ok)
  lazy val DB = DatabaseConfigProvider.get[JdbcProfile]("mysql").db

  def response(status: StatusLevel, dependencies: Seq[HealthStatus]) = {
    <health service="giraffe" mode={getConfig[String]("app.mode").get} build={getConfig[String]("app.version").get}>
      <date>{DateTime.now}</date>
      <status>{status.name}</status>
      <dependencies>
        {
          dependencies.map(status =>
            <dependency>
              <name>{status.name}</name>
              <status>{status.status.name}</status>
              {
                val report = status.report.getOrElse("")
                if (!report.isEmpty) <report>{report}</report>
              }
            </dependency>
          )
        }
      </dependencies>
    </health>
  }
  case class HealthStatus(name: String, status: StatusLevel, report: Option[String])

  case class StatusLevel(name: String) extends Ordered[StatusLevel] {
    override def compare(that: StatusLevel): Int = {
      seq.indexOf(this) - seq.indexOf(that)
    }

  }
}
