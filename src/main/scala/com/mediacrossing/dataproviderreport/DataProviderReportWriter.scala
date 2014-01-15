package com.mediacrossing.dataproviderreport

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook

class DataProviderReportWriter(dataProviders: List[DataProvider]) {
  def writeReport : Workbook = {
    //create new wb
    val wb = new HSSFWorkbook()

    val df = wb.createDataFormat()
    val numbers = wb.createCellStyle()
    numbers.setDataFormat(df.getFormat("#,###,###"))
    val currency = wb.createCellStyle()
    currency.setDataFormat(df.getFormat("$#,##0.00"))
    val percentage = wb.createCellStyle()
    percentage.setDataFormat(df.getFormat("0.00%"))
    
    val sumSheet = wb createSheet "Data Providers"
    var rowCount = 0

    val headerRow = sumSheet.createRow(rowCount)
    val headers = Map((0, "Data Provider"), (1, "No. of Campaigns"), (2, "Imps"), (3, "Clicks"), (4, "Average CPM"))
    headers.foreach(h => headerRow createCell h._1 setCellValue h._2)

    rowCount += 1

    dataProviders.foreach(dp => {
      val dataRow = sumSheet.createRow(rowCount)
      dataRow createCell 0 setCellValue dp.name
      dataRow createCell 1 setCellValue dp.campaignList.size
      dataRow createCell 2 setCellValue dp.totalImps
      dataRow createCell 3 setCellValue dp.totalClicks
      dataRow createCell 4 setCellValue dp.averageCpm

      rowCount += 1

      val dpSheet = wb.createSheet(dp.name)
      val head = dpSheet.createRow(0)
      val dpHeaders = Map((0, "ID"), (1, "Campaign"), (2, "Imps"), (3, "Clicks"), (4, "cpm"))
      dpHeaders.foreach(h => head.createCell(h._1).setCellValue(h._2))
      var count = 1
      dp.campaignList.foreach(c => {
        val dataRow = dpSheet.createRow(count)
        dataRow.createCell(0).setCellValue(c.id)
        dataRow.createCell(1).setCellValue(c.name)
        dataRow.createCell(2).setCellValue(c.imps)
        dataRow.createCell(3).setCellValue(c.clicks)
        dataRow.createCell(4).setCellValue(c.cpm)
        
        dataRow.getCell(2).setCellStyle(numbers)
        dataRow.getCell(3).setCellStyle(numbers)
        dataRow.getCell(4).setCellStyle(currency)
        
        count += 1
      })
      val totalRow = dpSheet.createRow(count+1)
      totalRow.createCell(0).setCellValue("Totals:")
      totalRow.createCell(2).setCellValue(dp.campaignList.foldLeft(0)(_ +_.imps))
      totalRow.createCell(3).setCellValue(dp.campaignList.foldLeft(0)(_ +_.clicks))

      totalRow.getCell(2).setCellStyle(numbers)
      totalRow.getCell(3).setCellStyle(numbers)

      for (x <- 0 to 4) dpSheet.autoSizeColumn(x)
    })

    for (x <- 0 to 4) sumSheet autoSizeColumn x

    wb
  }
}
