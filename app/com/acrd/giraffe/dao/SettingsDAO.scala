package com.acrd.giraffe.dao

import javax.inject._
import com.acrd.giraffe.base.{BaseContext, BaseDAO}
import com.acrd.giraffe.common.exceptions.{IdNotExistsException, InvalidFormatException, IdAlreadyExistsException}
import com.acrd.giraffe.models.Metadata
import slick.jdbc.GetResult
import scala.collection.mutable
import scala.concurrent.{ExecutionContext => EC, Future}
import scala.util._

object SettingsDAO {

  implicit val getSettingResult = GetResult(r => Metadata(r.<<, r.<<, r.<<, r.<<, r.<<))

  private def ensureHierarchy(metadata: Metadata): Boolean = {
    val children = metadata.children
    if (children.isDefined && children.nonEmpty)
      children.get.forall(c => c.parent.contains(metadata.id) && ensureHierarchy(c))
    else true
  }

  private def getFlatMetadata(metadata: Metadata): Seq[Metadata] = {
    val seq = mutable.MutableList(metadata)
    metadata.children.foreach(_.foreach(seq ++= getFlatMetadata(_)))
    seq
  }


  /**
    * Setup a tree of Metadata recursively based on the 'parent' attribute of each.
    */
  private def getTreeMetadata(rootName: String, metadatas: Seq[Metadata]): Option[Metadata] = {

    if (metadatas.isEmpty) return None

    // get root node
    var parts = metadatas.partition(_.id == rootName)
    if (parts._1.isEmpty) return None
    val root = parts._1.head

    // get children and attach them to root.children
    parts = parts._2.partition(_.parent.contains(rootName))
    val children = parts._1
    root.children = Option(children)

    // for the rest element, execute this method recursively
    children.foreach(m => getTreeMetadata(m.id, parts._2))

    // TODO, performance/algorithm upgrade? possibility change to tail recursion?
    Option(root)

  }

}

@Singleton
class SettingsDAO @Inject()(val context: BaseContext) extends BaseDAO(context){

  import tables.profile.api._
  import tables.Settings
  import SettingsDAO._

  def getAll(compact: Boolean = true)(implicit ec: EC) = {
    db.run(Settings.result)
        .map(r => if (compact)
                    r.map(_.copy(value = None, `type` = None))
                  else r)
        .map(r => r.filter(foo => foo.parent.isEmpty || foo.parent.contains("") )
                    .map(root => getTreeMetadata(root.id, r)))
  }

  /**
   * Query a metadata object by id
   * @param id
   * @param recursively if <code>true</code>, will return the whole tree, otherwise the root
   * @return Option[MetaData]
   */
  def getById(id: String, recursively: Boolean = true, flat: Boolean = false)(implicit ec: EC) = {

    if (recursively){
      val action = sql"""select *
                         from settings
                         join (select @pv:=$id)tmp
                         where parent=@pv or id=@pv"""
                        .as[Metadata]

      db.run(action).map(m => getTreeMetadata(id, m.toSeq))

    } else {
      db.run(byIdQuery(id).result.headOption)
    }
  }

  /** Insert a metadata, all metadatas must be new items
   */
  def insert(meta: Metadata)(implicit ec: EC): Future[Try[Unit]] = {
    val flat = getFlatMetadata(meta)
    val ids = flat.map(_.id).toSet

    val q = for {
      isValid <- DBIO.from(isMetadataValid(meta))
      _ <- if (isValid) DummyDBIO else DBIO.failed(new InvalidFormatException)
      idList <- Settings.filter(_.id inSetBind ids).map(_.id).result
      _ <- if (idList.isEmpty) Settings ++= flat
           else DBIO.failed(new IdAlreadyExistsException(idList.mkString(", "), "Settings"))
    } yield ()

    db.run(q).map(any => Success(())).recover{ case e => Failure(e) }

  }

  def updateSingle(m: Metadata)(implicit ec: EC): Future[Try[Unit]] = {
    db.run(updateSingle(m.id).update(m.name, m.value, m.`type`)).map(_ => Success(()))
  }

  def updateHierarchy(m: Metadata)(implicit ec: EC): Future[Try[Unit]] = {
    val q = for {
      _ <- delete(m.id)
      _ <- insert(m)
    } yield ()
    q.map(_ => Success(()))
  }

  /** Delete a metadata by id, all decedents are deleted as well
   */
  def delete(id: String)(implicit ec: EC):Future[Try[Unit]] = {

    val getId = sql"""select id
                       from settings
                       join (select @pv:=$id)tmp
                       where parent=@pv or id=@pv""".as[String]

    val q = for {
      idList <- getId
      _ <- if (idList.nonEmpty) batchSelectAction(idList).delete
           else DBIO.failed(new IdNotExistsException(id, "Settings"))
    } yield ()
    db.run(q).map(any => Success(())).recover{ case e => Failure(e) }
  }


  val byIdQuery = Compiled((id: ConstColumn[String]) => Settings.filter(_.id === id))
  val updateSingle = Compiled((id: ConstColumn[String]) => Settings.filter(_.id === id).map(m => (m.name, m.value, m.`type`)))

  override def createSchema(implicit ec: EC) = db.run(Settings.schema.create)

  private def checkConflict(metadata: Metadata)(implicit ec: EC): Future[Boolean] = {
    val flatMetas = getFlatMetadata(metadata)
    db.run(batchSelectAction(flatMetas.map(_.id).toSet).result)
        .map(_.forall(line => line.parent == flatMetas.find(_.id == line.id).get.parent))

  }
  private def isMetadataValid(metadata: Metadata)(implicit ec: EC): Future[Boolean] = {

    import com.acrd.giraffe.common.utils.Util.ValidationAsync._

    all(
      Future(ensureHierarchy(metadata)),
      checkConflict(metadata)
    )

  }
  private def batchSelectAction(idSet: TraversableOnce[String]) = Settings.filter(_.id inSetBind idSet.toSet)

}


