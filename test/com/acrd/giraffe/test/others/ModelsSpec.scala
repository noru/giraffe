package com.acrd.giraffe.test.others

import com.acrd.giraffe.models.{FacetBody, PayloadBody}
import com.acrd.giraffe.models.gen.Models
import Models.FacetRow
import models._
import org.junit.runner._
import org.specs2.runner._
import play.api.test.PlaySpecification
import com.acrd.giraffe.test.init.TestingData.Payload.payload1
import com.acrd.giraffe.test.init.TestingData.Media._
import scala.util.Try

@RunWith(classOf[JUnitRunner])
class ModelsSpec extends PlaySpecification {

  "Models" should {

    "FacetBody: from FacetRow" in {

      val f = FacetBody("test", Seq("a", "b", "c") )
      val r = f.toFacetRow
      r must have size 3
      r.head.id mustEqual 0
      r.head.facet must startingWith("test")
      r.map(_.facet.split("::").head).toSet must have size 1

    }

    "FacetBody: to FacetRow" in {

      val seq = Seq(FacetRow(1L, "test::a"),
        FacetRow(1L, "test::b"),
        FacetRow(1L, "test::c"))

      var f = FacetBody.fromFacetRows(seq)

      f must have size 1
      f.head.name mustEqual "test"
      f.head.values must have size 3

      val err = seq :+ FacetRow(2L, "test::shouldNotSupportMultipleId")

      Try {
        f = FacetBody.fromFacetRows(err)
      }.isFailure must beTrue

      val seq2 = Seq(FacetRow(1L, "other::a"),
        FacetRow(1L, "other::b"),
        FacetRow(1L, "other::c"))

      f = FacetBody.fromFacetRows(seq ++ seq2)

      f must have size 2
      f.head.values must have size 3
      f.last.values must have size 3

    }

    "PayloadBody: to payload and media row" in {

      var result = PayloadBody.fromPayloadRowAndMediaRow(payload1, null)
      result.id.get mustEqual payload1.id
      result.attachments.isEmpty must beTrue

      result = PayloadBody.fromPayloadRowAndMediaRow(payload1, attachmentSeq)
      result.attachments.get must have size 3

    }

    "PayloadBody: from payload and media row" in {
      val body = PayloadBody.fromPayloadRowAndMediaRow(payload1, Seq(screenshot1, screenshot2, command))
      val tuple = body.toPayloadRowAndAttachmentRow
      tuple._1 mustEqual payload1

      tuple._2.isDefined must beTrue
      tuple._2.get must have size 3
    }

  }
}
