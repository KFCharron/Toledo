package com.mediacrossing.campaigncleanup

import com.mediacrossing.connections._
import com.mediacrossing.properties.ConfigurationProperties
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

object RunCampaignCleanup extends App {

  val LOG = LoggerFactory.getLogger(RunCampaignCleanup.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val givenLineId = List(
  "1883031",
  "1883033",
  "1883034",
  "1883037",
  "1883039"

  )
  val givenDmaName = "Inglewood 5/2/15"
  // YYYY-MM-dd HH:mm:ss
  val startDate = "2015-04-12 00:00:00"
  val endDate = "2015-05-02 23:59:59"
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
      println(c._1 + " (" + c._2 + ")")
      val campPutString = s"""{"campaign":{"name": "${c._1}", "start_date": "$startDate","end_date": "$endDate"}}"""
      val proPutString = "{\"profile\":{\"dma_action\":\"exclude\", \"country_action\":\"exclude\", \"region_action\":\"exclude\", \"city_action\":\"exclude\", \"country_targets\":[],\"city_targets\":[],\"dma_targets\":[],\"region_targets\":[],\"zip_targets\":[]}}"
      anConn.requests.putRequest(properties.getPutneyUrl + s"/campaign?id=${c._2}", campPutString)
      anConn.requests.putRequest(properties.getPutneyUrl + s"/profile?id=${c._3}", proPutString)
    })
  })
  println("Done.")
}
