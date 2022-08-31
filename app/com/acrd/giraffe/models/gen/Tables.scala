package com.acrd.giraffe.models.gen
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.driver.JdbcProfile
  import profile.api._
  
  import Models._
  import play.api.i18n.Lang
  import com.acrd.giraffe.models.Metadata
  import com.acrd.giraffe.models.AppStoreTables
  import AppStoreTables.CommentStatus._
  import AppStoreTables.Platform._
  import AppStoreTables.PayloadStatus._
  implicit def mapStringLang = MappedColumnType.base[Lang, String](_.code, Lang(_))
  implicit def mapStringCommentStatus = MappedColumnType.base[CommentStatus, String](_.toString, {a=>a})
  implicit def mapStringPlatform = MappedColumnType.base[Platform, String](_.toString, {a=>a})
  implicit def mapStringPayloadStatus = MappedColumnType.base[PayloadStatus, String](_.toString, {a=>a})
      
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = Array(ActionLog.schema, App.schema, AppHistory.schema, AppLive.schema, Attachment.schema, AttachmentLive.schema, Comment.schema, Company.schema, Facet.schema, FacetLive.schema, Payload.schema, PayloadHistory.schema, PayloadLive.schema, PricePlan.schema, PricePlanHistory.schema, PricePlanLive.schema, Rating.schema, Settings.schema, User.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema


  /** GetResult implicit for fetching ActionLogRow objects using plain SQL queries */
  implicit def GetResultActionLogRow(implicit e0: GR[Long], e1: GR[java.sql.Timestamp], e2: GR[Option[String]]): GR[ActionLogRow] = GR{
    prs => import prs._
    ActionLogRow.tupled((<<[Long], <<[java.sql.Timestamp], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<[Long], <<?[String], <<?[String]))
  }
  /** Table description of table action_log. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class ActionLog(_tableTag: Tag) extends Table[ActionLogRow](_tableTag, "action_log") {
    def * = (id, timestamp, action, fromState, toState, msg1, msg2, parentId, userId, `type`) <> (ActionLogRow.tupled, ActionLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(timestamp), action, fromState, toState, msg1, msg2, Rep.Some(parentId), userId, `type`).shaped.<>({r=>import r._; _1.map(_=> ActionLogRow.tupled((_1.get, _2.get, _3, _4, _5, _6, _7, _8.get, _9, _10)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column timestamp SqlType(TIMESTAMP) */
    val timestamp: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("timestamp")
    /** Database column action SqlType(VARCHAR), Length(20,true), Default(None) */
    val action: Rep[Option[String]] = column[Option[String]]("action", O.Length(20,varying=true), O.Default(None))
    /** Database column from_state SqlType(VARCHAR), Length(20,true), Default(None) */
    val fromState: Rep[Option[String]] = column[Option[String]]("from_state", O.Length(20,varying=true), O.Default(None))
    /** Database column to_state SqlType(VARCHAR), Length(20,true), Default(None) */
    val toState: Rep[Option[String]] = column[Option[String]]("to_state", O.Length(20,varying=true), O.Default(None))
    /** Database column msg1 SqlType(TINYTEXT), Length(255,true), Default(None) */
    val msg1: Rep[Option[String]] = column[Option[String]]("msg1", O.Length(255,varying=true), O.Default(None))
    /** Database column msg2 SqlType(TEXT), Default(None) */
    val msg2: Rep[Option[String]] = column[Option[String]]("msg2", O.Default(None))
    /** Database column parent_id SqlType(BIGINT UNSIGNED) */
    val parentId: Rep[Long] = column[Long]("parent_id")
    /** Database column user_id SqlType(CHAR), Length(20,false), Default(None) */
    val userId: Rep[Option[String]] = column[Option[String]]("user_id", O.Length(20,varying=false), O.Default(None))
    /** Database column type SqlType(VARCHAR), Length(20,true), Default(None)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[Option[String]] = column[Option[String]]("type", O.Length(20,varying=true), O.Default(None))

    /** Index over (parentId) (database name idx_parent_id) */
    val index1 = index("idx_parent_id", parentId)
  }
  /** Collection-like TableQuery object for table ActionLog */
  lazy val ActionLog = new TableQuery(tag => new ActionLog(tag))


  /** GetResult implicit for fetching AppRow objects using plain SQL queries */
  implicit def GetResultAppRow(implicit e0: GR[Long], e1: GR[java.sql.Timestamp], e2: GR[String], e3: GR[Option[String]]): GR[AppRow] = GR{
    prs => import prs._
    AppRow.tupled((<<[Long], <<[java.sql.Timestamp], <<[java.sql.Timestamp], <<[String], <<[String], <<[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table app. Objects of this class serve as prototypes for rows in queries. */
  class App(_tableTag: Tag) extends Table[AppRow](_tableTag, "app") {
    def * = (id, createAt, updateAt, title, authorId, icon, supportContact, webServiceIdentification, prodVerMap) <> (AppRow.tupled, AppRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(createAt), Rep.Some(updateAt), Rep.Some(title), Rep.Some(authorId), Rep.Some(icon), supportContact, webServiceIdentification, prodVerMap).shaped.<>({r=>import r._; _1.map(_=> AppRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column update_at SqlType(TIMESTAMP) */
    val updateAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_at")
    /** Database column title SqlType(VARCHAR), Length(100,true) */
    val title: Rep[String] = column[String]("title", O.Length(100,varying=true))
    /** Database column author_id SqlType(CHAR), Length(20,false) */
    val authorId: Rep[String] = column[String]("author_id", O.Length(20,varying=false))
    /** Database column icon SqlType(VARCHAR), Length(1023,true) */
    val icon: Rep[String] = column[String]("icon", O.Length(1023,varying=true))
    /** Database column support_contact SqlType(VARCHAR), Length(100,true), Default(None) */
    val supportContact: Rep[Option[String]] = column[Option[String]]("support_contact", O.Length(100,varying=true), O.Default(None))
    /** Database column web_service_identification SqlType(VARCHAR), Length(45,true), Default(None) */
    val webServiceIdentification: Rep[Option[String]] = column[Option[String]]("web_service_identification", O.Length(45,varying=true), O.Default(None))
    /** Database column prod_ver_map SqlType(TEXT), Default(None) */
    val prodVerMap: Rep[Option[String]] = column[Option[String]]("prod_ver_map", O.Default(None))

    /** Index over (title) (database name ftidx_app_title) */
    val index1 = index("ftidx_app_title", title)
    /** Index over (authorId) (database name idx_author) */
    val index2 = index("idx_author", authorId)
    /** Index over (createAt) (database name idx_create_at) */
    val index3 = index("idx_create_at", createAt)
    /** Index over (updateAt) (database name idx_update_at) */
    val index4 = index("idx_update_at", updateAt)
  }
  /** Collection-like TableQuery object for table App */
  lazy val App = new TableQuery(tag => new App(tag))


  /** GetResult implicit for fetching AppHistoryRow objects using plain SQL queries */

  /** Table description of table app_history. Objects of this class serve as prototypes for rows in queries. */
  class AppHistory(_tableTag: Tag) extends Table[AppRow](_tableTag, "app_history") {
    def * = (id, createAt, updateAt, title, authorId, icon, supportContact, webServiceIdentification, prodVerMap) <> (AppRow.tupled, AppRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(createAt), Rep.Some(updateAt), Rep.Some(title), Rep.Some(authorId), Rep.Some(icon), supportContact, webServiceIdentification, prodVerMap).shaped.<>({r=>import r._; _1.map(_=> AppRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED) */
    val id: Rep[Long] = column[Long]("id")
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column update_at SqlType(TIMESTAMP) */
    val updateAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_at")
    /** Database column title SqlType(VARCHAR), Length(100,true) */
    val title: Rep[String] = column[String]("title", O.Length(100,varying=true))
    /** Database column author_id SqlType(CHAR), Length(20,false) */
    val authorId: Rep[String] = column[String]("author_id", O.Length(20,varying=false))
    /** Database column icon SqlType(VARCHAR), Length(1023,true) */
    val icon: Rep[String] = column[String]("icon", O.Length(1023,varying=true))
    /** Database column support_contact SqlType(VARCHAR), Length(100,true), Default(None) */
    val supportContact: Rep[Option[String]] = column[Option[String]]("support_contact", O.Length(100,varying=true), O.Default(None))
    /** Database column web_service_identification SqlType(VARCHAR), Length(45,true), Default(None) */
    val webServiceIdentification: Rep[Option[String]] = column[Option[String]]("web_service_identification", O.Length(45,varying=true), O.Default(None))
    /** Database column prod_ver_map SqlType(TEXT), Default(None) */
    val prodVerMap: Rep[Option[String]] = column[Option[String]]("prod_ver_map", O.Default(None))

    /** Primary key of AppHistory (database name app_history_PK) */
    val pk = primaryKey("app_history_PK", (id, createAt))

    /** Index over (authorId) (database name idx_history_author) */
    val index1 = index("idx_history_author", authorId)
    /** Index over (createAt) (database name idx_history_create_at) */
    val index2 = index("idx_history_create_at", createAt)
  }
  /** Collection-like TableQuery object for table AppHistory */
  lazy val AppHistory = new TableQuery(tag => new AppHistory(tag))


  /** GetResult implicit for fetching AppLiveRow objects using plain SQL queries */

  /** Table description of table app_live. Objects of this class serve as prototypes for rows in queries. */
  class AppLive(_tableTag: Tag) extends Table[AppRow](_tableTag, "app_live") {
    def * = (id, createAt, updateAt, title, authorId, icon, supportContact, webServiceIdentification, prodVerMap) <> (AppRow.tupled, AppRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(createAt), Rep.Some(updateAt), Rep.Some(title), Rep.Some(authorId), Rep.Some(icon), supportContact, webServiceIdentification, prodVerMap).shaped.<>({r=>import r._; _1.map(_=> AppRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column update_at SqlType(TIMESTAMP) */
    val updateAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_at")
    /** Database column title SqlType(VARCHAR), Length(100,true) */
    val title: Rep[String] = column[String]("title", O.Length(100,varying=true))
    /** Database column author_id SqlType(CHAR), Length(20,false) */
    val authorId: Rep[String] = column[String]("author_id", O.Length(20,varying=false))
    /** Database column icon SqlType(VARCHAR), Length(1023,true) */
    val icon: Rep[String] = column[String]("icon", O.Length(1023,varying=true))
    /** Database column support_contact SqlType(VARCHAR), Length(100,true), Default(None) */
    val supportContact: Rep[Option[String]] = column[Option[String]]("support_contact", O.Length(100,varying=true), O.Default(None))
    /** Database column web_service_identification SqlType(VARCHAR), Length(45,true), Default(None) */
    val webServiceIdentification: Rep[Option[String]] = column[Option[String]]("web_service_identification", O.Length(45,varying=true), O.Default(None))
    /** Database column prod_ver_map SqlType(TEXT), Default(None) */
    val prodVerMap: Rep[Option[String]] = column[Option[String]]("prod_ver_map", O.Default(None))

    /** Index over (title) (database name ftidx_app_live_title) */
    val index1 = index("ftidx_app_live_title", title)
    /** Index over (authorId) (database name idx_live_author) */
    val index2 = index("idx_live_author", authorId)
    /** Index over (createAt) (database name idx_live_create_at) */
    val index3 = index("idx_live_create_at", createAt)
    /** Index over (updateAt) (database name idx_live_update_at) */
    val index4 = index("idx_live_update_at", updateAt)
  }
  /** Collection-like TableQuery object for table AppLive */
  lazy val AppLive = new TableQuery(tag => new AppLive(tag))


  /** GetResult implicit for fetching AttachmentRow objects using plain SQL queries */
  implicit def GetResultAttachmentRow(implicit e0: GR[Long], e1: GR[Option[Int]], e2: GR[Option[String]], e3: GR[Option[Long]]): GR[AttachmentRow] = GR{
    prs => import prs._
    AttachmentRow.tupled((<<[Long], <<?[Int], <<?[String], <<?[Int], <<?[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[Long], <<?[Long]))
  }
  /** Table description of table attachment. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class Attachment(_tableTag: Tag) extends Table[AttachmentRow](_tableTag, "attachment") {
    def * = (id, order, uri, size, mime, `type`, value, shortDescription, description, setId, appId) <> (AttachmentRow.tupled, AttachmentRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), order, uri, size, mime, `type`, value, shortDescription, description, setId, appId).shaped.<>({r=>import r._; _1.map(_=> AttachmentRow.tupled((_1.get, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column order SqlType(INT), Default(Some(0)) */
    val order: Rep[Option[Int]] = column[Option[Int]]("order", O.Default(Some(0)))
    /** Database column uri SqlType(VARCHAR), Length(1023,true), Default(None) */
    val uri: Rep[Option[String]] = column[Option[String]]("uri", O.Length(1023,varying=true), O.Default(None))
    /** Database column size SqlType(INT), Default(None) */
    val size: Rep[Option[Int]] = column[Option[Int]]("size", O.Default(None))
    /** Database column mime SqlType(VARCHAR), Length(45,true), Default(None) */
    val mime: Rep[Option[String]] = column[Option[String]]("mime", O.Length(45,varying=true), O.Default(None))
    /** Database column type SqlType(VARCHAR), Length(45,true), Default(None)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[Option[String]] = column[Option[String]]("type", O.Length(45,varying=true), O.Default(None))
    /** Database column value SqlType(TEXT), Default(None) */
    val value: Rep[Option[String]] = column[Option[String]]("value", O.Default(None))
    /** Database column short_description SqlType(TINYTEXT), Length(255,true), Default(None) */
    val shortDescription: Rep[Option[String]] = column[Option[String]]("short_description", O.Length(255,varying=true), O.Default(None))
    /** Database column description SqlType(TEXT), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column set_id SqlType(BIGINT UNSIGNED), Default(None) */
    val setId: Rep[Option[Long]] = column[Option[Long]]("set_id", O.Default(None))
    /** Database column app_id SqlType(BIGINT UNSIGNED), Default(None) */
    val appId: Rep[Option[Long]] = column[Option[Long]]("app_id", O.Default(None))

    /** Index over (appId) (database name app_id) */
    val index1 = index("app_id", appId)
    /** Index over (setId) (database name set_id) */
    val index2 = index("set_id", setId)
  }
  /** Collection-like TableQuery object for table Attachment */
  lazy val Attachment = new TableQuery(tag => new Attachment(tag))


  /** GetResult implicit for fetching AttachmentLiveRow objects using plain SQL queries */

  /** Table description of table attachment_live. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class AttachmentLive(_tableTag: Tag) extends Table[AttachmentRow](_tableTag, "attachment_live") {
    def * = (id, order, uri, size, mime, `type`, value, shortDescription, description, setId, appId) <> (AttachmentRow.tupled, AttachmentRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), order, uri, size, mime, `type`, value, shortDescription, description, setId, appId).shaped.<>({r=>import r._; _1.map(_=> AttachmentRow.tupled((_1.get, _2, _3, _4, _5, _6, _7, _8, _9, _10, _11)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column order SqlType(INT), Default(Some(0)) */
    val order: Rep[Option[Int]] = column[Option[Int]]("order", O.Default(Some(0)))
    /** Database column uri SqlType(VARCHAR), Length(1023,true), Default(None) */
    val uri: Rep[Option[String]] = column[Option[String]]("uri", O.Length(1023,varying=true), O.Default(None))
    /** Database column size SqlType(INT), Default(None) */
    val size: Rep[Option[Int]] = column[Option[Int]]("size", O.Default(None))
    /** Database column mime SqlType(VARCHAR), Length(45,true), Default(None) */
    val mime: Rep[Option[String]] = column[Option[String]]("mime", O.Length(45,varying=true), O.Default(None))
    /** Database column type SqlType(VARCHAR), Length(45,true), Default(None)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[Option[String]] = column[Option[String]]("type", O.Length(45,varying=true), O.Default(None))
    /** Database column value SqlType(TEXT), Default(None) */
    val value: Rep[Option[String]] = column[Option[String]]("value", O.Default(None))
    /** Database column short_description SqlType(TINYTEXT), Length(255,true), Default(None) */
    val shortDescription: Rep[Option[String]] = column[Option[String]]("short_description", O.Length(255,varying=true), O.Default(None))
    /** Database column description SqlType(TEXT), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column set_id SqlType(BIGINT UNSIGNED), Default(None) */
    val setId: Rep[Option[Long]] = column[Option[Long]]("set_id", O.Default(None))
    /** Database column app_id SqlType(BIGINT UNSIGNED), Default(None) */
    val appId: Rep[Option[Long]] = column[Option[Long]]("app_id", O.Default(None))

    /** Index over (appId) (database name idx_app_id) */
    val index1 = index("idx_app_id", appId)
    /** Index over (setId) (database name idx_set_id) */
    val index2 = index("idx_set_id", setId)
  }
  /** Collection-like TableQuery object for table AttachmentLive */
  lazy val AttachmentLive = new TableQuery(tag => new AttachmentLive(tag))


  /** GetResult implicit for fetching CommentRow objects using plain SQL queries */
  implicit def GetResultCommentRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[Lang]], e3: GR[Option[String]], e4: GR[java.sql.Timestamp], e5: GR[CommentStatus], e6: GR[Int], e7: GR[Option[Long]], e8: GR[Option[Boolean]]): GR[CommentRow] = GR{
    prs => import prs._
    CommentRow.tupled((<<[Long], <<[Long], <<[String], <<?[Lang], <<?[String], <<?[String], <<[java.sql.Timestamp], <<[java.sql.Timestamp], <<[CommentStatus], <<[Int], <<?[Long], <<?[String], <<?[Boolean]))
  }
  /** Table description of table comment. Objects of this class serve as prototypes for rows in queries. */
  class Comment(_tableTag: Tag) extends Table[CommentRow](_tableTag, "comment") {
    def * = (id, appId, author, lang, title, text, createAt, updateAt, status, rating, replyTo, storeId, isVerifiedDownload) <> (CommentRow.tupled, CommentRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(appId), Rep.Some(author), lang, title, text, Rep.Some(createAt), Rep.Some(updateAt), Rep.Some(status), Rep.Some(rating), replyTo, storeId, isVerifiedDownload).shaped.<>({r=>import r._; _1.map(_=> CommentRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7.get, _8.get, _9.get, _10.get, _11, _12, _13)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column app_id SqlType(BIGINT UNSIGNED) */
    val appId: Rep[Long] = column[Long]("app_id")
    /** Database column author SqlType(CHAR), Length(20,false) */
    val author: Rep[String] = column[String]("author", O.Length(20,varying=false))
    /** Database column lang SqlType(ENUM), Length(5,false), Default(Some(en)) */
    val lang: Rep[Option[Lang]] = column[Option[Lang]]("lang", O.Length(5,varying=false), O.Default(Some(Lang("en"))))
    /** Database column title SqlType(TINYTEXT), Length(255,true), Default(None) */
    val title: Rep[Option[String]] = column[Option[String]]("title", O.Length(255,varying=true), O.Default(None))
    /** Database column text SqlType(TEXT), Default(None) */
    val text: Rep[Option[String]] = column[Option[String]]("text", O.Default(None))
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column update_at SqlType(TIMESTAMP) */
    val updateAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_at")
    /** Database column status SqlType(ENUM), Length(14,false), Default(preview) */
    val status: Rep[CommentStatus] = column[CommentStatus]("status", O.Length(14,varying=false), O.Default("preview"))
    /** Database column rating SqlType(TINYINT), Default(0) */
    val rating: Rep[Int] = column[Int]("rating", O.Default(0))
    /** Database column reply_to SqlType(BIGINT UNSIGNED), Default(None) */
    val replyTo: Rep[Option[Long]] = column[Option[Long]]("reply_to", O.Default(None))
    /** Database column store_id SqlType(VARCHAR), Length(45,true), Default(None) */
    val storeId: Rep[Option[String]] = column[Option[String]]("store_id", O.Length(45,varying=true), O.Default(None))
    /** Database column is_verified_download SqlType(BIT), Default(Some(false)) */
    val isVerifiedDownload: Rep[Option[Boolean]] = column[Option[Boolean]]("is_verified_download", O.Default(Some(false)))

    /** Foreign key referencing AppHistory (database name fk_comment_app_history1) */
    lazy val appHistoryFk = foreignKey("fk_comment_app_history1", appId, AppHistory)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
    /** Foreign key referencing Comment (database name fk_comment_comment1) */
    lazy val commentFk = foreignKey("fk_comment_comment1", replyTo, Comment)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table Comment */
  lazy val Comment = new TableQuery(tag => new Comment(tag))


  /** GetResult implicit for fetching CompanyRow objects using plain SQL queries */
  implicit def GetResultCompanyRow(implicit e0: GR[Long], e1: GR[Option[String]], e2: GR[Option[Int]]): GR[CompanyRow] = GR{
    prs => import prs._
    CompanyRow.tupled((<<[Long], <<?[String], <<?[String], <<?[Int]))
  }
  /** Table description of table company. Objects of this class serve as prototypes for rows in queries. */
  class Company(_tableTag: Tag) extends Table[CompanyRow](_tableTag, "company") {
    def * = (id, email, address, services) <> (CompanyRow.tupled, CompanyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), email, address, services).shaped.<>({r=>import r._; _1.map(_=> CompanyRow.tupled((_1.get, _2, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column email SqlType(VARCHAR), Length(45,true), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Length(45,varying=true), O.Default(None))
    /** Database column address SqlType(VARCHAR), Length(45,true), Default(None) */
    val address: Rep[Option[String]] = column[Option[String]]("address", O.Length(45,varying=true), O.Default(None))
    /** Database column services SqlType(INT), Default(None) */
    val services: Rep[Option[Int]] = column[Option[Int]]("services", O.Default(None))
  }
  /** Collection-like TableQuery object for table Company */
  lazy val Company = new TableQuery(tag => new Company(tag))


  /** GetResult implicit for fetching FacetRow objects using plain SQL queries */
  implicit def GetResultFacetRow(implicit e0: GR[Long], e1: GR[String]): GR[FacetRow] = GR{
    prs => import prs._
    FacetRow.tupled((<<[Long], <<[String]))
  }
  /** Table description of table facet. Objects of this class serve as prototypes for rows in queries. */
  class Facet(_tableTag: Tag) extends Table[FacetRow](_tableTag, "facet") {
    def * = (id, facet) <> (FacetRow.tupled, FacetRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(facet)).shaped.<>({r=>import r._; _1.map(_=> FacetRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED) */
    val id: Rep[Long] = column[Long]("id")
    /** Database column facet SqlType(VARCHAR), Length(80,true) */
    val facet: Rep[String] = column[String]("facet", O.Length(80,varying=true))

    /** Uniqueness Index over (id,facet) (database name id_facet_unique) */
    val index1 = index("id_facet_unique", (id, facet), unique=true)
    /** Index over (facet) (database name idx_facet) */
    val index2 = index("idx_facet", facet)
    /** Index over (id) (database name idx_id) */
    val index3 = index("idx_id", id)
  }
  /** Collection-like TableQuery object for table Facet */
  lazy val Facet = new TableQuery(tag => new Facet(tag))


  /** GetResult implicit for fetching FacetLiveRow objects using plain SQL queries */

  /** Table description of table facet_live. Objects of this class serve as prototypes for rows in queries. */
  class FacetLive(_tableTag: Tag) extends Table[FacetRow](_tableTag, "facet_live") {
    def * = (id, facet) <> (FacetRow.tupled, FacetRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(facet)).shaped.<>({r=>import r._; _1.map(_=> FacetRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED) */
    val id: Rep[Long] = column[Long]("id")
    /** Database column facet SqlType(VARCHAR), Length(80,true) */
    val facet: Rep[String] = column[String]("facet", O.Length(80,varying=true))

    /** Uniqueness Index over (id,facet) (database name id_live_facet_unique) */
    val index1 = index("id_live_facet_unique", (id, facet), unique=true)
    /** Index over (facet) (database name idx_live_facet) */
    val index2 = index("idx_live_facet", facet)
    /** Index over (id) (database name idx_live_id) */
    val index3 = index("idx_live_id", id)
  }
  /** Collection-like TableQuery object for table FacetLive */
  lazy val FacetLive = new TableQuery(tag => new FacetLive(tag))


  /** GetResult implicit for fetching PayloadRow objects using plain SQL queries */
  implicit def GetResultPayloadRow(implicit e0: GR[Long], e1: GR[Lang], e2: GR[Platform], e3: GR[java.sql.Timestamp], e4: GR[Option[PayloadStatus]], e5: GR[String], e6: GR[Option[Int]], e7: GR[Option[String]]): GR[PayloadRow] = GR{
    prs => import prs._
    PayloadRow.tupled((<<[Long], <<[Lang], <<[Platform], <<[java.sql.Timestamp], <<[java.sql.Timestamp], <<?[PayloadStatus], <<[String], <<?[Int], <<?[String], <<[Long], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String], <<[String]))
  }
  /** Table description of table payload. Objects of this class serve as prototypes for rows in queries. */
  class Payload(_tableTag: Tag) extends Table[PayloadRow](_tableTag, "payload") {
    def * = (id, lang, os, createAt, updateAt, reviewStatus, url, size, mime, attachmentSetId, title, shortDescription, description, instruction, installation, additionalInfo, knownIssue, supportInfo, version, versionDescription, upgradeCode, productCode) <> (PayloadRow.tupled, PayloadRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(lang), Rep.Some(os), Rep.Some(createAt), Rep.Some(updateAt), reviewStatus, Rep.Some(url), size, mime, Rep.Some(attachmentSetId), Rep.Some(title), Rep.Some(shortDescription), Rep.Some(description), Rep.Some(instruction), Rep.Some(installation), Rep.Some(additionalInfo), Rep.Some(knownIssue), Rep.Some(supportInfo), Rep.Some(version), Rep.Some(versionDescription), Rep.Some(upgradeCode), Rep.Some(productCode)).shaped.<>({r=>import r._; _1.map(_=> PayloadRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get, _8, _9, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get, _18.get, _19.get, _20.get, _21.get, _22.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED) */
    val id: Rep[Long] = column[Long]("id")
    /** Database column lang SqlType(ENUM), Length(5,false) */
    val lang: Rep[Lang] = column[Lang]("lang", O.Length(5,varying=false))
    /** Database column os SqlType(ENUM), Length(8,false) */
    val os: Rep[Platform] = column[Platform]("os", O.Length(8,varying=false))
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column update_at SqlType(TIMESTAMP) */
    val updateAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_at")
    /** Database column review_status SqlType(ENUM), Length(28,false), Default(Some(draft)) */
    val reviewStatus: Rep[Option[PayloadStatus]] = column[Option[PayloadStatus]]("review_status", O.Length(28,varying=false), O.Default(Some("draft")))
    /** Database column url SqlType(VARCHAR), Length(1023,true) */
    val url: Rep[String] = column[String]("url", O.Length(1023,varying=true))
    /** Database column size SqlType(INT), Default(None) */
    val size: Rep[Option[Int]] = column[Option[Int]]("size", O.Default(None))
    /** Database column mime SqlType(ENUM), Length(24,false), Default(None) */
    val mime: Rep[Option[String]] = column[Option[String]]("mime", O.Length(24,varying=false), O.Default(None))
    /** Database column attachment_set_id SqlType(BIGINT UNSIGNED) */
    val attachmentSetId: Rep[Long] = column[Long]("attachment_set_id")
    /** Database column title SqlType(VARCHAR), Length(100,true) */
    val title: Rep[String] = column[String]("title", O.Length(100,varying=true))
    /** Database column short_description SqlType(VARCHAR), Length(511,true) */
    val shortDescription: Rep[String] = column[String]("short_description", O.Length(511,varying=true))
    /** Database column description SqlType(TEXT) */
    val description: Rep[String] = column[String]("description")
    /** Database column instruction SqlType(TEXT) */
    val instruction: Rep[String] = column[String]("instruction")
    /** Database column installation SqlType(TEXT) */
    val installation: Rep[String] = column[String]("installation")
    /** Database column additional_info SqlType(TEXT) */
    val additionalInfo: Rep[String] = column[String]("additional_info")
    /** Database column known_issue SqlType(TEXT) */
    val knownIssue: Rep[String] = column[String]("known_issue")
    /** Database column support_info SqlType(TEXT) */
    val supportInfo: Rep[String] = column[String]("support_info")
    /** Database column version SqlType(VARCHAR), Length(45,true) */
    val version: Rep[String] = column[String]("version", O.Length(45,varying=true))
    /** Database column version_description SqlType(TEXT) */
    val versionDescription: Rep[String] = column[String]("version_description")
    /** Database column upgrade_code SqlType(VARCHAR), Length(64,true) */
    val upgradeCode: Rep[String] = column[String]("upgrade_code", O.Length(64,varying=true))
    /** Database column product_code SqlType(VARCHAR), Length(64,true) */
    val productCode: Rep[String] = column[String]("product_code", O.Length(64,varying=true))

    /** Primary key of Payload (database name payload_PK) */
    val pk = primaryKey("payload_PK", (id, lang, os))

    /** Foreign key referencing App (database name fk_payload_app_header1) */
    lazy val appFk = foreignKey("fk_payload_app_header1", id, App)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)

    /** Index over (title) (database name idx_payload_title) */
    val index1 = index("idx_payload_title", title)
  }
  /** Collection-like TableQuery object for table Payload */
  lazy val Payload = new TableQuery(tag => new Payload(tag))


  /** GetResult implicit for fetching PayloadHistoryRow objects using plain SQL queries */

  /** Table description of table payload_history. Objects of this class serve as prototypes for rows in queries. */
  class PayloadHistory(_tableTag: Tag) extends Table[PayloadRow](_tableTag, "payload_history") {
    def * = (id, lang, os, createAt, updateAt, reviewStatus, url, size, mime, attachmentSetId, title, shortDescription, description, instruction, installation, additionalInfo, knownIssue, supportInfo, version, versionDescription, upgradeCode, productCode) <> (PayloadRow.tupled, PayloadRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(lang), Rep.Some(os), Rep.Some(createAt), Rep.Some(updateAt), reviewStatus, Rep.Some(url), size, mime, Rep.Some(attachmentSetId), Rep.Some(title), Rep.Some(shortDescription), Rep.Some(description), Rep.Some(instruction), Rep.Some(installation), Rep.Some(additionalInfo), Rep.Some(knownIssue), Rep.Some(supportInfo), Rep.Some(version), Rep.Some(versionDescription), Rep.Some(upgradeCode), Rep.Some(productCode)).shaped.<>({r=>import r._; _1.map(_=> PayloadRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get, _8, _9, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get, _18.get, _19.get, _20.get, _21.get, _22.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED) */
    val id: Rep[Long] = column[Long]("id")
    /** Database column lang SqlType(ENUM), Length(5,false) */
    val lang: Rep[Lang] = column[Lang]("lang", O.Length(5,varying=false))
    /** Database column os SqlType(ENUM), Length(8,false) */
    val os: Rep[Platform] = column[Platform]("os", O.Length(8,varying=false))
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column update_at SqlType(TIMESTAMP) */
    val updateAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_at")
    /** Database column review_status SqlType(ENUM), Length(28,false), Default(Some(live)) */
    val reviewStatus: Rep[Option[PayloadStatus]] = column[Option[PayloadStatus]]("review_status", O.Length(28,varying=false), O.Default(Some("live")))
    /** Database column url SqlType(VARCHAR), Length(1023,true) */
    val url: Rep[String] = column[String]("url", O.Length(1023,varying=true))
    /** Database column size SqlType(INT), Default(None) */
    val size: Rep[Option[Int]] = column[Option[Int]]("size", O.Default(None))
    /** Database column mime SqlType(ENUM), Length(24,false), Default(None) */
    val mime: Rep[Option[String]] = column[Option[String]]("mime", O.Length(24,varying=false), O.Default(None))
    /** Database column attachment_set_id SqlType(BIGINT UNSIGNED) */
    val attachmentSetId: Rep[Long] = column[Long]("attachment_set_id")
    /** Database column title SqlType(VARCHAR), Length(100,true) */
    val title: Rep[String] = column[String]("title", O.Length(100,varying=true))
    /** Database column short_description SqlType(VARCHAR), Length(511,true) */
    val shortDescription: Rep[String] = column[String]("short_description", O.Length(511,varying=true))
    /** Database column description SqlType(TEXT) */
    val description: Rep[String] = column[String]("description")
    /** Database column instruction SqlType(TEXT) */
    val instruction: Rep[String] = column[String]("instruction")
    /** Database column installation SqlType(TEXT) */
    val installation: Rep[String] = column[String]("installation")
    /** Database column additional_info SqlType(TEXT) */
    val additionalInfo: Rep[String] = column[String]("additional_info")
    /** Database column known_issue SqlType(TEXT) */
    val knownIssue: Rep[String] = column[String]("known_issue")
    /** Database column support_info SqlType(TEXT) */
    val supportInfo: Rep[String] = column[String]("support_info")
    /** Database column version SqlType(VARCHAR), Length(45,true) */
    val version: Rep[String] = column[String]("version", O.Length(45,varying=true))
    /** Database column version_description SqlType(TEXT) */
    val versionDescription: Rep[String] = column[String]("version_description")
    /** Database column upgrade_code SqlType(VARCHAR), Length(64,true) */
    val upgradeCode: Rep[String] = column[String]("upgrade_code", O.Length(64,varying=true))
    /** Database column product_code SqlType(VARCHAR), Length(64,true) */
    val productCode: Rep[String] = column[String]("product_code", O.Length(64,varying=true))

    /** Primary key of PayloadHistory (database name payload_history_PK) */
    val pk = primaryKey("payload_history_PK", (id, lang, os, createAt))

    /** Foreign key referencing AppHistory (database name fk_payload_history_app_history1) */
    lazy val appHistoryFk = foreignKey("fk_payload_history_app_history1", id, AppHistory)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)

    /** Index over (createAt) (database name idx_create_at_payload_history) */
    val index1 = index("idx_create_at_payload_history", createAt)
  }
  /** Collection-like TableQuery object for table PayloadHistory */
  lazy val PayloadHistory = new TableQuery(tag => new PayloadHistory(tag))


  /** GetResult implicit for fetching PayloadLiveRow objects using plain SQL queries */

  /** Table description of table payload_live. Objects of this class serve as prototypes for rows in queries. */
  class PayloadLive(_tableTag: Tag) extends Table[PayloadRow](_tableTag, "payload_live") {
    def * = (id, lang, os, createAt, updateAt, reviewStatus, url, size, mime, attachmentSetId, title, shortDescription, description, instruction, installation, additionalInfo, knownIssue, supportInfo, version, versionDescription, upgradeCode, productCode) <> (PayloadRow.tupled, PayloadRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(lang), Rep.Some(os), Rep.Some(createAt), Rep.Some(updateAt), reviewStatus, Rep.Some(url), size, mime, Rep.Some(attachmentSetId), Rep.Some(title), Rep.Some(shortDescription), Rep.Some(description), Rep.Some(instruction), Rep.Some(installation), Rep.Some(additionalInfo), Rep.Some(knownIssue), Rep.Some(supportInfo), Rep.Some(version), Rep.Some(versionDescription), Rep.Some(upgradeCode), Rep.Some(productCode)).shaped.<>({r=>import r._; _1.map(_=> PayloadRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get, _8, _9, _10.get, _11.get, _12.get, _13.get, _14.get, _15.get, _16.get, _17.get, _18.get, _19.get, _20.get, _21.get, _22.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED) */
    val id: Rep[Long] = column[Long]("id")
    /** Database column lang SqlType(ENUM), Length(5,false) */
    val lang: Rep[Lang] = column[Lang]("lang", O.Length(5,varying=false))
    /** Database column os SqlType(ENUM), Length(8,false) */
    val os: Rep[Platform] = column[Platform]("os", O.Length(8,varying=false))
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column update_at SqlType(TIMESTAMP) */
    val updateAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("update_at")
    /** Database column review_status SqlType(ENUM), Length(28,false), Default(Some(live)) */
    val reviewStatus: Rep[Option[PayloadStatus]] = column[Option[PayloadStatus]]("review_status", O.Length(28,varying=false), O.Default(Some("live")))
    /** Database column url SqlType(VARCHAR), Length(1023,true) */
    val url: Rep[String] = column[String]("url", O.Length(1023,varying=true))
    /** Database column size SqlType(INT), Default(None) */
    val size: Rep[Option[Int]] = column[Option[Int]]("size", O.Default(None))
    /** Database column mime SqlType(ENUM), Length(24,false), Default(None) */
    val mime: Rep[Option[String]] = column[Option[String]]("mime", O.Length(24,varying=false), O.Default(None))
    /** Database column attachment_set_id SqlType(BIGINT UNSIGNED) */
    val attachmentSetId: Rep[Long] = column[Long]("attachment_set_id")
    /** Database column title SqlType(VARCHAR), Length(100,true) */
    val title: Rep[String] = column[String]("title", O.Length(100,varying=true))
    /** Database column short_description SqlType(VARCHAR), Length(511,true) */
    val shortDescription: Rep[String] = column[String]("short_description", O.Length(511,varying=true))
    /** Database column description SqlType(TEXT) */
    val description: Rep[String] = column[String]("description")
    /** Database column instruction SqlType(TEXT) */
    val instruction: Rep[String] = column[String]("instruction")
    /** Database column installation SqlType(TEXT) */
    val installation: Rep[String] = column[String]("installation")
    /** Database column additional_info SqlType(TEXT) */
    val additionalInfo: Rep[String] = column[String]("additional_info")
    /** Database column known_issue SqlType(TEXT) */
    val knownIssue: Rep[String] = column[String]("known_issue")
    /** Database column support_info SqlType(TEXT) */
    val supportInfo: Rep[String] = column[String]("support_info")
    /** Database column version SqlType(VARCHAR), Length(45,true) */
    val version: Rep[String] = column[String]("version", O.Length(45,varying=true))
    /** Database column version_description SqlType(TEXT) */
    val versionDescription: Rep[String] = column[String]("version_description")
    /** Database column upgrade_code SqlType(VARCHAR), Length(64,true) */
    val upgradeCode: Rep[String] = column[String]("upgrade_code", O.Length(64,varying=true))
    /** Database column product_code SqlType(VARCHAR), Length(64,true) */
    val productCode: Rep[String] = column[String]("product_code", O.Length(64,varying=true))

    /** Primary key of PayloadLive (database name payload_live_PK) */
    val pk = primaryKey("payload_live_PK", (id, lang, os))

    /** Foreign key referencing AppLive (database name fk_payload_app_header10) */
    lazy val appLiveFk = foreignKey("fk_payload_app_header10", id, AppLive)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table PayloadLive */
  lazy val PayloadLive = new TableQuery(tag => new PayloadLive(tag))


  /** GetResult implicit for fetching PricePlanRow objects using plain SQL queries */
  implicit def GetResultPricePlanRow(implicit e0: GR[Long], e1: GR[java.sql.Timestamp], e2: GR[String], e3: GR[Option[String]], e4: GR[scala.math.BigDecimal], e5: GR[Option[scala.math.BigDecimal]]): GR[PricePlanRow] = GR{
    prs => import prs._
    PricePlanRow.tupled((<<[Long], <<[java.sql.Timestamp], <<[Long], <<[String], <<?[String], <<[scala.math.BigDecimal], <<[scala.math.BigDecimal], <<?[scala.math.BigDecimal], <<[String], <<?[String], <<?[String]))
  }
  /** Table description of table price_plan. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class PricePlan(_tableTag: Tag) extends Table[PricePlanRow](_tableTag, "price_plan") {
    def * = (id, createAt, appId, `type`, gateway, totalPrice, unitPrice, discount, currency, restriction, description) <> (PricePlanRow.tupled, PricePlanRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(createAt), Rep.Some(appId), Rep.Some(`type`), gateway, Rep.Some(totalPrice), Rep.Some(unitPrice), discount, Rep.Some(currency), restriction, description).shaped.<>({r=>import r._; _1.map(_=> PricePlanRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7.get, _8, _9.get, _10, _11)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column app_id SqlType(BIGINT UNSIGNED) */
    val appId: Rep[Long] = column[Long]("app_id")
    /** Database column type SqlType(ENUM), Length(12,false), Default(one_time)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type", O.Length(12,varying=false), O.Default("one_time"))
    /** Database column gateway SqlType(VARCHAR), Length(45,true), Default(None) */
    val gateway: Rep[Option[String]] = column[Option[String]]("gateway", O.Length(45,varying=true), O.Default(None))
    /** Database column total_price SqlType(DECIMAL UNSIGNED), Default(0.00) */
    val totalPrice: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("total_price", O.Default(scala.math.BigDecimal(0.00)))
    /** Database column unit_price SqlType(DECIMAL UNSIGNED), Default(0.00) */
    val unitPrice: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("unit_price", O.Default(scala.math.BigDecimal(0.00)))
    /** Database column discount SqlType(DECIMAL), Default(Some(0.00)) */
    val discount: Rep[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("discount", O.Default(Some(scala.math.BigDecimal(0.00))))
    /** Database column currency SqlType(ENUM), Length(4,false), Default(USD) */
    val currency: Rep[String] = column[String]("currency", O.Length(4,varying=false), O.Default("USD"))
    /** Database column restriction SqlType(TEXT), Default(None) */
    val restriction: Rep[Option[String]] = column[Option[String]]("restriction", O.Default(None))
    /** Database column description SqlType(TEXT), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))

    /** Foreign key referencing App (database name fk_PRICE_PLAN_APP_HEADER1) */
    lazy val appFk = foreignKey("fk_PRICE_PLAN_APP_HEADER1", appId, App)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table PricePlan */
  lazy val PricePlan = new TableQuery(tag => new PricePlan(tag))


  /** GetResult implicit for fetching PricePlanHistoryRow objects using plain SQL queries */

  /** Table description of table price_plan_history. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class PricePlanHistory(_tableTag: Tag) extends Table[PricePlanRow](_tableTag, "price_plan_history") {
    def * = (id, createAt, appId, `type`, gateway, totalPrice, unitPrice, discount, currency, restriction, description) <> (PricePlanRow.tupled, PricePlanRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(createAt), Rep.Some(appId), Rep.Some(`type`), gateway, Rep.Some(totalPrice), Rep.Some(unitPrice), discount, Rep.Some(currency), restriction, description).shaped.<>({r=>import r._; _1.map(_=> PricePlanRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7.get, _8, _9.get, _10, _11)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED) */
    val id: Rep[Long] = column[Long]("id")
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column app_id SqlType(BIGINT UNSIGNED) */
    val appId: Rep[Long] = column[Long]("app_id")
    /** Database column type SqlType(ENUM), Length(12,false), Default(one_time)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type", O.Length(12,varying=false), O.Default("one_time"))
    /** Database column gateway SqlType(VARCHAR), Length(45,true), Default(None) */
    val gateway: Rep[Option[String]] = column[Option[String]]("gateway", O.Length(45,varying=true), O.Default(None))
    /** Database column total_price SqlType(DECIMAL UNSIGNED), Default(0.00) */
    val totalPrice: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("total_price", O.Default(scala.math.BigDecimal(0.00)))
    /** Database column unit_price SqlType(DECIMAL UNSIGNED), Default(0.00) */
    val unitPrice: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("unit_price", O.Default(scala.math.BigDecimal(0.00)))
    /** Database column discount SqlType(DECIMAL UNSIGNED), Default(Some(0.00)) */
    val discount: Rep[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("discount", O.Default(Some(scala.math.BigDecimal(0.00))))
    /** Database column currency SqlType(ENUM), Length(4,false), Default(USD) */
    val currency: Rep[String] = column[String]("currency", O.Length(4,varying=false), O.Default("USD"))
    /** Database column restriction SqlType(TEXT), Default(None) */
    val restriction: Rep[Option[String]] = column[Option[String]]("restriction", O.Default(None))
    /** Database column description SqlType(TEXT), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))

    /** Primary key of PricePlanHistory (database name price_plan_history_PK) */
    val pk = primaryKey("price_plan_history_PK", (id, createAt, appId))

    /** Foreign key referencing AppHistory (database name fk_price_plan_history_app_history1) */
    lazy val appHistoryFk = foreignKey("fk_price_plan_history_app_history1", appId, AppHistory)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PricePlanHistory */
  lazy val PricePlanHistory = new TableQuery(tag => new PricePlanHistory(tag))


  /** GetResult implicit for fetching PricePlanLiveRow objects using plain SQL queries */

  /** Table description of table price_plan_live. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class PricePlanLive(_tableTag: Tag) extends Table[PricePlanRow](_tableTag, "price_plan_live") {
    def * = (id, createAt, appId, `type`, gateway, totalPrice, unitPrice, discount, currency, restriction, description) <> (PricePlanRow.tupled, PricePlanRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(createAt), Rep.Some(appId), Rep.Some(`type`), gateway, Rep.Some(totalPrice), Rep.Some(unitPrice), discount, Rep.Some(currency), restriction, description).shaped.<>({r=>import r._; _1.map(_=> PricePlanRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7.get, _8, _9.get, _10, _11)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.PrimaryKey)
    /** Database column create_at SqlType(TIMESTAMP) */
    val createAt: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("create_at")
    /** Database column app_id SqlType(BIGINT UNSIGNED) */
    val appId: Rep[Long] = column[Long]("app_id")
    /** Database column type SqlType(ENUM), Length(12,false), Default(one_time)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[String] = column[String]("type", O.Length(12,varying=false), O.Default("one_time"))
    /** Database column gateway SqlType(VARCHAR), Length(45,true), Default(None) */
    val gateway: Rep[Option[String]] = column[Option[String]]("gateway", O.Length(45,varying=true), O.Default(None))
    /** Database column total_price SqlType(DECIMAL UNSIGNED), Default(0.00) */
    val totalPrice: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("total_price", O.Default(scala.math.BigDecimal(0.00)))
    /** Database column unit_price SqlType(DECIMAL UNSIGNED), Default(0.00) */
    val unitPrice: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("unit_price", O.Default(scala.math.BigDecimal(0.00)))
    /** Database column discount SqlType(DECIMAL UNSIGNED), Default(Some(0.00)) */
    val discount: Rep[Option[scala.math.BigDecimal]] = column[Option[scala.math.BigDecimal]]("discount", O.Default(Some(scala.math.BigDecimal(0.00))))
    /** Database column currency SqlType(ENUM), Length(4,false), Default(USD) */
    val currency: Rep[String] = column[String]("currency", O.Length(4,varying=false), O.Default("USD"))
    /** Database column restriction SqlType(TEXT), Default(None) */
    val restriction: Rep[Option[String]] = column[Option[String]]("restriction", O.Default(None))
    /** Database column description SqlType(TEXT), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))

    /** Foreign key referencing AppLive (database name fk_PRICE_PLAN_APP_HEADER10) */
    lazy val appLiveFk = foreignKey("fk_PRICE_PLAN_APP_HEADER10", appId, AppLive)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.Cascade)
  }
  /** Collection-like TableQuery object for table PricePlanLive */
  lazy val PricePlanLive = new TableQuery(tag => new PricePlanLive(tag))


  /** GetResult implicit for fetching RatingRow objects using plain SQL queries */
  implicit def GetResultRatingRow(implicit e0: GR[Long], e1: GR[Short], e2: GR[scala.math.BigDecimal]): GR[RatingRow] = GR{
    prs => import prs._
    RatingRow.tupled((<<[Long], <<[Short], <<[Short], <<[Short], <<[Short], <<[Short], <<[Short], <<[scala.math.BigDecimal]))
  }
  /** Table description of table rating. Objects of this class serve as prototypes for rows in queries. */
  class Rating(_tableTag: Tag) extends Table[RatingRow](_tableTag, "rating") {
    def * = (appId, count1, count2, count3, count4, count5, countSum, averageRating) <> (RatingRow.tupled, RatingRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(appId), Rep.Some(count1), Rep.Some(count2), Rep.Some(count3), Rep.Some(count4), Rep.Some(count5), Rep.Some(countSum), Rep.Some(averageRating)).shaped.<>({r=>import r._; _1.map(_=> RatingRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column app_id SqlType(BIGINT UNSIGNED), PrimaryKey */
    val appId: Rep[Long] = column[Long]("app_id", O.PrimaryKey)
    /** Database column count1 SqlType(SMALLINT UNSIGNED), Default(0) */
    val count1: Rep[Short] = column[Short]("count1", O.Default(0))
    /** Database column count2 SqlType(SMALLINT UNSIGNED), Default(0) */
    val count2: Rep[Short] = column[Short]("count2", O.Default(0))
    /** Database column count3 SqlType(SMALLINT UNSIGNED), Default(0) */
    val count3: Rep[Short] = column[Short]("count3", O.Default(0))
    /** Database column count4 SqlType(SMALLINT UNSIGNED), Default(0) */
    val count4: Rep[Short] = column[Short]("count4", O.Default(0))
    /** Database column count5 SqlType(SMALLINT UNSIGNED), Default(0) */
    val count5: Rep[Short] = column[Short]("count5", O.Default(0))
    /** Database column count_sum SqlType(SMALLINT UNSIGNED), Default(0) */
    val countSum: Rep[Short] = column[Short]("count_sum", O.Default(0))
    /** Database column average_rating SqlType(DECIMAL UNSIGNED), Default(0.00) */
    val averageRating: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("average_rating", O.Default(scala.math.BigDecimal(0.00)))
  }
  /** Collection-like TableQuery object for table Rating */
  lazy val Rating = new TableQuery(tag => new Rating(tag))


  /** GetResult implicit for fetching SettingsRow objects using plain SQL queries */
  implicit def GetResultSettingsRow(implicit e0: GR[String], e1: GR[Option[String]]): GR[Metadata] = GR{
    prs => import prs._
    (Metadata.apply _).tupled((<<[String], <<?[String], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table settings. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type */
  class Settings(_tableTag: Tag) extends Table[Metadata](_tableTag, "settings") {
    def * = (id, parent, name, `type`, value) <> ((Metadata.apply _).tupled, Metadata.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), parent, name, `type`, value).shaped.<>({r=>import r._; _1.map(_=> (Metadata.apply _).tupled((_1.get, _2, _3, _4, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(VARCHAR), PrimaryKey, Length(128,true) */
    val id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(128,varying=true))
    /** Database column parent SqlType(VARCHAR), Length(128,true), Default(None) */
    val parent: Rep[Option[String]] = column[Option[String]]("parent", O.Length(128,varying=true), O.Default(None))
    /** Database column name SqlType(VARCHAR), Length(128,true), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Length(128,varying=true), O.Default(None))
    /** Database column type SqlType(MEDIUMTEXT), Length(16777215,true), Default(None)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `type`: Rep[Option[String]] = column[Option[String]]("type", O.Length(16777215,varying=true), O.Default(None))
    /** Database column value SqlType(TEXT), Default(None) */
    val value: Rep[Option[String]] = column[Option[String]]("value", O.Default(None))
  }
  /** Collection-like TableQuery object for table Settings */
  lazy val Settings = new TableQuery(tag => new Settings(tag))


  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[String], e1: GR[Option[String]], e2: GR[Option[Long]]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[String], <<?[String], <<?[String], <<?[String], <<?[String], <<?[Long]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends Table[UserRow](_tableTag, "user") {
    def * = (id, email, firstName, lastName, oxygenId, companyId) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), email, firstName, lastName, oxygenId, companyId).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2, _3, _4, _5, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(CHAR), PrimaryKey, Length(20,false) */
    val id: Rep[String] = column[String]("id", O.PrimaryKey, O.Length(20,varying=false))
    /** Database column email SqlType(VARCHAR), Length(45,true), Default(None) */
    val email: Rep[Option[String]] = column[Option[String]]("email", O.Length(45,varying=true), O.Default(None))
    /** Database column first_name SqlType(VARCHAR), Length(45,true), Default(None) */
    val firstName: Rep[Option[String]] = column[Option[String]]("first_name", O.Length(45,varying=true), O.Default(None))
    /** Database column last_name SqlType(VARCHAR), Length(45,true), Default(None) */
    val lastName: Rep[Option[String]] = column[Option[String]]("last_name", O.Length(45,varying=true), O.Default(None))
    /** Database column oxygen_id SqlType(VARCHAR), Length(45,true), Default(None) */
    val oxygenId: Rep[Option[String]] = column[Option[String]]("oxygen_id", O.Length(45,varying=true), O.Default(None))
    /** Database column company_id SqlType(BIGINT UNSIGNED), Default(None) */
    val companyId: Rep[Option[Long]] = column[Option[Long]]("company_id", O.Default(None))

    /** Foreign key referencing Company (database name fk_user_company1) */
    lazy val companyFk = foreignKey("fk_user_company1", companyId, Company)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))
}

object Models {
  import com.acrd.giraffe.common.implicits.JsonFormats._
  import play.api.i18n.Lang
  import play.api.libs.json.Json._
  import com.acrd.giraffe.models.AppStoreTables
  import AppStoreTables.CommentStatus._
  import AppStoreTables.PayloadStatus._
  import AppStoreTables.Platform._
   
  implicit val ActionLogRowFormatter = format[ActionLogRow]
  case class ActionLogRow(id: Long, timestamp: java.sql.Timestamp, action: Option[String] = None, fromState: Option[String] = None, toState: Option[String] = None, msg1: Option[String] = None, msg2: Option[String] = None, parentId: Long, userId: Option[String] = None, `type`: Option[String] = None)
             
  implicit val AppRowFormatter = format[AppRow]
  case class AppRow(id: Long, createAt: java.sql.Timestamp, updateAt: java.sql.Timestamp, title: String, authorId: String, icon: String, supportContact: Option[String] = None, webServiceIdentification: Option[String] = None, prodVerMap: Option[String] = None)
             
  implicit val AttachmentRowFormatter = format[AttachmentRow]
  case class AttachmentRow(id: Long, order: Option[Int] = Some(0), uri: Option[String] = None, size: Option[Int] = None, mime: Option[String] = None, `type`: Option[String] = None, value: Option[String] = None, shortDescription: Option[String] = None, description: Option[String] = None, setId: Option[Long] = None, appId: Option[Long] = None)
             
  implicit val CommentRowFormatter = format[CommentRow]
  case class CommentRow(id: Long, appId: Long, author: String, lang: Option[Lang] = Some(Lang("en")), title: Option[String] = None, text: Option[String] = None, createAt: java.sql.Timestamp, updateAt: java.sql.Timestamp, status: CommentStatus = "preview", rating: Int = 0, replyTo: Option[Long] = None, storeId: Option[String] = None, isVerifiedDownload: Option[Boolean] = Some(false))
             
  implicit val CompanyRowFormatter = format[CompanyRow]
  case class CompanyRow(id: Long, email: Option[String] = None, address: Option[String] = None, services: Option[Int] = None)
             
  implicit val FacetRowFormatter = format[FacetRow]
  case class FacetRow(id: Long, facet: String)
             
  implicit val PayloadRowFormatter = format[PayloadRow]
  case class PayloadRow(id: Long, lang: Lang, os: Platform, createAt: java.sql.Timestamp, updateAt: java.sql.Timestamp, reviewStatus: Option[PayloadStatus] = Some("draft"), url: String, size: Option[Int] = None, mime: Option[String] = None, attachmentSetId: Long, title: String, shortDescription: String, description: String, instruction: String, installation: String, additionalInfo: String, knownIssue: String, supportInfo: String, version: String, versionDescription: String, upgradeCode: String, productCode: String)
             
  implicit val PricePlanRowFormatter = format[PricePlanRow]
  case class PricePlanRow(id: Long, createAt: java.sql.Timestamp, appId: Long, `type`: String = "one_time", gateway: Option[String] = None, totalPrice: scala.math.BigDecimal = scala.math.BigDecimal(0.00), unitPrice: scala.math.BigDecimal = scala.math.BigDecimal(0.00), discount: Option[scala.math.BigDecimal] = Some(scala.math.BigDecimal(0.00)), currency: String = "USD", restriction: Option[String] = None, description: Option[String] = None)
             
  implicit val RatingRowFormatter = format[RatingRow]
  case class RatingRow(appId: Long, count1: Short = 0, count2: Short = 0, count3: Short = 0, count4: Short = 0, count5: Short = 0, countSum: Short = 0, averageRating: scala.math.BigDecimal = scala.math.BigDecimal(0.00))
             
  implicit val UserRowFormatter = format[UserRow]
  case class UserRow(id: String, email: Option[String] = None, firstName: Option[String] = None, lastName: Option[String] = None, oxygenId: Option[String] = None, companyId: Option[Long] = None)
             
 }
         
