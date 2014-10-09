//package com.mediacrossing.deliverybyweek
//
//import org.slf4j.LoggerFactory
//import com.mediacrossing.properties.ConfigurationProperties
//import com.mediacrossing.connections.{MxService, AppNexusService}
//import scala.collection.JavaConversions._
//import org.joda.time.DateTime
//import org.joda.time.format.DateTimeFormat
//
//
//object RunDeliveryByWeek extends App {
//
//  // Global Vars
//  val LOG = LoggerFactory.getLogger(RunDeliveryByWeek.getClass)
//  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
//    def uncaughtException(t: Thread, e: Throwable) {
//      LOG.error(e.getMessage, e)
//    }
//  })
//  val props = new ConfigurationProperties(args)
//  val fileOutputPath = props.getOutputPath
//  val anConn = new AppNexusService(
//    props.getPutneyUrl
//  )
//  val mxConn = {
//    if (props.getMxUsername == null) new MxService(props.getMxUrl)
//    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
//  }
//
//
//
//  // Request lifetime Impression numbers by day for each line item for each feld advertiser
//  val report = anConn.requestWeeklyImpReport().toList
//    .tail
//    .map(l => DataRow(DateTime.parse(l(0), dtf.forPattern("YYYY-MM-dd")), l(1), l(2), l(3).toInt))
//    .groupBy(_.adId)
//    .mapValues(dataList => dataList.groupBy(_.lineItem)
//                                   .mapValues(d => d.map(l => (l.day, l.imps)).groupBy(_._1.getWeekOfWeekyear)))
//
//
//}
//case class DataRow(day: DateTime, adId: String, lineItem: String, imps: Int)
