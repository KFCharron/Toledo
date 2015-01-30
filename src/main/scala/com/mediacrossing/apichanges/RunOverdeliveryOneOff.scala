package com.mediacrossing.apichanges

import java.io.FileReader

import au.com.bytecode.opencsv.CSVReader
import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.placementblacklist.RunPlacementBlacklist._
import com.mediacrossing.properties.ConfigurationProperties
import scala.collection.JavaConversions._

object RunOverdeliveryOneOff extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)


  // pull in line report csv
  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Downloads/Camp_Yest.csv"))
  val arrs: List[DataRow] = reader.readAll
    .toList
    .tail
    .map(a => DataRow(a(0), a(1), a(2), a(3), a(4)))

  // request lines
  val lines = mxConn.requestAllCampaigns().toList
   .map(l => (l.getId, l.getDailyImpBudget)).toMap

  // map budgets to line names w/ id in them
  val home = System.getProperty( "user.home" )
  val path = (new java.io.File(home + "/Desktop", "Camps.csv")).getAbsolutePath()
  val pw = new java.io.PrintWriter(path)
  val head = List("ID", "Line Item", "Budget", "Imps", "Spend", "CPM").mkString(",")
  pw.println(head)
  arrs.foreach(a => {
    println(a.id)
    val s = List(a.id, a.name, lines.get(a.id).get, a.imps, a.cost, a.cpm).mkString(",")
    pw.println(s)
  })
  pw.close
  // repeat for camps



}
case class DataRow(id: String, name: String, imps: String, cost: String, cpm: String)
