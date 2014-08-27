package com.mediacrossing.pacingreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import org.joda.time.{DateTimeZone, LocalDate, Duration, DateTime}
import java.util
import com.mediacrossing.dailypacingreport.PacingLineItem
import java.io.{File, FileOutputStream}

object RunPacing extends App {

  // Global Vars
  val LOG = LoggerFactory.getLogger(RunPacing.getClass)
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

  // Get List of all LineItems we're concerned about
  val today = new DateTime().withTimeAtStartOfDay()
  val lineItemsOfConcern = mxConn.requestAllLineItems().toList
    .filter(l => l.getStartDate.isBefore(today) &&
      l.getEndDate.isAfter(today.minusDays(3)))

  // Find Earliest Start Date To Use For Lifetime Report
  var earliestStartDate = today
  lineItemsOfConcern.foreach(l => {
    if (l.getStartDate.isBefore(earliestStartDate)) earliestStartDate = l.getStartDate
  })

  // Method Converting CSV to Map[LineId, Imps]
  def listToLineImpMap(orig : util.List[Array[String]]): Map[String, Int] = orig
    .toList
    .tail
    .map(line => (line(0), line(1).toInt))
    .toMap

  // Get LT Data For All Line Items Using Earliest Start Date
  val lifetimeLineToImpMap = listToLineImpMap(anConn.requestLifetimeLineItemImps(earliestStartDate))

  // Get Yesterday Data For All Line Items Using Earliest Start Date
  val yesterdayLineToImpMap = listToLineImpMap(anConn.requestYesterdayLineItemImps())

  // map Concerned Line Items To New Case Class LineItem, Calculate Other Fields
  val lineList = lineItemsOfConcern.map(l =>
    LineItem(
      l,
      lifetimeLineToImpMap.get(l.getId).getOrElse(0),
      yesterdayLineToImpMap.get(l.getId).getOrElse(0)
    )
  )

  // Change Each Line Item To Map w/ Key as Flight Name, Group By Key
  val flightMap = lineList.map( l => {
      // What you want the key to be
      val parsed = l.name.split("]")
      val name = l.name.toLowerCase
      val lineType = {
       if (name.contains("audio") || name.contains("radio")) "Radio"
        else if(name.contains("preroll") || name.contains("video")) "Video"
        else "Display"
      }
      val adName = parsed(0).substring(3)
      val flightName = parsed(1).substring(4)

      s"$adName - $flightName ($lineType)"

  } -> l)
    .groupBy(_._1)
    .mapValues(_.map(_._2))

  // Generate Report
  val wb = new PacingReport().generate(flightMap)
  val nowString = new LocalDate(DateTimeZone.UTC).toString
  val fileOut = new FileOutputStream(new File(fileOutputPath, s"Daily_Pacing_Report_$nowString.xls"))
  wb.write(fileOut)
  fileOut.close()
}

case class LineItem(config: PacingLineItem, lt: Int, yest: Int) {
  val id = config.getId
  val name = config.getName
  val start = config.getStartDate
  val end = config.getEndDate
  val daysLeft = {
    val dur = new Duration(new DateTime(), end).getStandardDays
    if (dur < 0) 0 else dur
  }
  val dailyBudget = config.getDailyBudget
  val yestDelivery = yest
  val dailyImpsNeeded = {
    if (config.getLifetimeBudget == 0) 0
    else (config.getLifetimeBudget - lt) / daysLeft
  }
  val ltBudget = config.getLifetimeBudget
  val ltDelivery = lt
}
