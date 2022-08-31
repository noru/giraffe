package com.acrd.giraffe.test.init

import com.acrd.giraffe.common.implicits
import com.acrd.giraffe.dao.CommentDAO._
import com.acrd.giraffe.models._
import com.acrd.giraffe.models.gen.Models._
import AppStoreTables.Platform._
import AppStoreTables.PayloadStatus._
import play.api.i18n.Lang
import com.acrd.giraffe.services.TextService._
import com.acrd.giraffe.common.utils.IdGenerator.getUID
import com.acrd.giraffe.common.Consts._

object TestingData {

  import implicits.Options._
  import implicits.Timestamps._
  import com.github.nscala_time.time.Imports._

  object Id {
    val mockId1 = 123L
    val mockId2 = 1234L
    val mockId3 = 12345L
    val mockId4 = 123456L
  }
  object User{
    val user1 = UserRow("author", "nooooru@gmail.com", "Jon", "Doe", "GFUYT#$!" )
    val user2 = UserRow("xiuzhu", "nooooru@gmail.com", "Xiu", "Zhu", "123123123", 123L )
  }

  object Facet {
    val category = FacetBody("category", Seq("mechanical", "architecture"))
    val productLine = FacetBody("productLine", Seq("ACD", "RVT", "MAYA"))
    val version = FacetBody("version", Seq("2011", "2012", "2013", "2014", "2015", "2016"))
    val bodySeq = Seq(category, productLine, version)
    val rowSeq = FacetBody.toFacetRow(bodySeq)(DummyId)

  }

  object Media {

    val mediaSetId = getUID
    val icon = AttachmentRow(id = 0, uri = "www.autodesk.com/logo.jpg", size = 123, mime = "image/jpg", `type` = "icon", value = None, shortDescription = "it is an icon", description = "oh", setId = 0L)
    val screenshot1 = AttachmentRow(id = 0, uri = "apps.autodesk.com/logo.jpg", size =3453, mime = "image/jpg", `type` = "screenshot", value = None, shortDescription = "screenshot1", description = "", setId = mediaSetId)
    val screenshot2 = AttachmentRow(id = 0, uri = "apps.autodesk.com/logo.png", size =3453, mime = "image/png", `type` = "screenshot", value = None, shortDescription = "screenshot2", description = "", setId = mediaSetId)
    val command = AttachmentRow(id = 0, uri = "command.icon.png", `type` = "command", value = None, shortDescription = "command", description = "run this command in console", setId = mediaSetId)

    val attachmentSeq = Seq(screenshot1, screenshot2, command)

    val attachmentOptionSeq = Some(attachmentSeq)

  }

  object Payload {

    import Id._
    import Media._

    val payload1 = PayloadRow(id = mockId1,
        lang = Lang("en"), os = Mac, createAt = now, updateAt = now,
        upgradeCode = "LKJHL!",
        productCode = "OJHLI",
        reviewStatus = Some(Draft),
        url = "http://google.com/image.png", size = 123, mime = "image/png",
        attachmentSetId = 123L,
        title = "App1 Title",
        shortDescription = "Short Description",
        description = "Looooooooooooonnnnnnnnnnnggggggggggg description",
        instruction = "instruction",
        installation = "installation guide",
        additionalInfo = "blabla",
        knownIssue = "it has a lot bugs",
        supportInfo = "Don't call me, I'll call you",
        version = "1.0.1",
        versionDescription = "desc 1.0.1"
    )

    val payloadBody1 = PayloadBody.fromPayloadRowAndMediaRow(payload1, attachmentSeq)

    val payload2 = PayloadRow(id = mockId2,
      lang = Lang("en"), os = Mac, createAt = now, updateAt = now,
      upgradeCode = "LKJHL!",
      productCode = "OJHLI",
      reviewStatus = Some(Draft),
      url = "http://google.com/image.png", size = 123, mime = "image/png",
      attachmentSetId = 123L,
      title = "App1 Title",
      shortDescription = "Short Description",
      description = "Looooooooooooonnnnnnnnnnnggggggggggg description",
      instruction = "instruction",
      installation = "installation guide",
      additionalInfo = "blabla",
      knownIssue = "it has a lot bugs",
      supportInfo = "Don't call me, I'll call you" ,
      version = "1.0.1",
      versionDescription = "desc 1.0.1")

    val payloadBody2 = PayloadBody.fromPayloadRowAndMediaRow(payload2, attachmentSeq)

    val payload3 = PayloadRow(id = mockId3,
      lang = Lang("en"), os = Mac, createAt = now, updateAt = now,
      upgradeCode = "LKJHL!",
      productCode = "OJHLI",
      reviewStatus = Some(Draft),
      url = "http://google.com/image.png", size = 123, mime = "image/png",
      attachmentSetId = 123L,
      title = "App1 Title",
      shortDescription = "Short Description",
      description = "Looooooooooooonnnnnnnnnnnggggggggggg description",
      instruction = "instruction",
      installation = "installation guide",
      additionalInfo = "blabla",
      knownIssue = "it has a lot bugs",
      supportInfo = "Don't call me, I'll call you",
      version = "1.0.1",
      versionDescription = "desc 1.0.1")
    val payloadBody3 = PayloadBody.fromPayloadRowAndMediaRow(payload3, attachmentSeq)

