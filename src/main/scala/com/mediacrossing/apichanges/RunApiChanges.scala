package com.mediacrossing.apichanges

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{HTTPRequest, MxService, AppNexusService}
import scala.collection.JavaConversions._
import com.mediacrossing.dailycheckupsreport.{SegmentGroupTarget, JSONParse}
import au.com.bytecode.opencsv.CSVReader
import java.io.FileReader
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository
import com.mediacrossing.segmenttargeting.profiles.PutneyProfileRepository
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.DateTime
import java.util

object RunApiChanges extends App {
  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  //Get all campaign ids
  //For each campaign ID, upload pacing
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  //val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/Motosports_Conv_Report.csv"))

  val home = System.getProperty( "user.home" )
  val path = (new java.io.File(home, "/Desktop/MJFF_StartEnds.csv")).getAbsolutePath()
  val pw = new java.io.PrintWriter(path)
  pw.println("Name, Status, Start, End")

  mxConn.requestAllCampaigns().toList
    .filter(c => c.getAdvertiserID.equals("435502"))
    .map(c => List(c.getName, c.getStatus, c.getStartDate, c.getEndDate).mkString(","))
    .foreach(pw.println(_))


  pw.close()


  /* FIXES NAMING CONVENTION ISSUES FOR REPORTING MOBILE AND CONTEXTUAL CAMPAIGNS
      val camps = mxConn.requestAllCampaigns()
         .toList
         .filter(c => c.getStatus.equals("active"))
         .foreach(c => {
            if (c.getName.toLowerCase.contains("grapeshot") || c.getName.toLowerCase.contains("peer39")) {
              val name = {c.getName :: "_Contextual" :: Nil}.mkString
              println("Changing " + c.getId + " to " + name)
              val json = "{" + "\"campaign\":" + "{" + "\"name\":" + "\"" + name + "\"" +"}}"
              anConn.putRequest("/campaign?id=" + c.getId + "&advertiser_id=" + c.getAdvertiserID, json)
            }
            if (c.getName.toLowerCase.contains("tablet") || c.getName.toLowerCase.contains("phone")) {
              val name = {c.getName :: "_Mobile" :: Nil}.mkString
              println("Changing " + c.getId + " to " + name)
              val json = "{" + "\"campaign\":" + "{" + "\"name\":" + "\"" + name + "\"" +"}}"
              anConn.putRequest("/campaign?id=" + c.getId + "&advertiser_id=" + c.getAdvertiserID, json)
            }
          })
  */

  /*
    val adId = "214113"
    val pros = {
      val json = anConn.requests.getRequest(properties.getPutneyUrl + s"/profile?advertiser_id=$adId")
      JSONParse.parseProfiles(json).toList
    }
    val campProMap: Map[String, String] = mxConn.requestAllCampaigns().toList
      .filter(c => c.getAdvertiserID.equals(adId))
      .map(c => c.getProfileID -> c)
      .toMap
      .mapValues(c => c.getName + " (" + c.getId + ")")

    val neededInfo = pros.map(p => (campProMap.get(p.getId).getOrElse("No Camp"), p.getDomainListAction, p.getDomainListTargetList))
      .filterNot(i => i._3.contains("247440"))

    neededInfo.foreach(x => (x._3.foreach(id => println(x._1 + ", " + x._2 + ", " + id))))
  */

//  // Convert to map
//  // Advertiser ID to Domain List IDs
//  val idMap = result.map(r => r(0) -> r(1).split(" ")).toMap
//
//  def inThere(adId: String, targetList: List[String]) : Boolean = {
//    var exists = false
//    targetList.foreach(t => {
//      if (idMap.get(adId).getOrElse(Array()).contains(t) || t.equals("277861")) exists = true
//    })
//    exists
//  }
  // Get all Campaigns


  /*
    val adverts = mxConn.requestAllAdvertisers().toList
      .filter(a => a.isLive)
      .map(_.getAdvertiserID)

    val camps = mxConn.requestAllCampaigns().toList
      .filter(c => c.getStatus.equals("active") && adverts.contains(c.getAdvertiserID))

    val lines = mxConn.requestAllLineItems().toList
      .filter(_.getStatus.equals("active"))
      .map(_.getId)

    val allProfiles = adverts
      .map(adId => {
        val json = anConn.requests.getRequest(properties.getPutneyUrl + s"/profile?advertiser_id=$adId")
        JSONParse.parseProfiles(json).toList
      })
      .flatten
      .map(p => p.getId -> p)
      .toMap

    val badCamps = camps.filterNot(c => (allProfiles.get(c.getProfileID).get.getDomainListAction.contains("exclude")
      && inThere(c.getAdvertiserID, allProfiles.get(c.getProfileID).get.getDomainListTargetList.toList)))
      .filter(c => allProfiles.get(c.getProfileID).get.getDomainListAction.contains("exclude"))
      .filter(c => lines.contains(c.getLineItemID))

    // pw results to desktop
    val home = System.getProperty( "user.home" )
    val path = (new java.io.File(home, "/Desktop/BadCamps.csv")).getAbsolutePath()
    val pw = new java.io.PrintWriter(path)

    badCamps.foreach(c => println(c.getStatus + " (" + c.getId + ") " + c.getName))

    pw.close()
  */

