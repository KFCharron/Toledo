package com.mediacrossing.dataproviderreport

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook

class DataProviderReportWriter(dataProviders: List[DataProvider]) {
  def writeReport : Workbook = {
    //create new wb
    val wb = new HSSFWorkbook()
    val sheet = wb createSheet "Data Providers"
    var rowCount = 0

    val headerRow = sheet createRow rowCount
    val headers = Map((0, "Data Provider"), (1, "No. of Campaigns"), (2, "Imps"), (3, "Clicks"), (4, "Average CPM"))
    headers.foreach(h => headerRow createCell h._1 setCellValue h._2)

    rowCount += 1

    dataProviders.foreach(dp => {
      val dataRow = sheet createRow rowCount
      dataRow createCell 0 setCellValue dp.name
      dataRow createCell 1 setCellValue dp.campaignList.size
      dataRow createCell 2 setCellValue dp.totalImps
      dataRow createCell 3 setCellValue dp.totalClicks
      dataRow createCell 4 setCellValue dp.averageCpm

      rowCount += 1
    })

    for (x <- 0 to 4) sheet autoSizeColumn x

    wb
  }
}
