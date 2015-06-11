package com.mediacrossing.placementblacklist

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import au.com.bytecode.opencsv.CSVReader
import java.io.FileReader
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._


object RunPlacementBlacklist extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  //Get all campaign ids
  //For each campaign ID, upload pacing
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val campProMap = mxConn.requestAllCampaigns().toList.map(c => c.getId -> c.getProfileID).toMap

  // Read In Csv
  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Downloads/Placement_removal_report_last4Days_v2.csv"))
  val proR = (
    (__ \ "id").read[Int] ~
      (__ \ "action").read[String]
    ).tupled

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

  })
}
