package com.mediacrossing.apichanges

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import au.com.bytecode.opencsv.CSVReader
import java.io.FileReader
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._


object RunSellerWhitelist extends App {

  val LOG = LoggerFactory.getLogger(RunSellerWhitelist.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  //Get all campaign ids
  //For each campaign ID, upload pacing
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  //val campProMap = mxConn.requestAllCampaigns().toList.map(c => c.getId -> c.getProfileID).toMap

  // Read In Csv
  //  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Downloads/SitePlacement-Blocks-04.14.2015.csv"))
  //  val proR = (
  //    (__ \ "id").read[Int] ~
  //      (__ \ "action").read[String]
  //    ).tupled
  /*
    val proToPlacementMap = reader
      .readAll
      .toList
      .tail
      .filterNot(l => l(0).equals(""))
      .map(a => (campProMap.get(a(2)).get, a(0)))
      .groupBy(_._1)
      .mapValues(v => v.map(l => l._2))
      .foreach(p => {
      // Request Profile

        val json = anConn.requests.getRequest(properties.getPutneyUrl + s"/profile?id=${p._1}")
        val existingPlacements = Json.parse(json).\("response").\("profile").\("platform_placement_targets")
          .validate(list(proR)) match {
            case e@JsError(_) => {
              if(JsError.toFlatJson(e).toString().equals("""{"obj":[{"msg":"validate.error.expected.jsarray","args":[]}]}"""))
                List()
              else sys.error(JsError.toFlatJson(e).toString())
            }
            case JsSuccess(v, _) => v
        }
        val tups = p._2.map(s => (s.toInt, "exclude"))
        val allTups = tups ++ existingPlacements
        val putString = allTups.map(t => s"""{"id":${t._1},"action":"${t._2}"}""").mkString(",")
        val finalString = s"""{"profile":{ "platform_placement_targets":[$putString]}} """
        println(finalString)
        anConn.requests.putRequest(properties.getPutneyUrl + s"/profile?id=${p._1}", finalString)

    })*/
  //  val placementList = reader
  //    .readAll
  //    .toList
  //    .map(_(0))

  val templateLines = List(
    "1918102",
    "1985757",
    "2010992",
    "2011370",
    "1924071",
    "1924550",
    "1924953",
    "1924968",
    "1925034",
    "1962797",
    "1962878",
    "1962893",
    "1962904",
    "1962924"

  )

  val activeLines = mxConn
    .requestAllLineItems
    .toList
    .filter(l => l.getEndDate.isAfter(new DateTime()) /*&& l.getStartDate.isAfter(new DateTime().minusDays(7))*/ && !l.getName.toLowerCase.contains("retargeting"))
    .map(l => l.getId)

  val pros = mxConn.requestAllCampaigns
    .toList
    //.filter(c => activeLines.contains(c.getLineItemID))
    .filter(c => templateLines.contains(c.getLineItemID))
    .map(c => c.getProfileID)

  println(pros.size)
  val members = List(357,
    280,
    459,
    181,
    1263,
    1356,
    926,
    1184,
    1908,
    1613,
    541,
    2553,
    310,
    311,
    1944,
    1752,
    1610,
    2745,
    2714)

  val memberList = members.map(m => "{\"id\":" + m + ", \"action\":\"include\"}").mkString(",")

  pros.foreach(p => {

    //      val json = anConn.requests.getRequest(properties.getPutneyUrl + s"/profile?id=${p}")
    //      val existingPlacements: List[(Int, String)] = Json.parse(json).\("response").\("profile").\("platform_placement_targets")
    //        .validate(list(proR)) match {
    //        case e@JsError(_) => {
    //          if(JsError.toFlatJson(e).toString().equals("""{"obj":[{"msg":"validate.error.expected.jsarray","args":[]}]}"""))
    //            List()
    //          else sys.error(JsError.toFlatJson(e).toString())
    //        }
    //        case JsSuccess(v, _) => v
    //      }
    //
    //      val tups: List[(Int, String)] = placementList.map(p => (p.toInt, "exclude"))
    //      val allTups = tups ++ existingPlacements
    //      val putString = allTups.map(t => s"""{"id":${t._1},"action":"${t._2}"}""").mkString(",")
    val finalString = s"""{"profile":{"member_default_action": "include", "member_targets": [$memberList] }} """
    println(finalString)
    anConn.requests.putRequest(properties.getPutneyUrl + s"/profile?id=${p}", finalString)
  })
}
