package com.mediacrossing.catalog.infrastructure

import org.specs2.mutable.Specification
import com.mediacrossing.util.Resources
import play.api.libs.json.{Reads, JsSuccess, Json}
import Reads._
import CatalogReads.advertiserR
import com.mediacrossing.catalog.domain.Catalog.Advertiser

class AdvertiserReadsSpec extends Specification with Resources {

  "Advertiser reads" should {

    "deserialize single valid advertiser" >> {
      usingResource("catalog/advertisers/single_valid.json") {
        source =>
          Json.parse(
            source.getLines()
              .mkString)
            .validate(advertiserR) must beEqualTo {
            JsSuccess(
              Advertiser(
                id = "149641",
                name = "Freelancers Union",
                status = "active",
                lineItemIds = "482958" :: Nil,
                campaignIds = "1539581" :: Nil))
          }
      }
    }

    "deserialize list of valid advertisers" >> {
      usingResource("catalog/advertisers/multiple_valid.json") {
        source =>
          Json.parse(
            source.getLines()
              .mkString)
            .validate(list(advertiserR)) must beLike {
            case JsSuccess(advertisers, _) =>
              advertisers.size must beEqualTo(18)
          }
      }
    }
  }
}
