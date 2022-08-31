package com.acrd.giraffe.test.controllers

import com.acrd.giraffe.base.{BaseContext, TestingContext}
import com.acrd.giraffe.common.implicits
import com.acrd.giraffe.models.{ActionLogBody, CommentSeqBody, CommentBody, AppStoreTables}
import com.acrd.giraffe.models.gen.Models
import Models.CommentRow
import com.acrd.giraffe.test.init.{Initializer => Init, CommentsInitializer => CI}
import com.acrd.giraffe.test.init.TestingData.Comments._
import AppStoreTables.CommentStatus.{Preview, Live}
import com.acrd.giraffe.test.init.TestingData._
import org.junit.runner._
import org.specs2.runner._
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test._

@RunWith(classOf[JUnitRunner])
class CommentsSpec extends PlaySpecification  {
  import implicits.Options._

  "Comments controller" should {

    def app = new GuiceApplicationBuilder().overrides(bind[BaseContext].to[TestingContext])

    "get all comments (no filter)" in new WithApplication(app.build) {
      Init[CI]

      var allComments = route(FakeRequest(GET, "/comments")).get
      status(allComments) must equalTo(OK)

      val json = contentAsJson(allComments)

      json.validate[CommentSeqBody].isSuccess must beTrue
      json.as[CommentSeqBody].comments must have size 2
      json.as[CommentSeqBody].count must beEqualTo(2)
    }

    "get a comment with pagination (skip=1 and top=1)" in new WithApplication(app.build) {
      Init[CI]

      val comments = route(FakeRequest(GET, "/comments?skip=1&top=1")).get
      status(comments) must equalTo(OK)

      val json = contentAsJson(comments).as[CommentSeqBody]
      json.comments must have size 1
      json.count must beEqualTo(2)
    }


    "get a comment by id" in new WithApplication(app.build) {
      Init[CI]

      val id = contentAsJson(route(FakeRequest(GET, "/comments")).get).as[CommentSeqBody].comments.head.id
      val foundComment = route(FakeRequest(GET, s"/comments/$id")).get
      status(foundComment) must equalTo(OK)

      val cmt = contentAsJson(foundComment).as[CommentRow]
      cmt.id must equalTo(id)
    }

    "get comments by replyTo" in new WithApplication(app.build) {
      Init[CI]

      val foundComment = route(FakeRequest(GET, "/comments?replyTo=1")).get
      status(foundComment) must equalTo(OK)

      val cmt = contentAsJson(foundComment).as[CommentSeqBody]
      cmt.comments must have size 0
    }

    "get comments by userId" in new WithApplication(app.build) {
      Init[CI]

      val author = User.user1.id
      val foundComment = route(FakeRequest(GET, s"/comments?author=$author")).get
      status(foundComment) must equalTo(OK)

      val cmt = contentAsJson(foundComment).as[CommentSeqBody]

      cmt.comments must have size 2
      cmt.comments.forall(_.author == author) must beTrue
    }

    "get comments by storeId" in new WithApplication(app.build) {
      Init[CI]

      val store = comment1.storeId.get
      val foundComment = route(FakeRequest(GET, s"/comments?store=$store")).get
      status(foundComment) must equalTo(OK)

      val cmt = contentAsJson(foundComment).as[CommentSeqBody]

      cmt.comments must have size 1
      cmt.comments.forall(_.storeId.contains(store)) must beTrue
    }

    "get comments by lang" in new WithApplication(app.build) {
      Init[CI]

      val lang = comment1.lang.get
      val foundComment = route(FakeRequest(GET, s"/comments?lang=${lang.code}")).get
      status(foundComment) must equalTo(OK)

      val cmt = contentAsJson(foundComment).as[CommentSeqBody]

      cmt.comments must have size 1
      cmt.comments.forall(_.lang.contains(lang)) must beTrue
    }


    "get comments by multiple conditions" in new WithApplication(app.build) {
      Init[CI]

      val appId = App.app1.id.get
      val author = User.user1.id
      val foundComment = route(FakeRequest(GET, s"/comments?appId=$appId&author=$author&top=1")).get
      status(foundComment) must equalTo(OK)

      val cmt = contentAsJson(foundComment).as[CommentSeqBody]
      cmt.comments must have size 1
      cmt.count must beEqualTo(2)
    }

    "get a comment by an nonexistent id" in new WithApplication(app.build) {
      Init[CI]

      val foundComment = route(FakeRequest(GET, "/comments/123123123")).get
      status(foundComment) must equalTo(NOT_FOUND)
    }

    "get a comment log" in new WithApplication(app.build) {
      Init[CI]

      var allComments = route(FakeRequest(GET, "/comments")).get

      val json = contentAsJson(allComments)
      val id = json.as[CommentSeqBody].comments.filter(_.lang == comment1.lang).head.id

      val logs = route(FakeRequest(GET, s"/comments/$id/actionLog")).get
      status(logs) mustEqual OK
      contentAsJson(logs).as[Seq[ActionLogBody]] must have size 1

    }

    "post a comment log" in new WithApplication(app.build) {
      Init[CI]

      var allComments = route(FakeRequest(GET, "/comments")).get

      val json = contentAsJson(allComments)
      val id = json.as[CommentSeqBody].comments.head.id
      var log = route(FakeRequest(POST, s"/comments/$id/actionLog").withJsonBody(Json.toJson(ActionLogs.log1))).get
      status(log) mustEqual OK

      log = route(FakeRequest(POST, s"/comments/348934897/actionLog").withJsonBody(Json.toJson(ActionLogs.log1))).get
      status(log) mustEqual NOT_FOUND

    }

    "delete a comment" in new WithApplication(app.build) {
      Init[CI]
      val id = contentAsJson(route(FakeRequest(GET, "/comments")).get).as[CommentSeqBody].comments.head.id
      val delResult = route(FakeRequest(DELETE, s"/comments/$id")).get
      status(delResult) must equalTo(OK)

      val foundComment = route(FakeRequest(GET, s"/comments/$id").withHeaders(("Cache-Control", "no-cache"))).get
      status(foundComment) must equalTo(NOT_FOUND)
    }

    "delete a comment and all the comments which reply to it" in new WithApplication(app.build) {
      Init[CI]

      val id = contentAsJson(route(FakeRequest(GET, "/comments")).get).as[CommentSeqBody].comments.filter(_.replyTo.isEmpty).head.id

      val delResult = route(FakeRequest(DELETE, s"/comments/$id")).get
      status(delResult) must equalTo(OK)

      val foundComment = route(FakeRequest(GET, "/comments").withHeaders(("Cache-Control", "no-cache"))).get
      val cmt = contentAsJson(foundComment).as[CommentSeqBody]
      cmt.comments must have size 0
      cmt.count must beEqualTo(0)

    }

    "delete a nonexistent comment" in new WithApplication(app.build) {
      Init[CI]

      val delResult = route(FakeRequest(DELETE, "/comments/5134135134")).get
      status(delResult) must equalTo(NOT_FOUND)
    }

    "insert a comment, then find it" in new WithApplication(app.build) {
      Init[CI]

      val insertResult = route(FakeRequest(POST, "/comments").withJsonBody(Json.toJson(Comments.comment1))).get
      status(insertResult) must equalTo(OK)
      val insertId = contentAsJson(insertResult).as[CommentRow].id
      insertId must greaterThan(0L)

      val foundComment = route(FakeRequest(GET, s"/comments/$insertId")).get
      status(foundComment) must equalTo(OK)
      contentAsJson(foundComment).as[CommentRow].id must beEqualTo(insertId)
    }

    "insert a comment with only content" in new WithApplication(app.build) {
      Init[CI]

      val body = CommentBody(author = User.user1.id, text = Some("中文"), appId = App.app1.id.get)

      val insertResult = route(FakeRequest(POST, "/comments").withJsonBody(Json.toJson(body))).get
      status(insertResult) must equalTo(OK)

      val insertId = contentAsJson(insertResult).as[CommentRow].id

      // Find the inserted record
      val foundComment = route(FakeRequest(GET, s"/comments/$insertId")).get
      status(foundComment) must equalTo(OK)
      val cmt = contentAsJson(foundComment).as[CommentRow]
      cmt.text.get must equalTo("中文")
    }


    "insert a comment with only rating" in new WithApplication(app.build) {
      Init[CI]

      val body = CommentBody(author = User.user1.id, rating = Some(4), appId = App.app1.id.get)

      val insertResult = route(FakeRequest(POST, "/comments").withJsonBody(Json.toJson(body))).get
      status(insertResult) must equalTo(OK)

      val insertId = contentAsJson(insertResult).as[CommentRow].id

      // Find the inserted record
      val foundComment = route(FakeRequest(GET, s"/comments/$insertId")).get
      status(foundComment) must equalTo(OK)
      val cmt = contentAsJson(foundComment).as[CommentRow]
      cmt.rating.get must equalTo(4)

    }

    "insert a comment with invalid rating" in new WithApplication(app.build) {
      Init[CI]

      val body = CommentBody(author = User.user1.id, rating = Some(9), appId = App.app1.id.get)

      var insertResult = route(FakeRequest(POST, "/comments").withJsonBody(Json.toJson(body))).get
      status(insertResult) must equalTo(BAD_REQUEST)

      val body2 = CommentBody(author = User.user1.id, rating = Some(-1), appId = App.app1.id.get)

      insertResult = route(FakeRequest(POST, "/comments").withJsonBody(Json.toJson(body2))).get
      status(insertResult) must equalTo(BAD_REQUEST)

    }


    "update a comment with rating and content" in new WithApplication(app.build) {

      Init[CI]

      val body = CommentBody(author = User.user1.id, title = "title", text = "text", rating = Some(4), appId = App.app1.id.get)

      var insertResult = route(FakeRequest(POST, "/comments").withJsonBody(Json.toJson(body))).get
      status(insertResult) must equalTo(OK)

    }

    "update a comment status" in new WithApplication(app.build) {
      Init[CI]
      val id = contentAsJson(route(FakeRequest(GET, "/comments")).get).as[CommentSeqBody].comments.filter(_.status == Preview).head.id

      val body = CommentBody(author = User.user1.id, status = Some(Live), appId = App.app1.id.get)

      val res = route(FakeRequest(PUT, s"/comments/$id").withJsonBody(Json.toJson(body))).get
      status(res) must equalTo(OK)

      val foundComment = route(FakeRequest(GET, s"/comments/$id")).get
      val cmt = contentAsJson(foundComment).as[CommentRow]
      cmt.status must beEqualTo(Live)

    }

    "update a comment status with rating change: Live => Live" in new WithApplication(app.build) {
      Init[CI]
      val comment = contentAsJson(route(FakeRequest(GET, "/comments")).get).as[CommentSeqBody].comments.filter(_.status == Live).head
      val id = comment.id
      val newRating = comment.rating match {
        case i if i == 5 || i == 1 => 3
        case i => i - 1
      }
      val body = CommentBody(author = User.user1.id, status = Some(Live), appId = App.app1.id.get, rating = newRating)

      val res = route(FakeRequest(PUT, s"/comments/$id").withJsonBody(Json.toJson(body))).get
      status(res) must equalTo(OK)

      val foundComment = route(FakeRequest(GET, s"/comments/$id")).get
      val cmt = contentAsJson(foundComment).as[CommentRow]
      cmt.status must beEqualTo(Live)
      cmt.rating must beEqualTo(newRating)

    }
    "update a comment status with rating change: Live => Preview" in new WithApplication(app.build) {
      Init[CI]
      val comment = contentAsJson(route(FakeRequest(GET, "/comments")).get).as[CommentSeqBody].comments.filter(_.status == Live).head
      val id = comment.id
      val newRating = comment.rating match {
        case i if i == 5 || i == 1 => 3
        case i => i - 1
      }
      val body = CommentBody(author = User.user1.id, status = Some(Preview), appId = App.app1.id.get, rating = newRating)

      val res = route(FakeRequest(PUT, s"/comments/$id").withJsonBody(Json.toJson(body))).get
      status(res) must equalTo(OK)

      val foundComment = route(FakeRequest(GET, s"/comments/$id")).get
      val cmt = contentAsJson(foundComment).as[CommentRow]
      cmt.status must beEqualTo(Preview)
      cmt.rating must beEqualTo(newRating)

    }


    "update a comment status with invalid value" in new WithApplication(app.build) {
      Init[CI]
      val id = contentAsJson(route(FakeRequest(GET, "/comments")).get).as[CommentSeqBody].comments.head.id


      val body = Json.toJson(CommentBody(author = User.user1.id, status = Some(Live), appId = App.app1.id.get)).toString.replace("live", "invalid")

      val updateResult = route(FakeRequest(PUT, s"/comments/$id").withJsonBody(Json.parse(body))).get
      status(updateResult) must equalTo(BAD_REQUEST)
    }
  }
}
