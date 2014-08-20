package com.mediacrossing.apichanges

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._

object RunApiChanges extends App {
  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  //Get all campaign ids
  //For each campaign ID, upload pacing
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

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
}
