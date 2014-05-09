//package com.mediacrossing.dailypacingreport
//
//import com.mediacrossing.properties.ConfigurationProperties
//import com.mediacrossing.connections.{AppNexusService, MxService}
//import org.joda.time.DateTime
//import scala.collection.JavaConversions._
//import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
//import scala.collection.immutable.Iterable
//
//
//object RunPacing extends App {
//
//  val props = new ConfigurationProperties(args)
//  val mxConn = {
//    if (props.getMxUsername == null) new MxService(props.getMxUrl)
//    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
//  }
//  val anConn = new AppNexusService(
//    props.getAppNexusUrl,
//    props.getAppNexusUsername,
//    props.getAppNexusPassword)
//
//  val dateFormat: DateTimeFormatter = DateTimeFormat
//    .forPattern("yy-mm-dd")
//
//  // Query MX for all adverts.
//  // Filter one's that are active
//  // Set Date Range to Earliest Start and Today
//
//  val dataRows: List[DataRow] = anConn.requestLifetimePacingReport
//    .toList
//    .tail
//    .map(line => DataRow(advertiserName = line(0),
//                        lineItemName = line(1),
//                        day = dateFormat.parseDateTime(line(2)),
//                        imps = line(3).toInt))
//
//  val advertisers: Iterable[Map[String, List[DataRow]]] = dataRows.groupBy(r => r.advertiserName).map(a => a._2.groupBy(l => l.lineItemName))
//
//
//}
//case class DataRow(advertiserName: String, lineItemName: String, day: DateTime, imps: Int)
////case class Advertiser()
