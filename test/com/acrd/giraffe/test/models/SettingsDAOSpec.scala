package com.acrd.giraffe.test.models

import com.acrd.giraffe.base.TestingContext
import com.acrd.giraffe.models.Metadata
import com.acrd.giraffe.dao.SettingsDAO
import com.acrd.giraffe.test.init.{Initializer => Init, SettingsInitializer => SI}
import org.junit.runner._
import org.specs2.runner._
import play.api.test._
import play.api.libs.concurrent.Execution.Implicits._

@RunWith(classOf[JUnitRunner])
class SettingsDAOSpec extends PlaySpecification {

  "run a test against Settings model" in new WithApplication{

    Init[SI]
    import com.acrd.giraffe.common.implicits.Options._
    val dao = new SettingsDAO(new TestingContext)
    val root = Metadata("root", None, "Root Node", "string", "I am root settings")
    val leaf1 = Metadata("leaf1", "root", "Leaf Node 1", "int", "1")
    val leaf2 = Metadata("leaf2", "root", "Leaf Node 2", "json", "{foo:bar}")
    val illegalLeaf = Metadata("leafWithWrongParent", None, "Leaf Node 3", "json", "{foo:bar}")

    root.children = Some(Seq(leaf1, leaf2, illegalLeaf))

    var result = await(dao.insert(root))
    result.isFailure must beTrue

    root.children = Some(Seq(leaf1, leaf2))

    result = await(dao.insert(root))
    result.isSuccess must beTrue

    result = await(dao.insert(root))
    result.isFailure must beTrue

    /**
     * Since some of the native sql is not supported by H2, query/delete cannot be tested
     */
  }

}
