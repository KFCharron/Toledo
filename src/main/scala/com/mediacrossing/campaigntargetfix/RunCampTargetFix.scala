package com.mediacrossing.campaigntargetfix

import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.dailycheckupsreport.{Profile, JSONParse}
import com.mediacrossing.properties.ConfigurationProperties
import scala.collection.JavaConversions._

object RunCampTargetFix extends App {
  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  // Request all camps, filter to feld advertiser, map to active, map to pro

  // request each pro, if action set to include and it's empty, filterNot
  val adList = List("214113", "186354", "186355", "186356")
  val camps = mxConn.requestAllCampaigns().toList.filter(c => adList.contains(c.getAdvertiserID))
  val pros: List[Profile] = {
    val json = anConn.requests.getRequest(properties.getPutneyUrl + "/profile?advertiser_id=" + adList.mkString(","))
    JSONParse.parseProfiles(json).toList
  }




}
