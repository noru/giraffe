package com.acrd.giraffe.dao

import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.acrd.giraffe.common.exceptions.IdNotExistsException
import com.acrd.giraffe.models.gen.Models._
import com.google.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext => EC, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class FacetDAO @Inject()(val context: BaseContext) extends BaseDAO(context){

  import tables._
  import tables.profile.api._

  def insert(facet: FacetRow)(implicit ec: EC):Future[Try[Unit]] = {
    val q = for {
      appExists <- App.filter(_.id === facet.id).exists.result
      _ <- if(appExists) Facet += facet else throw new IdNotExistsException(facet.id, "App")
    } yield ()

    db.run(q).map(_ => Success(())).recover{ case e => Failure(e) }
  }

  def delete(facet: FacetRow)(implicit ec: EC):Future[Try[Unit]] = {
    db.run(compiledQuery(facet.id, facet.facet).delete).map{
      case 1 => Success(())
      case _ => Failure(new IdNotExistsException((facet.id, facet.facet), "Facet"))
    }.recover{ case e => Failure(e) }
  }

  val compiledQuery = Compiled((id: ConstColumn[Long], facet: ConstColumn[String]) => Facet.filter(f => f.id === id && f.facet === facet))

}
