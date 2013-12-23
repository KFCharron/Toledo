package com.mediacrossing.catalog.infrastructure

import org.specs2.mutable.Specification
import play.api.libs.json.{JsResult, Reads, JsSuccess, Json}
import Reads._
import com.mediacrossing.buyerbranddatareport.{AppNexusReads, PubJson, RunBuyerBrandReport}

class BrandReportSpec extends Specification{

  val rawJson = """[{"id":"176863","name":"Default Real Time","status":"inactive","siteIds":["322088"],"placementIds":["1204679"]},{"id":"181738","name":"DailyCamera.com","status":"inactive","siteIds":["330448"],"placementIds":["1237183"]},{"id":"183947","name":"MediaCrossing_testSite","status":"active","siteIds":["333665"],"placementIds":["1258459","1258465","1258595","1260329","1262207"]},{"id":"192890","name":"RetirementLiving","status":"active","siteIds":["348702"],"placementIds":["1333770","1333778"]},{"id":"242884","name":"Apartments.com","status":"active","siteIds":["459649"],"placementIds":["1668062","1691865","1691887","1691893","1691899","1691900","1691901"]},{"id":"263806","name":"Web.com","status":"active","siteIds":["503606"],"placementIds":["1837049","1837085","1853228"]},{"id":"266318","name":"Minnesota Star Tribune","status":"active","siteIds":["508952"],"placementIds":["1865317","1865951","1875250","1875252","1875259","1875260","1875261","1875262","1875265","1875266"]},{"id":"281099","name":"MediaCrossing Premium B2B - Pro IT Users","status":"active","siteIds":["545102"],"placementIds":["1956818","1956862","1956999","1957000"]},{"id":"281112","name":"MediaCrossing Premium B2B - Windows Pro","status":"active","siteIds":["545125"],"placementIds":["1956900","1956905","1957077","1957078"]},{"id":"281119","name":"MediaCrossing Premium B2B - Sharepoint Interest","status":"active","siteIds":["545132"],"placementIds":["1956956","1956975","1957053","1957054"]},{"id":"282391","name":"uVidi","status":"active","siteIds":["548305"],"placementIds":["1966497","1966515","1966518","1966519","1966526","1966527","1966528","1966529"]},{"id":"284710","name":"Bonnier OutDoorGroup","status":"active","siteIds":["552412"],"placementIds":["1997156","1997269","1997279","1997296","1997297","1997298"]}]"""
  val single = """{"id":"176863","name":"Default Real Time","status":"inactive","siteIds":["322088"],"placementIds":["1204679"]}"""

  "Publisher reads" should {
    "deserialize single valid publisher" >> {
      val pub: JsResult[PubJson] = Json.parse(single)
        .validate(AppNexusReads.pubR)
      pub must beEqualTo {
            JsSuccess(
              PubJson(
                id = "176863",
                name = "Default Real Time",
                status = "inactive",
                siteIds = "322088" :: Nil,
                placementIds = "1204679" :: Nil
                )
            )
          }
    }
  }

  "Deserialize list of valid publishers" >> {
    val pubs = Json.parse(rawJson).validate(list(AppNexusReads.pubR))

    pubs must beLike {
      case JsSuccess(publishers, _) =>
        publishers.size must beEqualTo(12)
    }
  }
}