    val payload4 = PayloadRow(id = mockId4,
      lang = Lang("en"), os = Mac, createAt = now, updateAt = now,
      upgradeCode = "LKJHL!",
      productCode = "OJHLI",
      reviewStatus = Some(Draft),
      url = "http://google.com/image.png", size = 123, mime = "image/png",
      attachmentSetId = 123L,
      title = "App1 Title",
      shortDescription = "Short Description",
      description = "Looooooooooooonnnnnnnnnnnggggggggggg description",
      instruction = "instruction",
      installation = "installation guide",
      additionalInfo = "blabla",
      knownIssue = "it has a lot bugs",
      supportInfo = "Don't call me, I'll call you",
      version = "1.0.1",
      versionDescription = "desc 1.0.1")
    val payloadBody4 = PayloadBody.fromPayloadRowAndMediaRow(payload4, attachmentSeq)
  }

  object App {

    import Id._
    import Payload._
    import Media._

    val price1 = PricePlanRow(mockId1, now, mockId1)

    val app1 = AppBody.fromTuple(
      (
        AppRow(mockId1, now, now, "app1", User.user1.id, "icon.jpg", "", ""),
        Some(getNewRatingRow(mockId1)),
        Facet.rowSeq,
        Seq(price1),
        Seq(
          (payload1, attachmentOptionSeq)
        )
      )
    )
    val changedApp1 = app1.copy(payloads = app1.payloads.map(_.map(_.copy(url = "changed url"))))

    val price2 = PricePlanRow(mockId2, now, mockId2)

    val app2 = AppBody.fromTuple((
      AppRow(mockId2, now, now, "app2", User.user2.id, "icon.jpg", "", ""),
      Some(getNewRatingRow(mockId2)),
      Facet.rowSeq,
      Seq(price2),
      Seq((payload2, attachmentOptionSeq))))


    val price3 = PricePlanRow(mockId3, now, mockId3)

    val app3 = AppBody.fromTuple((
      AppRow(mockId3, now, now, "app3", User.user2.id, "icon.jpg", "", ""),
      Some(getNewRatingRow(mockId3)),
      Facet.rowSeq,
      Seq(price3),
      Seq((payload3, attachmentOptionSeq))))

    val price4 = PricePlanRow(mockId4, now, mockId4)

    val app4 = AppBody.fromTuple((
      AppRow(mockId4, now, now, "app4", User.user1.id, "icon", "", ""),
      Some(getNewRatingRow(mockId4)),
      Facet.rowSeq,
      Seq(price4),
      Seq((payload4, attachmentOptionSeq))))
  }

  object Settings {

    val root = Metadata("root", None, "Root Node", "string", "I am root settings")
    val root2 = Metadata("root2", None, "Root Node 2", "string", "I am root settings")
    val leaf1 = Metadata("leaf1", "root", "Leaf Node 1", "int", "1")
    val leaf2 = Metadata("leaf2", "root", "Leaf Node 2", "json", "{foo:bar}")
    val illegalLeaf = Metadata("leafWithWrongParent", None, "Leaf Node 3", "json", "{foo:bar}")

  }

  object Comments {

    import User._, App._, AppStoreTables.CommentStatus._
    val comment1 = CommentBody(
      author = user1.id,
      createAt = now,
      updateAt = now,
      lang = Some(en),
      title = "title",
      text = "this is a comment",
      status = Some(Live),
      rating = 3,
      appId = app1.id.get,
      storeId = "ACD",
      isVerifiedDownload = true)
    val comment2 = CommentBody(
      author = user1.id,
      lang = Some(fr),
      title = "bon",
      text = "bonjour misure",
      status = Some(Preview),
      rating = 5,
      appId = app1.id.get)
    val comment3 = CommentBody(
      author = user1.id,
      lang = Some(zh_CN),
      title = "中文",
      text = "这是一个评论",
      status = Some(Preview),
      rating = 2,
      appId = app1.id.get)
    val comment4 = CommentBody(
      author = user1.id,
      lang = Some(ja),
      title = "哦哈哟",
      text = "ハイドアンド・シーク",
      status = Some(Preview),
      rating = 4,
      appId = app2.id.get)


  }

  object ActionLogs {
    import App._
    val log1 = ActionLogBody(id = getUID, timestamp = now, msg1 = "action log1", parentId = app1.id.get )
    val log2 = ActionLogBody(id = getUID, timestamp = now, msg1 = "action log2", parentId = app2.id.get )


  }



}