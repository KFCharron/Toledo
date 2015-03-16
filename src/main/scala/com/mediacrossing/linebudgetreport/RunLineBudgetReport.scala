package com.mediacrossing.linebudgetreport

import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.properties.ConfigurationProperties
import scala.collection.JavaConversions._

object RunLineBudgetReport extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val outputFile = "Live_Line_Item_Budgets.csv"
  val home = properties.getOutputPath
  val path = (new java.io.File(home, outputFile)).getAbsolutePath()
  val pw = new java.io.PrintWriter(path)
  pw.println("ID, Name, LT Budget")
  mxConn.requestAllLineItems()
    .toList
    .filter(l => l.getStatus.equals("active"))
    .map(l => s"""${l.getId}, ${l.getName}, ${l.getLifetimeBudget}""")
    .foreach(pw.println)
}
