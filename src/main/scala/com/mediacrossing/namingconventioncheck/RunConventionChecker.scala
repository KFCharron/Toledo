package com.mediacrossing.namingconventioncheck

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.joda.time.{DateTimeZone, LocalDate}
import java.io.{File, FileOutputStream}
import scala.collection.mutable._
import com.mediacrossing.dailycheckupsreport.Campaign

object RunConventionChecker extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)
  val fileOutputPath = properties.getOutputPath

  val changedCamps = MutableList[Campaign]()

  val camps = mxConn.requestAllCampaigns()
    .toList
    .filter(c => c.getStatus.equals("active"))
    .foreach(c => {

      if ((c.getName.toLowerCase.contains("grapeshot") || c.getName.toLowerCase.contains("peer39")) &&
            !c.getName.toLowerCase.substring(c.getName.length - "contextual".length).contains("contextual")) {

        val name = {c.getName :: "_Contextual" :: Nil}.mkString
        println(s"Changing ${c.getName} to $name")
        val json = "{" + "\"campaign\":" + "{" + "\"name\":" + "\"" + name + "\"" +"}}"
        anConn.putRequest("/campaign?id=" + c.getId + "&advertiser_id=" + c.getAdvertiserID, json)
        changedCamps += c

      }

      if ((c.getName.toLowerCase.contains("tablet") || c.getName.toLowerCase.contains("phone")) &&
            !c.getName.toLowerCase.substring(c.getName.length - "mobile".length).contains("mobile")) {

        val name = {c.getName :: "_Mobile" :: Nil}.mkString
        println(s"Changing ${c.getName} to $name")
        val json = "{" + "\"campaign\":" + "{" + "\"name\":" + "\"" + name + "\"" +"}}"
        anConn.putRequest("/campaign?id=" + c.getId + "&advertiser_id=" + c.getAdvertiserID, json)
        changedCamps += c

      }
    })

  val wb = new HSSFWorkbook()
  val sheet = wb.createSheet("Names Changed")
  val headerRow = sheet.createRow(0)
  headerRow.createCell(0).setCellValue("ID")
  headerRow.createCell(1).setCellValue("Name")
  var rowCount = 1
  changedCamps.foreach(c => {
    val row = sheet.createRow(rowCount)
    row.createCell(0).setCellValue(c.getId)
    row.createCell(1).setCellValue(c.getName)
    rowCount += 1
  })
  sheet.autoSizeColumn(0)
  sheet.autoSizeColumn(1)

  val nowString = new LocalDate(DateTimeZone.UTC).toString
  val fileOut = new FileOutputStream(new File(fileOutputPath, s"Naming_Convention_Update_$nowString.xls"))
  wb.write(fileOut)
  fileOut.close()

}
