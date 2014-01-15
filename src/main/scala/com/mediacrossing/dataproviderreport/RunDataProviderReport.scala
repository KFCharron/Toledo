package com.mediacrossing.dataproviderreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import java.io.{File, FileOutputStream}
import org.joda.time.format.DateTimeFormat

object RunDataProviderReport extends App {

  //setup logging
  val LOG = LoggerFactory.getLogger(RunDataProviderReport.getClass)


  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  //init variables
  val p = new ConfigurationProperties(args)
  val anConn = new AppNexusService(
    p.getAppNexusUrl,
    p.getAppNexusUsername,
    p.getAppNexusPassword,
    p.getPartitionSize,
    p.getRequestDelayInSeconds)
  val mxConn = {
    if (p.getMxUsername == null) new MxService(p.getMxUrl)
    else new MxService(p.getMxUrl, p.getMxUsername, p.getMxPassword)
  }

  //get campaign list, convert to scala list
  val campList = mxConn.requestAllCampaigns.toList

  val campaignDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

  //TODO: Separate report generation function out so it can be tested on it's own
  val currentTime = DateTime.now
  //for each campaign, if today is before end date, add the adId to Set, add serving fee names to set
  val (advertiserIds, dpNames) = campList.filter(camp => {
    !(camp.getEndDate == "null") && {
      val campEnd = campaignDateFormat.parseDateTime(camp.getEndDate)
      currentTime.isBefore(campEnd)
    }
  }).map(vc => (vc.getAdvertiserID, vc.getServingFeeList.map(_.getBrokerName))).unzip

  val dpList = dpNames.flatten

//  for each adId, request report, and if line matches campaign in list, create the new campaign instance, and
//  save it to the list of which dp it's a part of.
  val dataProviders: List[DataProvider] = (for {
    adId: String <- advertiserIds.toSet
    line: Array[String] <- anConn.getCampaignReport("last_7_days", adId).toList
    c <- campList if line(0) == c.getId
    newCamp = new CampaignDP(line(0), line(1), Integer.parseInt(line(2)),
      Integer.parseInt(line(3)), line(8).toDouble,
      c.getServingFeeList.toList)
    dp <- dpList
    sf <- newCamp.servingFees if sf.getBrokerName == dp
  } yield dp -> newCamp).toList.groupBy(_._1).map {
  case (k, v) => DataProvider(k, v.map(_._2))
}.toList


  //create workbook
  val wb = new DataProviderReportWriter(dataProviders).writeReport
  //today's date for file name
  val today = new LocalDate(DateTimeZone.UTC)
  //init file output stream
  val fileOut = new FileOutputStream(new File(p.getOutputPath, "DataProviderReport_" + today.toString + ".xls"))
  //write out file
  wb.write(fileOut)
  //close output stream
  fileOut.close()
}
