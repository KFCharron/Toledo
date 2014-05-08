package com.mediacrossing.dailydomainreport

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{AppNexusService, MxService}
import scala.collection.JavaConversions._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.{File, FileOutputStream}
import org.joda.time.{DateTimeZone, LocalDate}
import com.mediacrossing.sources.appnexus.PutneyConnectivity
import com.mediacrossing.sources.appnexus.reports.buy.{ConfiguredBuyReportRequests, SiteDomainPerformance}
import SiteDomainPerformance.Request

object RunDailyDomainReport
  extends App
  with PutneyConnectivity
  with ConfiguredBuyReportRequests {

  val configuration = new ConfigurationProperties(args)
  val mxConn = {
    if (configuration.getMxUsername == null) new MxService(configuration.getMxUrl)
    else new MxService(configuration.getMxUrl, configuration.getMxUsername, configuration.getMxPassword)
  }
  val anConn = new AppNexusService(
    configuration.getAppNexusUrl,
    configuration.getAppNexusUsername,
    configuration.getAppNexusPassword)

  val ads = mxConn.requestAllAdvertisers().toList.filter(a => a.isLive)

  import scala.concurrent.ExecutionContext.Implicits.global

  //store ad name and list of data rows in class
  val adList = for (a <- ads) yield {
    val name = a.getAdvertiserName
    val id = a.getAdvertiserID
    val dataRows =
      for (line <- siteDomainPerformance(Request(advertiserId = id))
        .tail) yield {
        DataRow(name = line(0),
          imps = line(1).toInt,
          clicks = line(2).toInt,
          ctr = line(3).replace("%", "").toFloat,
          viewConvs = line(4).toInt,
          clickConvs = line(5).toInt,
          cpm = line(6).toFloat)
      }
    DomainAdvertiser(name, id, dataRows.toList)
  }

  //write sheet for every class
  val wb = new HSSFWorkbook()

  val df = wb.createDataFormat()

  val cur = wb.createCellStyle()
  cur.setDataFormat(df.getFormat("$#,##0.00"))

  val num = wb.createCellStyle()
  num.setDataFormat(df.getFormat("#,##0"))

  for (a <- adList) {
    if (a.dataRows.size != 0) {
      val sheet = wb.createSheet(a.name + " (" + a.id + ")")
      val hRow = sheet.createRow(0)
      val headers = List("Domain", "Imps", "Clicks", "CTR (%)", "Post View Convs", "Post Click Convs", "CPM")
      for (x <- 0 to 6) hRow.createCell(x).setCellValue(headers(x))
      for (r <- a.dataRows) {
        val row = sheet.createRow(a.dataRows.indexOf(r) + 1)
        row.createCell(0).setCellValue(r.name)
        row.createCell(1).setCellValue(r.imps)
        row.createCell(2).setCellValue(r.clicks)
        row.createCell(3).setCellValue(r.ctr)
        row.createCell(4).setCellValue(r.viewConvs)
        row.createCell(5).setCellValue(r.clickConvs)
        row.createCell(6).setCellValue(r.cpm)

        row.getCell(1).setCellStyle(num)
        row.getCell(2).setCellStyle(num)
        row.getCell(4).setCellStyle(num)
        row.getCell(5).setCellStyle(num)
        row.getCell(6).setCellStyle(cur)
      }
      for (x <- 0 to 6) sheet.autoSizeColumn(x)
    }
  }
  val today = new LocalDate(DateTimeZone.UTC)
  wb.write(new FileOutputStream(new File(configuration.getOutputPath, "Daily_Domain_Report_" + today.toString + ".xls")))


}

case class DataRow(name: String, imps: Int, clicks: Int, ctr: Float, viewConvs: Int, clickConvs: Int, cpm: Float)

case class DomainAdvertiser(name: String, id: String, dataRows: List[DataRow])
