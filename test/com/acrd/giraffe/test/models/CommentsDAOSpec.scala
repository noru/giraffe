package com.acrd.giraffe.test.models

import com.acrd.giraffe.common.exceptions.InvalidParameterException
import com.acrd.giraffe.models.{CommentBody, AppStoreTables}
import com.acrd.giraffe.models.gen.Models
import Models.RatingRow
import com.acrd.giraffe.base.TestingContext
import com.acrd.giraffe.dao.CommentDAO
import CommentBody.isEqual
import com.acrd.giraffe.test.init.TestingData.Comments._
import com.acrd.giraffe.test.init.TestingData.App._
import com.acrd.giraffe.test.init.{Initializer => Init, CommentsInitializer => CI}
import AppStoreTables.CommentStatus
import org.junit.runner._
import org.specs2.runner._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class CommentsDAOSpec extends PlaySpecification {

  import com.acrd.giraffe.common.implicits.Options._

  "Comment Dao" should {

    "static method: sum" in {
      val rating = RatingRow(123L, 1, 0, 0, 0, 5, 6, 26/6 )

      // single input
      var newRating = CommentDAO.sum(rating, 3)
      newRating.count3 must beEqualTo(1)
      newRating.countSum must beEqualTo(7)
      newRating.averageRating must beEqualTo(29d/7)
      newRating = CommentDAO.sum(newRating, 5)
      newRating.count5 must beEqualTo(6)
      newRating.countSum must beEqualTo(8)
      newRating.averageRating must beEqualTo(34d/8)

      // multi input
      newRating = CommentDAO.sum(rating, 1, 2, 3, 4, -5)
      newRating.countSum mustEqual 9
      newRating.count1 mustEqual 2
      newRating.count2 mustEqual 1
      newRating.count3 mustEqual 1
      newRating.count4 mustEqual 1
      newRating.count5 mustEqual 4
      newRating.averageRating mustEqual 31d/9

      // edge case
      rating mustEqual CommentDAO.sum(rating)
      rating mustEqual CommentDAO.sum(rating, 0)
      CommentDAO.sum(rating, -6) must throwA[InvalidParameterException]
      CommentDAO.sum(rating, 100) must throwA[InvalidParameterException]
      CommentDAO.sum(rating, -3) must throwA[InvalidParameterException]
      val wrongSum = rating.copy(countSum = 10)
      CommentDAO.sum(wrongSum, 3) must throwA[InvalidParameterException]

    }

    "static method: getNewRatingRow" in {
      val newRating = CommentDAO.getNewRatingRow(123L)
      newRating.count1 must beEqualTo(0)
      newRating.count2 must beEqualTo(0)
      newRating.count3 must beEqualTo(0)
      newRating.count4 must beEqualTo(0)
      newRating.count5 must beEqualTo(0)
      newRating.countSum must beEqualTo(0)
      newRating.averageRating must beEqualTo(0)
    }


    "insert a comment" in new WithApplication{

      Init[CI]

      val dao = new CommentDAO(new TestingContext)

      var body = await(dao.insert(comment2))
      body.isDefined must beTrue
      isEqual(comment2, body.get) must beTrue

    }

    "get a comment by id" in new WithApplication{

      Init[CI]

      val dao = new CommentDAO(new TestingContext)
      var getById = await(dao.getById(comment2.id))
      getById.isEmpty must beTrue

      val body = await(dao.insert(comment2))
      getById = await(dao.getById(body.get.id))
      getById.isDefined must beTrue
      isEqual(comment2, body.get) must beTrue

    }

    "get all comments" in new WithApplication{
      Init[CI]
      val dao = new CommentDAO(new TestingContext)
      var get = await(dao.get())
      get must not beNull

      get.count must be_==(2)
      get.comments must have size 2
    }

    "get all comments with paging" in new WithApplication{
      Init[CI]
      val dao = new CommentDAO(new TestingContext)
      await(dao.insert(comment2))
      var get = await(dao.get(skip = 1, top = 1))
      get must not beNull

      get.count must beEqualTo(3)
      get.comments must have size 1
    }

    "get comments by app id" in new WithApplication{

      Init[CI]

      val dao = new CommentDAO(new TestingContext)
      var get = await(dao.get(appId = app1.id))
      get must not beNull

      get.count must beEqualTo(2)
      get.comments must have size 2

      await(dao.insert(comment2))
      get = await(dao.get(appId = app1.id))
      get.comments must have size 3

    }

    "delete a comment by id" in new WithApplication{

      Init[CI]

      val dao = new CommentDAO(new TestingContext)
      val id = await(dao.insert(comment2)).get.id
      val int = await(dao.delete(id))
      int.get must beEqualTo(1)

    }
    "update a comment" in new WithApplication{

      Init[CI]

      val dao = new CommentDAO(new TestingContext)
      val id = await(dao.insert(comment2)).get.id
      val response = await(dao.update(id, comment2.copy(text = "changed")))
      response must beAnInstanceOf[CommentBody]
      response.text must beEqualTo(Some("changed"))

      val result = await(dao.getById(id)).get
      result.text.get must beEqualTo("changed")
      result.rating must beEqualTo(5)
      result.status must beEqualTo(CommentStatus.Preview)

    }

    "update a comment status" in new WithApplication{

      Init[CI]

      import CommentStatus.{Live, Preview}

      val dao = new CommentDAO(new TestingContext)
      val id = await(dao.insert(comment2)).get.id
      var response = await(dao.update(id, comment2.copy(status = Some(Preview))))
      response must beAnInstanceOf[CommentBody]

      var result = await(dao.getById(id)).get
      result.status must beEqualTo(Preview)

      await(dao.update(id, comment2.copy(status = Some(Live))))
      result = await(dao.getById(id)).get
      result.status must beEqualTo(Live)

      var rating = await(dao.getAppRating(app1.id)).get
      rating.averageRating must beEqualTo(4)
      rating.count5 must beEqualTo(1)

      response = await(dao.update(id, comment2.copy(status = Some(Preview))))
      rating = await(dao.getAppRating(app1.id)).get
      rating.averageRating must beEqualTo(3)
      rating.count5 must beEqualTo(0)

    }

    "update a rating" in new WithApplication{

      Init[CI]

      val dao = new CommentDAO(new TestingContext)
      val id = await(dao.insert(comment1)).get.id

      var result = await(dao.update(id, comment1.copy(rating = 4)))
      var rating = await(dao.getAppRating(app1.id)).get
      rating.averageRating mustEqual 3.5
      rating.count5 mustEqual 0
      rating.count4 mustEqual 1


    }

  }

}
