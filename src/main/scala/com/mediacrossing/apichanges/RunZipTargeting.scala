package com.mediacrossing.apichanges

import java.io.FileReader
import au.com.bytecode.opencsv.CSVReader
import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.properties.ConfigurationProperties
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

object RunZipTargeting extends App {

  // Logging
  val LOG = LoggerFactory.getLogger(RunZipTargeting.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  // Properties
  val props = new ConfigurationProperties(args)
  val anConn = new AppNexusService(props.getPutneyUrl)
  val mxConn = new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)

  val lineIds = List(
    "1724088",
    "1724089",
    "1724090"
  )

  //import zip csv
  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Downloads/nassau zips.csv"))
  val zipList: List[String] = reader.readAll
    .toList
    .map(_(0))
  //generate zip string
  val zipString = zipList.map(z => s"""{"from_zip": "$z", "to_zip": "$z"}""").mkString(",")
  val finalPost = s"""{"profile":{"zip_targets":[$zipString]}}"""

  // Get all campaigns
  // filter by line item list
  // map to proIds
  val proIds = mxConn.requestAllCampaigns().toList.filter(c => lineIds.contains(c.getLineItemID)).map(_.getProfileID)

  // query each pro
  proIds.foreach(p => anConn.putRequest(s"/profile?id=$p", finalPost))

}