  /*val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Downloads/Blacklist_Campaigns.csv"))
  val blacklistCampIds = reader
    .readAll
    .toList
    .tail
    .map(_(0).substring(2, 9))


  val allCampToProIdMap = mxConn.requestAllCampaigns().toList
    .map(c => c.getId -> c.getProfileID)
    .toMap

  val blacklistProIds = blacklistCampIds.map(id => allCampToProIdMap.get(id).get)

  blacklistProIds.foreach(id => {
    val json = "{\n  \"profile\": {\n    \"domain_list_action\": \"exclude\",\n    \"domain_list_targets\":[\n      " +
      "{\n        \"id\": \"277861\"\n      }\n    ]\n  }\n}"
    anConn.putRequest(s"/profile?id=$id", json)
  })*/


  /*val adIds = {"206084" :: Nil}
  val blackListProIds = mxConn.requestAllCampaigns().toList
    .filter(c => adIds.contains(c.getAdvertiserID))
    .map(c => (c.getAdvertiserID, c.getProfileID))

  val proIds = blackListProIds.map(p => p._2)

  private def production(r: HTTPRequest): ProfileRepository = {
    return new PutneyProfileRepository(r)
  }
  val profileRepository: ProfileRepository = production(anConn.requests)

  val profiles = profileRepository.findBy(blackListProIds, properties.getPutneyUrl)
    .filter(p => proIds.contains(p.getId))
    .filterNot(p => p.getDomainListAction.equals("include"))
    .map(p => (p.getId, p.getDomainListTargetList))


  profiles.foreach(p => {
    val targetString = (p._2.map(s => "{\"id\":\"" + s + "\"}") ++ List("{\"id\": \"298628\"}")).mkString(",")
    val json = "{\n  \"profile\": {\n    \"domain_list_action\": \"exclude\",\n    \"domain_list_targets\":[" + targetString + "]\n  }\n}"
    anConn.putRequest(s"/profile?id=${p._1}", json)
  })*/


  // Get all creative for Regis 324264
 /* val creativeR = (
    (__ \ "id").read[Int] ~
      (__ \ "name").read[String]
    ).tupled

  val creativeUrl = s"/creative?advertiser_id=324264"
  val fullUrl = properties.getPutneyUrl + creativeUrl
  val creatives: List[(Int, String)] = Json.parse(anConn.requests.getRequest(fullUrl)).\("response").\("creatives")
    .validate(list(creativeR)) match {
    case e@JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v
  }
  val mapped: Map[Int, String] = creatives.toMap

  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/Regis_Numbers.csv"))
  val csvData = reader
    .readAll
    .toList
    .tail
    .map(l => (l(0), mapped.get(l(1).substring(1).toInt).get, l(2)))

  val outputFilename = "/Users/charronkyle/Desktop/Regis_Numbers_2.csv"

  val pw = new java.io.PrintWriter(outputFilename)
  val header = {
    ("Date" :: "Creative" :: "Imps" :: Nil).
      mkString(",")
  }
  pw.println(header)

  csvData.foreach(t => {
    val line = (t._1 :: t._2 :: t._3 :: Nil).mkString(",")
    pw.println(line)
  })

  pw.close()*/

 /*val lines = mxConn.requestAllLineItems()
    .toList
    .map(l => l.getId -> l.getName)
    .toMap

  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/FELD_QUERY_1.csv"))
  val csvData = reader
    .readAll
    .toList
    .tail

  val outputFilename = "/Users/charronkyle/Desktop/Final_FELD.csv"

  val pw = new java.io.PrintWriter(outputFilename)
  val header = {
    ("LineItem" :: "DMA" :: "Imps" :: Nil).
      mkString(",")
  }
  pw.println(header)

  csvData.foreach(l => {
    val line = (s"${lines.get(l(0)).getOrElse("None")} (${l(0)})" :: l(1) :: l(2) :: Nil).mkString(",")
    pw.println(line)
  })

  pw.close()*/


  /*// Get all camps
  // filter to regis w/ start date after sept 29
  // map to proId
  val dtf = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss'Z'")
  val proIds: List[String] = mxConn.requestAllCampaigns().toList
    .filter(c => c.getAdvertiserID.equals("324264") && dtf.parseDateTime(c.getStartDate).isAfter(new DateTime().minusDays(2)))
    .map(c => c.getProfileID)


  // get all regis pros
  val regisPros = anConn.requestAllProfileSegments()
    .toMap
    .filter(m => proIds.contains(m._1))
    .filter(m => m._2.size() == 0)
    .foreach(pro => {


      val id = pro._1

      val existingSegmentObjects = pro._2.map(sgt => {

        val segments = sgt.getSegmentArrayList.map(s => s"""{"id":${s.getId}, "action":"${s.getAction}"}""").mkString(",")

        s"""{"boolean_operator": "${sgt.getBoolOp}",""" +
            s""" "segments": [""" +
            s"$segments  " +
            s"]}"
      }).mkString(",")


      val query = s"""{"profile": {  "segment_group_targets" : [    {      "boolean_operator":"and",      "segments":[        {          "id": 1739913,          "action": "exclude"        }      ]    }  ]}}"""
      println(query)
      anConn.requests.putRequest(s"http://localhost:8888/an/profile?id=$id", query)
  })*/
  //val lines = mxConn.requestAllLineItems().filter(l => l.getAdvertiserId.equals("186356") && l.getName.toLowerCase.contains("retargeting")).map(l => s""""${l.getId}",""").foreach(println)



  // map proIds to pros

  // add exclude pixel to current pro

}
