package com.mediacrossing.clickfraudreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{AppNexusService}
import scala.collection.JavaConversions._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.joda.time.{DateTimeZone, LocalDate}
import java.io.{File, FileOutputStream}

object RunClickFraudReport extends App {

  // Adjustable Values Used To Determine Fraud
  val minimumClicks = 100
  val minimumCtr = .5f

  // Global Vars
  val LOG = LoggerFactory.getLogger(RunClickFraudReport.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })
  val props = new ConfigurationProperties(args)
  val fileOutputPath = props.getOutputPath
  val anConn = new AppNexusService(props.getPutneyUrl)

  // Request Yest. Imps + Clicks From AN For All Campaigns
  val dataRowsFromAn: List[(String, String, Int, Int, Float)] = anConn.requestClickFraudReport()
    .toList
    .tail
    .map(a => (a(0), a(1), a(2).toInt, a(3).toInt, a(4).toFloat * 100))
    .filter(r => r._4 > minimumClicks && r._5 > minimumCtr)

  if (dataRowsFromAn.size > 0) {
    val wb = new HSSFWorkbook()
    val sheet = wb.createSheet("Sketchy Campaigns")
    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("Campaign")
    headerRow.createCell(1).setCellValue("Imps")
    headerRow.createCell(2).setCellValue("Clicks")
    headerRow.createCell(3).setCellValue("CTR")
    var rowCount = 1
    dataRowsFromAn.foreach(c => {
      val row = sheet.createRow(rowCount)
      row.createCell(0).setCellValue(s"(${c._1}) ${c._2}")
      row.createCell(1).setCellValue(c._3)
      row.createCell(2).setCellValue(c._4)
      row.createCell(3).setCellValue(c._5)
      rowCount += 1
    })
    for(x <- 0 to 3) sheet.autoSizeColumn(x)

    val today = new LocalDate(DateTimeZone.UTC)
    val out = new FileOutputStream(new File(props.getOutputPath, "Sketchy_Campaigns_" + today.toString+ ".xls"))
    wb.write(out)
    out.close()

  } else LOG.info("No Sketchy Campaigns Were Found")

}
