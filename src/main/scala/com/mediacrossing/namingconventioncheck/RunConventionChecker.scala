package com.mediacrossing.namingconventioncheck

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.joda.time.{DateTimeZone, LocalDate}
import java.io.{File, FileOutputStream}
import scala.collection.mutable._
import org.slf4j.LoggerFactory

object RunConventionChecker extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)
  val fileOutputPath = properties.getOutputPath

  val LOG = LoggerFactory.getLogger(RunConventionChecker.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val changedCamps = MutableList[(String, String)]()

  val camps = mxConn.requestAllCampaigns()
    .toList
    .filter(c => c.getStatus.equals("active"))
    .foreach(c => {

      if ((c.getName.toLowerCase.contains("grapeshot") || c.getName.toLowerCase.contains("peer39")) &&
        !(c.getName.toLowerCase.substring(c.getName.length - "mobile".length).contains("mobile") ||
          c.getName.toLowerCase.substring(c.getName.length - "contextual".length).contains("contextual"))) {
        val oldName = c.getName
        val newName = {c.getName :: "_Contextual" :: Nil}.mkString
        println(s"Changing ${c.getName} to $newName")
        val json = "{" + "\"campaign\":" + "{" + "\"name\":" + "\"" + newName + "\"" +"}}"
        anConn.putRequest("/campaign?id=" + c.getId + "&advertiser_id=" + c.getAdvertiserID, json)
        changedCamps += ((oldName, newName))

      }

      if ((c.getName.toLowerCase.contains("tablet") || c.getName.toLowerCase.contains("phone")) &&
          !(c.getName.toLowerCase.substring(c.getName.length - "mobile".length).contains("mobile") ||
            c.getName.toLowerCase.substring(c.getName.length - "contextual".length).contains("contextual"))) {
        val oldName = c.getName
        val newName = {c.getName :: "_Mobile" :: Nil}.mkString
        println(s"Changing ${c.getName} to $newName")
        val json = "{" + "\"campaign\":" + "{" + "\"name\":" + "\"" + newName + "\"" +"}}"
        anConn.putRequest("/campaign?id=" + c.getId + "&advertiser_id=" + c.getAdvertiserID, json)
        changedCamps += ((oldName, newName))

      }
    })

  if (changedCamps.size > 0) {
    val wb = new HSSFWorkbook()
    val sheet = wb.createSheet("Names Changed")
    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("From")
    headerRow.createCell(1).setCellValue("To")
    var rowCount = 1
    changedCamps.foreach(c => {
      val row = sheet.createRow(rowCount)
      row.createCell(0).setCellValue(c._1)
      row.createCell(1).setCellValue(c._2)
      rowCount += 1
    })
    sheet.autoSizeColumn(0)
    sheet.autoSizeColumn(1)

    val today = new LocalDate(DateTimeZone.UTC)
    val out = new FileOutputStream(new File(properties.getOutputPath, "Changed_Campaign_Names_" + today.toString+ ".xls"))
    wb.write(out)
    out.close()

  } else LOG.info("No Naming Convention Errors Were Found")


}
