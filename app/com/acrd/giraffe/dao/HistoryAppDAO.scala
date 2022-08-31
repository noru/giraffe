package com.acrd.giraffe.dao

import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.acrd.giraffe.common.registers._
import com.acrd.giraffe.models.AppBody
import com.acrd.giraffe.models.AppStoreTables.Platform.Platform
import com.acrd.giraffe.models.gen.Models
import com.google.inject._
import Models.{PayloadRow, PricePlanRow, AppRow}
import play.api.i18n.Lang
import scala.concurrent.{ExecutionContext => EC, Future}
import scala.util._

@Singleton
class HistoryAppDAO @Inject()(val context: BaseContext) extends BaseDAO(context){

  import tables._
  import tables.profile.api._
  import Actions._

  def getPayloadHistory(id: Long) = db.run(getPayloadAction(id).result)

  object Actions {

    val getPayloadAction = Compiled((id: ConstColumn[Long]) => PayloadHistory.filter(_.id === id).sortBy(p =>(p.lang, p.createAt.desc)))
    val getLatestHeader = Compiled((id: ConstColumn[Long]) => AppHistory.filter(_.id === id).sortBy(_.createAt.desc).take(1))
    val getLatestPrice = Compiled((id: ConstColumn[Long]) => PricePlanHistory.filter(_.id === id).sortBy(_.createAt.desc).take(1))
    val getLatestPayload = Compiled((id: ConstColumn[Long], lang: ConstColumn[Lang], os: ConstColumn[Platform]) =>
      PayloadHistory.filter(p => p.id === id && p.lang === lang && p.os === os).sortBy(_.createAt.desc).take(1)
    )

    def insert(appBody: AppBody)(implicit ec: EC) = {

      import com.acrd.giraffe.common.utils.Util._

      val regApp = appBody & CreateHistoryRecordStamper
      val (header, _, prices, payloads) = regApp.toTuple
      val id = header.id

      /** If header info is changed, add a new record. otherwise do nothing
        */
      def upsertHeader = for {
        latestHeader <- getLatestHeader(id).result.headOption
        action <- latestHeader match {
          case Some(h) if h.ignore("createAt", "updateAt") === header => DummyDBIO
          case _ => AppHistory += header
        }
      } yield action

      /** FIXME: how to identify Price Plan?
        * currently use uid, but it does not conform with update preview app action
        */
      def upsertPrices = {
        val seq = prices.map{ newPrice =>
          for {
            l <- getLatestPrice(newPrice.id).result.headOption
            action <- l match {
              case Some(p) if p.ignore("createAt") === newPrice => DummyDBIO
              case _ => PricePlanHistory += newPrice
            }
          } yield action
        }
        DBIO.seq(seq: _*)
      }

      /** If payload info is changed, add a new record. otherwise do nothing
        */
      def upsertPayloads = {
        val seq = payloads.get.map(_.toPayloadRowAndAttachmentRow._1).map{ newPayload =>
          for {
            l <- getLatestPayload(newPayload.id, newPayload.lang, newPayload.os).result.headOption
            action <- l match {
              case Some(p) if p.ignore("createAt", "updateAt") === newPayload => DummyDBIO
              case _ => PayloadHistory += newPayload
            }
          } yield action
        }
        DBIO.seq(seq: _*)
      }

      // Composed action
      for {
        _ <- upsertHeader
        _ <- upsertPrices
        _ <- upsertPayloads
      } yield ()
    }
  }
}


