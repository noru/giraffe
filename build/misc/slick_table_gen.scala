/**
 * Script file for generating slick tables
 * Change the parameters accordingly, and run it in Intellij as Scala Script
 *
 * TODO: should be able to run as a standalone script, that is, assign class
 * path for local jar inline and ability to resolve maven dependencies
 */

val slickDriver = "slick.driver.MySQLDriver"
val jdbcDriver = "com.mysql.jdbc.Driver"
val url = "jdbc:mysql://localhost:3306/giraffe"
val outputFolder = "./app"
val pkg = "com.acrd.giraffe.models.gen"
val user = "root"
val password = "root"

import slick.codegen.SourceCodeGenerator
import slick.driver.JdbcProfile
import slick.model.Model

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class CustomizeGenerator(model: Model) extends SourceCodeGenerator(model) {

  var entities = ""

  def shouldBypass(name: String): Boolean = {
    val bypassKeywords = Seq("Live", "History", "Settings")
    bypassKeywords.exists(name.contains)
  }

  override def code =
    """
      |import Models._
      |import play.api.i18n.Lang
      |import com.acrd.giraffe.models.Metadata
      |import com.acrd.giraffe.models.AppStoreTables
      |import AppStoreTables.CommentStatus._
      |import AppStoreTables.Platform._
      |import AppStoreTables.PayloadStatus._
      |implicit def mapStringLang = MappedColumnType.base[Lang, String](_.code, Lang(_))
      |implicit def mapStringCommentStatus = MappedColumnType.base[CommentStatus, String](_.toString, {a=>a})
      |implicit def mapStringPlatform = MappedColumnType.base[Platform, String](_.toString, {a=>a})
      |implicit def mapStringPayloadStatus = MappedColumnType.base[PayloadStatus, String](_.toString, {a=>a})
    """.stripMargin + "\n" + super.code

  override def Table = new Table(_){
    /**
     * Customize: fix a issue on mapping DB type Decimal (https://github.com/slick/slick/issues/1000)
     * and add mapping for type Lang
     */
    override def Column = new Column(_) {
      override def rawType = {
        model.name match {
          case "lang" => "Lang"
          case "status" => "CommentStatus"
          case "os" => "Platform"
          case "review_status" => "PayloadStatus"
          case "rating" => "Int"
          case _ => super.rawType
        }
      }
      override def defaultCode = v => {
        def raw(v: Any) = rawType match {
          case "Lang" => "Lang(\"" + v + "\")"
          case "String" | "CommentStatus" | "Platform" | "PayloadStatus" => "\"" + v + "\""
          case "Long" => v + "L"
          case "Float" => v + "F"
          case "Char" => "'" + v + "'"
          case "scala.math.BigDecimal" => s"scala.math.BigDecimal($v)"
          case "Byte" | "Short" | "Int" | "Double" | "Boolean" => v.toString
        }
        v match {
          case Some(x) => s"Some(${raw(x)})"
          case None    => "None"
          case x       => raw(x)
        }
      }
    }
    override def EntityType = new EntityTypeDef {

      override def doc: String = ""

      override def code: String = {
        entities += {
          /**
           * Customize: add default json R/W for every case class, for duplicate live/history table,
           * skip the entity generation
           */
          if (classEnabled && !shouldBypass(name.toString)){
            s"""
               |implicit val ${name}Formatter = format[$name]
               |${super.code}
           """.stripMargin
          } else ""
        }
        "" // do not create entities under Tables trait
      }
    }
    override def TableClass = new TableClassDef {

      /** for Live/History table, use the same entity generated from preview table
       */
      override def elementType: String = {
        if (shouldBypass(name)){
          super.elementType.replace("Live", "").replace("History", "").replace("SettingsRow", "Metadata")
        } else super.elementType
      }

      /** for Settings table, use models.Metadata as entity instead of default
       * @return
       */
      override def code = if (elementType == "Metadata") super.code.replaceAll("Metadata.tupled", "(Metadata.apply _).tupled") else super.code
    }

    override def PlainSqlMapper = new PlainSqlMapperDef {
      override def code = if (TableClass.elementType == "Metadata") super.code.replaceAll("Metadata.tupled", "(Metadata.apply _).tupled")
                          else if (shouldBypass(name)) ""
                          else super.code
    }
  }

  /**
   * Customize 3: Move all case classes to a separate object
   */
  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String])
      = super.packageCode(profile, pkg, container, parentType) + "\n" +
        s"""
           |object Models {
           |  import com.acrd.giraffe.common.implicits.JsonFormats._
           |  import play.api.i18n.Lang
           |  import play.api.libs.json.Json._
           |  import com.acrd.giraffe.models.AppStoreTables
           |  import AppStoreTables.CommentStatus._
           |  import AppStoreTables.PayloadStatus._
           |  import AppStoreTables.Platform._
           |   ${indent(entities)}
           | }
         """.stripMargin
}

object CustomizeGenerator{
  def run(slickDriver: String, jdbcDriver: String, url: String, outputDir: String, pkg: String, user: Option[String], password: Option[String]): Unit = {
    val driver: JdbcProfile =
      Class.forName(slickDriver + "$").getField("MODULE$").get(null).asInstanceOf[JdbcProfile]
    val dbFactory = driver.api.Database
    val db = dbFactory.forURL(url, driver = jdbcDriver,
      user = user.orNull, password = password.orNull, keepAliveConnection = true)
    try {
      val m = Await.result(db.run(driver.createModel(None, ignoreInvalidDefaults = true)(ExecutionContext.global).withPinnedSession), Duration.Inf)
      new CustomizeGenerator(m).writeToFile(slickDriver,outputDir,pkg)
    } finally db.close
  }
}

CustomizeGenerator.run(slickDriver, jdbcDriver, url, outputFolder, pkg, Option(user), Option(password))
