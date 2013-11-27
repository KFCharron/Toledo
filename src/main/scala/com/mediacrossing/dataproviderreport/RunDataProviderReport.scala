package com.mediacrossing.dataproviderreport

import org.slf4j.{LoggerFactory, Logger}
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.{mutable, JavaConversions}
import java.text.SimpleDateFormat
import java.util.Date
import java.lang.Float
import org.joda.time.{DateTimeZone, LocalDate}
import java.io.{File, FileOutputStream}

object RunDataProviderReport {

  //setup logging
  val LOG = LoggerFactory getLogger(RunDataProviderReport.getClass)
  def registerLoggerWithUncaughtExceptions {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
      def uncaughtException(t: Thread, e: Throwable) {
        LOG.error(e.getMessage, e)
      }
    })
  }

  def main(args: Array[String]) {

    registerLoggerWithUncaughtExceptions

    //init variables
    val p = new ConfigurationProperties(args)
    val outputPath = p.getOutputPath
    val anConn = new AppNexusService(
      p getAppNexusUrl,
      p getAppNexusUsername,
      p getAppNexusPassword,
      p getPartitionSize,
      p getRequestDelayInSeconds)
    val mxConn = {
      if (p.getMxUsername == null) new MxService(p getMxUrl)
      else new MxService(p getMxUrl, p getMxUsername, p getMxPassword)
    }

    //get campaign list, convert to scala list
    val campList = JavaConversions.asScalaBuffer(mxConn requestAllCampaigns)

    //create mutable set for unique adIds for report requests
    val adIds = mutable.Set[String]()

    //create set for unique dp names
    val dpNames = mutable.Set[String] ()

    //for each campaign, if today is before end date, add the adId to Set, add serving fee names to set
    campList.foreach(camp => {
      if(!camp.getEndDate.equals("null")) {
        val campEnd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(camp.getEndDate)
        if ((new Date).getTime < campEnd.getTime) {
          adIds.add(camp.getAdvertiserID)
          JavaConversions.asScalaBuffer(camp.getServingFeeList).foreach(sf => dpNames.add(sf.getBrokerName))
        }
      }
    })

    val dpList = mutable.Set[DataProvider]()
    dpNames.foreach(f => dpList.add(new DataProvider(f)))

    //for each adId, request report, and if line matches campaign in list, create the new campaign instance, and
    //save it to the list of which dp it's a part of.
    adIds.foreach(adId => {
      val listResponse = JavaConversions.asScalaBuffer(anConn.getCampaignReport("last_7_days", adId))
      listResponse.foreach(line => {
        campList.foreach(c => {
          if (line(0).equals(c.getId)) {
            val newCamp = new CampaignDP(line(0), line(1), Integer.parseInt(line(2)),
              Integer.parseInt(line(3)), Float.parseFloat(line(8)),
              JavaConversions.asScalaBuffer(c.getServingFeeList))

            dpList.foreach(d => {
              newCamp.getServingFees.foreach(sf => {
                if (sf.getBrokerName.equals(d.getName)) d.getCampaignList.append(newCamp)
              })
            })
          }
        })
      })
    })

    //create workbook
    val wb = new DataProviderReportWriter(dpList).writeReport
    //today's date for file name
    val today = new LocalDate(DateTimeZone.UTC)
    //init file output stream
    val fileOut = new FileOutputStream(new File(outputPath, "DataProviderReport_" + today.toString + ".xls"));
    //write out file
    wb.write(fileOut)
    //close output stream
    fileOut.close()
  }
}
