package com.acrd.giraffe.test.others

import com.acrd.giraffe.common.Consts.LogicOperand._
import com.acrd.giraffe.common.implicits.Options
import com.acrd.giraffe.common.utils._
import com.acrd.giraffe.dao.AppDAO.SortOption._
import org.junit.runner._
import org.specs2.runner._
import play.api.test.PlaySpecification

@RunWith(classOf[JUnitRunner])
class FilterAndSqlBuilderSpec extends PlaySpecification {

  import com.acrd.giraffe.common.implicits.Options._

  "Filter Helper class" should {

    "parse a filter string correctly" in {

      val raw = "type eq avant_garde and brand nq VW and transmission nq MT and size eq full_size_SUV"

      val facets = FilterHelper.parse(raw)

      facets must have size 4
      facets must contain(Filter("type", "avant_garde"))

    }
  }

  "SqlBuilder" should {

    "generate clean sql" in {

      val sql = new SqlBuilder().q

      sql.contains("select * from app") must beTrue
      sql.contains("order by update_at") must beTrue
      sql.contains("limit 5000") must beTrue
      sql.contains("offset 0") must beTrue
      sql.contains("where") must beFalse

    }

    "sql with skip/top/order" in {

      import Options._
      val sql = new SqlBuilder().withParameters(10, 20, Some(CreateAt)).q
      sql.contains("select * from app") must beTrue
      sql.contains("order by create_at") must beTrue
      sql.contains("limit 20") must beTrue
      sql.contains("offset 10") must beTrue
      sql.contains("where") must beFalse

    }

    "sql where clause with text attribute Filter" in {

      val title = Filter("title", "'The Song of Ice And Fire'")
      val sql = new SqlBuilder().withFilters(title).q
      sql.contains("select * ,match (title) against ('The Song of Ice And Fire' in boolean mode) as relevance_title from app") must beTrue
      sql.contains("order by relevance_title desc, update_at") must beTrue
      sql.contains("limit 5000") must beTrue
      sql.contains("offset 0") must beTrue
      sql.contains("where match (title) against ('The Song of Ice And Fire' in boolean mode)") must beTrue

    }

    "sql where clause with multiple text attribute Filters" in {

      val title = Filter("title", "'The Song of Ice And Fire'")
      val title2 = Filter("title", "'The Song of Air And Dust'")
      val sql = new SqlBuilder().withFilters(title, title2).q
      sql.contains("select * ,match (title) against ('The Song of Ice And Fire' in boolean mode) as relevance_title,match (title) against ('The Song of Air And Dust' in boolean mode) as relevance_title from app") must beTrue
      sql.contains("order by relevance_title + relevance_title desc, update_at") must beTrue
      sql.contains("where match (title) against ('The Song of Ice And Fire' in boolean mode) and match (title) against ('The Song of Air And Dust' in boolean mode)") must beTrue

    }

    "sql where clause with root attribute Filters" in {

      val title = Filter("title", "'The Song of Ice And Fire'")
      val author = Filter("author_id", "'GRRM'")
      val sql = new SqlBuilder().withFilters(title, author).q
      sql.contains("select * ,match (title) against ('The Song of Ice And Fire' in boolean mode) as relevance_title from app") must beTrue
      sql.contains("order by relevance_title desc, update_at") must beTrue
      sql.contains("limit 5000") must beTrue
      sql.contains("offset 0") must beTrue
      sql.contains("where match (title) against ('The Song of Ice And Fire' in boolean mode) and author_id = 'GRRM'") must beTrue

    }

    "sql where clause with facet attribute Filters" in {

      val facet1 = Filter("$tag", "'naked_girl'")
      val facet2 = Filter("$category", "('xxx','18x')", IN)
      val sql = new SqlBuilder().withFilters(facet1, facet2).q
      sql.contains("facet in ('category::xxx','category::18x')") must beTrue
      sql.contains("facet in ('tag::naked_girl')") must beTrue

    }

    "sql where clause with root and facet attribute Filters" in {

      val facet1 = Filter("$tag", "'naked_girl'")
      val facet2 = Filter("$category", "('xxx','18x')", IN)
      val author = Filter("author_id", "'GRRM'")

      val sql = new SqlBuilder().withFilters(facet1, facet2, author).q
      sql.contains("facet in ('category::xxx','category::18x')") must beTrue
      sql.contains("facet in ('tag::naked_girl')") must beTrue
      sql.contains("author_id = 'GRRM'") must beTrue

    }

    "sql where clause with root, facet and sub attribute Filters" in {

      val facet1 = Filter("$tag", "'naked_girl'")
      val facet2 = Filter("$category", "('xxx','18x')", IN)
      val author = Filter("author_id", "'GRRM'")
      val status = Filter("review_status", "'draft'")

      val sql = new SqlBuilder().withFilters(facet1, facet2, author, status).q
      sql.contains("facet in ('category::xxx','category::18x')") must beTrue
      sql.contains("facet in ('tag::naked_girl')") must beTrue
      sql.contains("author_id = 'GRRM'") must beTrue
      sql.contains("review_status = 'draft'") must beTrue

    }

    "sql where clause with sub attribute filters, don't inner join for the first sub table" in {

      val status = Filter("review_status", "'draft'")
      val lang = Filter("lang", "'en'")

      val sql = new SqlBuilder().withFilters(status, lang).q

      sql.contains("inner join") must beFalse
      sql.contains("review_status = 'draft'") must beTrue
      sql.contains("lang = 'en'") must beTrue
    }

    "sql where clause with sub attribute filters, inner join only once for the same table" in {

      val price = Filter("total_price", "0.0")
      val status = Filter("review_status", "'draft'")
      val lang = Filter("lang", "'en'")

      val sql = new SqlBuilder().withFilters(price, status, lang).q

      val join = "inner join".r.findAllIn(sql)
      join.length must beEqualTo(1)
      sql.contains("review_status = 'draft'") must beTrue
      sql.contains("lang = 'en'") must beTrue
    }

    "generate the most complex sql" in {

      val facet1 = Filter("$tag", "'naked_girl'")
      val facet2 = Filter("$category", "('xxx','18x')", IN)
      val author = Filter("author_id", "'GRRM'")
      val status = Filter("review_status", "'draft'")
      val price = Filter("total_price", "0.0")
      val lang = Filter("lang", "'en'")

      val timer = new StopWatch
      timer.start
      val sql = new SqlBuilder().withFilters(facet1, facet2, author, status, price, lang)
        .withParameters(2, 1, Some(UpdateAt)).q

      val elapsed = timer.stop
      elapsed must beLessThan(10L)
      sql.contains("facet in ('category::xxx','category::18x')") must beTrue
      sql.contains("facet in ('tag::naked_girl')") must beTrue
      sql.contains("author_id = 'GRRM'") must beTrue
      sql.contains("review_status = 'draft'") must beTrue
      sql.contains("total_price = 0.0") must beTrue
      sql.contains("lang = 'en'") must beTrue
      sql.contains("order by update_at") must beTrue
      sql.contains("limit 1") must beTrue
      sql.contains("offset 2") must beTrue

    }

    "SqlBuilder InSetQuery" in {
      val query = SqlBuilder.InSetQuery("test", "id", Set(123))
      query.contains("select * from test where id in (123)") must beTrue
    }

    "FacetCondition class" in {

      import com.acrd.giraffe.common.Consts.LogicOperand._

      implicit val isLive = false
      var facet = Filter("$tag", "'naked_girl'")
      var sql = FacetCondition(facet).q

      sql.contains("select id from facet") must beTrue
      sql.contains("facet in ('tag::naked_girl')") must beTrue
      sql.contains("group by id") must beTrue
      sql.contains("having count(id) > 0") must beTrue


      facet = Filter("$tag", "('naked_girl','naked_woman')", IN)
      sql = FacetCondition(facet).q

      sql.contains("select id from facet") must beTrue
      sql.contains("facet in ('tag::naked_girl','tag::naked_woman')") must beTrue
      sql.contains("group by id") must beTrue
      sql.contains("having count(id) > 0") must beTrue


      facet = Filter("$tag", "('naked_girl','naked_woman','naked_grandma')", HAS)
      sql = FacetCondition(facet).q

      sql.contains("select id from facet") must beTrue
      sql.contains("facet in ('tag::naked_girl','tag::naked_woman','tag::naked_grandma')") must beTrue
      sql.contains("group by id") must beTrue
      sql.contains("having count(id) = 3") must beTrue

    }

  }
}
