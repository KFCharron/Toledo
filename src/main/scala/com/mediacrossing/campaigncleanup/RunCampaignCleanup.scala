package com.mediacrossing.campaigncleanup

import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.properties.ConfigurationProperties
import scala.collection.JavaConversions._

object RunCampaignCleanup extends App {
  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val givenLineId = List(
    "1724088",
    "1724089",
    "1724090"
  )
  val givenDmaName = "Uniondale3/8/15"
  // YYYY-MM-dd HH:mm:ss
  val startDate = "2015-02-04 00:00:00"
  val endDate = "2015-03-07 23:59:59"
  val creatives = List(
    "25041646",
  "25041644",
  "25041641",
  "25041637",
  "24991899",
  "24991886",
  "24991894",
  "24991876",
  "24991850",
  "24991861",
  "24991840"
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
      .filter(c => c.getLineItemID.equals(l) /*&& c.getName.contains("Copy")*/)
      .map(c => (nameChange(c.getName), c.getId, c.getProfileID))

    camps.foreach(c => {
      val campPutString = s"""{"campaign":{"name": "${c._1}","start_date": "$startDate","end_date": "$endDate", "creatives":[$creatives]}}"""
      val proPutString = "{\"profile\":{\"dma_action\":\"exclude\", \"country_action\":\"exclude\", \"region_action\":\"exclude\", \"city_action\":\"exclude\", \"country_targets\":[],\"city_targets\":[],\"dma_targets\":[],\"region_targets\":[],\"zip_targets\":[]}}"
      anConn.requests.putRequest(properties.getPutneyUrl + s"/campaign?id=${c._2}", campPutString)
      anConn.requests.putRequest(properties.getPutneyUrl + s"/profile?id=${c._3}", proPutString)
    })
  })
}
