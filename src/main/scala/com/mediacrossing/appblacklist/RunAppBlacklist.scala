package com.mediacrossing.appblacklist

import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.properties.ConfigurationProperties
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import scala.collection.JavaConversions._


object RunAppBlacklist extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val today = new DateTime()
  // Get all campaigns w/ end dates after today
  val dtf: DateTimeFormatter = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss'Z'")
  val camps = mxConn.requestAllCampaigns().toList.filter(c => !c.getEndDate.equals("null")).filter(c => {
    println(c.getName)
    println(c.getEndDate)
    dtf.parseDateTime(c.getEndDate).isAfter(today)
  }).map(c => (c.getAdvertiserID, c.getProfileID))
  val json = "{\"profile\":{\"mobile_app_instance_list_action_include\":\"false\", \"mobile_app_instance_list_targets\":[{\"id\":1013}] }}"
  camps.foreach(c => {
    anConn.requests.putRequest(properties.getPutneyUrl + "/profile?id=" + c._2 + "&advertiser_id=" + c._1, json)
  })
}