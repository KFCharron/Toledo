package com.mediacrossing.dailyrundown

import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.properties.ConfigurationProperties
import scala.collection.JavaConversions._


object RunDailyRundown extends App {

  // Imports
  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  /* Get Line Item config from MX:
  *   Name
  *   ID
  *   Start Date
  *   End Date
  */
  val lines = mxConn.requestAllLineItems().toList


  /* Get lifetime Line Item delivery data from AN:
  *  Imps
  *  Spend
  *  Revenue
  */
  //val lineData = anConn.requestLineItemRundown().toList


  // parse G: into its own column

  // Generate Report

}
