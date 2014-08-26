package com.mediacrossing.apichanges

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import com.mediacrossing.dailycheckupsreport.JSONParse
import au.com.bytecode.opencsv.CSVReader
import java.io.FileReader

object RunApiChanges extends App {
  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  //Get all campaign ids
  //For each campaign ID, upload pacing
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)


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

  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Downloads/Blacklist_Campaigns.csv"))
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
  })
}
