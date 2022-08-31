package com.acrd.giraffe.dao

import com.acrd.giraffe.models.gen.Models
import Models._
import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.google.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext => EC, Future}
import com.acrd.giraffe.common.implicits.Options._


@Singleton
class MediaSetDAO @Inject()(val context: BaseContext) extends BaseDAO(context){

  import tables._
  import tables.profile.api._

  object Actions {
    def createSet(rows: Seq[AttachmentRow])(implicit ec: EC) = {
      if (rows.isEmpty) {
        DBIO.successful(())
      } else {
        Attachment ++= rows.zipWithIndex.map{ case (row, i) ⇒ row.copy(order = i) }
      }
    }

    def deleteSet(setId: Long)(implicit ec: EC) = for {
      _ <- Attachment.filter(_.setId === setId).delete
    } yield ()
  }

}
