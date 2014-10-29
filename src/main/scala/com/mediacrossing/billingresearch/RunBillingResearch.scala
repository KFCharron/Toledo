package com.mediacrossing.billingresearch

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import au.com.bytecode.opencsv.CSVReader
import java.io.{File, FileOutputStream, FileReader}
import org.apache.poi.hssf.usermodel.HSSFWorkbook


object RunBillingResearch extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val adIdToNameMap = mxConn.requestAllAdvertisers().toList.map(a => a.getAdvertiserID -> a.getAdvertiserName).toMap

  val adToCampaignMap = mxConn.requestAllCampaigns()
    .toList
    .map(c => (adIdToNameMap.get(c.getAdvertiserID).get, s"${c.getName} (${c.getId})", c.getServingFeeList.toList.map(_.getBrokerName)))
    .groupBy(_._1)
    .mapValues(listOfTups => listOfTups.map(t => t._2 -> t._3).toMap)

  adToCampaignMap.foreach(a => println(a._1))

  //TODO set File Source
  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/July_AllAdvert.csv"))
  val campToImpDataMap = reader
    .readAll()
    .toList
    .tail
    .map(a => a(0) -> (a(1).toInt, a(2).toFloat))
    .toMap

  val wb = new HSSFWorkbook()

  adToCampaignMap.foreach(a => {
    println(a._1)
    val sheet = wb.createSheet(a._1)

    var cellCount = 3
    var rowCount = 0

    val providerNames = a._2.map(m => m._2).flatten.toSet
    val headerRow = sheet.createRow(rowCount)
    headerRow.createCell(0).setCellValue("Campaign")
    headerRow.createCell(1).setCellValue("Imps")
    headerRow.createCell(2).setCellValue("Cost")
    providerNames.foreach(n => {
      headerRow.createCell(cellCount).setCellValue(n)
      cellCount += 1
    })

    rowCount += 1

    a._2.foreach(c => {
      val campRow = sheet.createRow(rowCount)
      cellCount = 3
      val impCostTup = campToImpDataMap.get(c._1).getOrElse((0, 0.0f))
      if(impCostTup._1 > 0) {
        campRow.createCell(0).setCellValue(c._1)
        campRow.createCell(1).setCellValue(impCostTup._1)
        campRow.createCell(2).setCellValue(impCostTup._2)
        while(cellCount < headerRow.getPhysicalNumberOfCells) {
          val imps = {
            if(c._2.contains(headerRow.getCell(cellCount).getStringCellValue)) impCostTup._1
            else 0
          }
          campRow.createCell(cellCount).setCellValue(imps)
          cellCount += 1
        }
        rowCount += 1
      }
    })
    for(x <- 0 to headerRow.getPhysicalNumberOfCells-1) sheet.autoSizeColumn(x)
  })

  val fileOut: FileOutputStream = new FileOutputStream(new File(properties.getOutputPath, "July_AllAdvertiserBreakdown.xls"))
  wb.write(fileOut)
  fileOut.close()

}
