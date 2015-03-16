package com.mediacrossing.campaigncleanup

import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.properties.ConfigurationProperties
import scala.collection.JavaConversions._

object RunCampaignCleanup extends App {
  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val givenLineId = List(
    "1823331",
    "1823328",
    "1823327"

  )
  val givenDmaName = "DL_NewYork4/12/15"
  val salesforceId = "006d000000Oldbv"
  // YYYY-MM-dd HH:mm:ss
  val startDate = "2015-03-16 00:00:00"
  val endDate = "2015-04-11 23:59:59"
  val creatives = List(
  )
    .map(s => s"""{"id":"$s"}""").mkString(",")

  def nameChange(name: String): String = {
    val splitWithoutCopy = name.replace("Copy ", "").split("]_")
    splitWithoutCopy.update(1, s"[G:$givenDmaName")
    splitWithoutCopy.mkString("]_")
  }

  val allCamps = mxConn.requestAllCampaigns().toList

  givenLineId.foreach(l => {
    val camps = allCamps
      .filter(c => c.getLineItemID.equals(l))
      .map(c => (nameChange(c.getName), c.getId, c.getProfileID))

    // Took out creative allocation , "creatives":[$creatives]
    camps.foreach(c => {
      val campPutString = s"""{"campaign":{"name": "${c._1}", "code": "$salesforceId ${c._2}", "start_date": "$startDate","end_date": "$endDate"}}"""
      val proPutString = "{\"profile\":{\"dma_action\":\"exclude\", \"country_action\":\"exclude\", \"region_action\":\"exclude\", \"city_action\":\"exclude\", \"country_targets\":[],\"city_targets\":[],\"dma_targets\":[],\"region_targets\":[],\"zip_targets\":[]}}"
      anConn.requests.putRequest(properties.getPutneyUrl + s"/campaign?id=${c._2}", campPutString)
      //anConn.requests.putRequest(properties.getPutneyUrl + s"/profile?id=${c._3}", proPutString)
    })
  })
}
