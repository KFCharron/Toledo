package com.mediacrossing.intradaypacingreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.mediacrossing.dailypacingreport.PacingLineItem


object RunIntradayPacingReport extends App {

  // Global Vars
  val LOG = LoggerFactory.getLogger(RunIntradayPacingReport.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })
  val props = new ConfigurationProperties(args)
  val fileOutputPath = props.getOutputPath
  val anConn = new AppNexusService(
    props.getPutneyUrl
  )
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }

  // Get List of Live Line Items
  val activeLines = mxConn.requestAllLineItems()
    .toList
    .filter(l => l.getStatus.equals("active"))

  // Map each Line Item to their detail metrics, requesting and parsing the detail metrics
  val mapped: List[(PacingLineItem, (Int, Array[Long], Array[Long]))] = activeLines.map(l => l -> {
    val adId = l.getAdvertiserId
    val lineId = l.getId
    val today = new DateTime()
    val start = today.minusDays(1).toString("YYYY-MM-dd")
    val end = today.toString("YYYY-MM-dd")
    val helixUrl = s"${props.getMxUrl}/api/dashboard/buy-side/detail/advertisers/$adId/line-items/$lineId?delta=60&start=$start&end=$end"
    // Get Request
    val json = mxConn.requestPacingMetricJson(helixUrl)
    println(json)
    val metricR = (
      (__ \ "summaryMetrics").\("imps").read[Int] ~
        (__ \ "detailMetrics").\("timestamps").read[Array[Long]] ~
        (__ \ "detailMetrics").\("imps").read[Array[Long]]
      ).tupled

    Json.parse(json)
      .validate(metricR) match {
        case e@JsError(_) => sys.error(JsError.toFlatJson(e).toString())
        case JsSuccess(v, _) => v
    }

  })
//  def trouble(line : (PacingLineItem, (Int, Array[Long], Array[Long]))) : Boolean = {
//
//  }
//  // Filter the line items for the scenario
//  val troublePacers = mapped.filter(l => {
//    trouble(l)
//  })

  // List the line items

}
