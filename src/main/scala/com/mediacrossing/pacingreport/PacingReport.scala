package com.mediacrossing.pacingreport

import org.apache.poi.ss.usermodel._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.joda.time.format.DateTimeFormat

class PacingReport {
  def generate(flightMap: Map[String, List[LineItem]]) : Workbook = {

    val wb = new HSSFWorkbook


    //val noBudgetFlights = ("No Budget Set", flightMap.mapValues(list => list.filter(line => line.yestDelivery == 0)).filter(m => m._2.size > 0))

    // Find Latest End Date, If On Or After End Date, Add Here
    val completedFlights = ("Completed Flights", flightMap.mapValues(list => list.filter(line => line.yestDelivery == 0)).filter(m => m._2.size > 0))

    // Sum all lines, and filter out ones that don't meet bad criteria
    val pacingConcernFlights = ("Pacing Watchlist", flightMap.mapValues(list => list.filter(line => line.yestDelivery == 0)).filter(m => m._2.size > 0))

    val allFlights = ("All Flights", flightMap)



    val flightHeaderStyle = wb.createCellStyle()
    flightHeaderStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex)
    flightHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)
    val headerFont = wb.createFont()
    headerFont.setBoldweight(1000)
    headerFont.setColor(IndexedColors.WHITE.getIndex)
    flightHeaderStyle.setFont(headerFont)

    val boldStyle = wb.createCellStyle()
    val boldFont = wb.createFont()
    boldFont.setBoldweight(1000)
    boldStyle.setFont(boldFont)

    val dtf = DateTimeFormat.forPattern("MM/dd/YYYY")

    {allFlights :: Nil}
      .foreach(a => writePacingSheet(a._1, a._2))

    // Write method that writes a sheet with the given flight map
    def writePacingSheet(title: String, flights: Map[String, List[LineItem]]) : Unit = {
      val sheet = wb.createSheet(title)
      var rowCount = 1
      flights.foreach(f => {

        val flightHeaderRow = sheet.createRow(rowCount)
        for (x <- 0 to 8) {
          flightHeaderRow.createCell(x).setCellStyle(flightHeaderStyle)
        }
        flightHeaderRow.getCell(0).setCellValue(f._1)


        rowCount += 1

        val headerRow = sheet.createRow(rowCount)
        headerRow.createCell(0).setCellValue("Line Item")
        headerRow.createCell(1).setCellValue("Start")
        headerRow.createCell(2).setCellValue("End")
        headerRow.createCell(3).setCellValue("Days Left")
        headerRow.createCell(4).setCellValue("Daily Budget")
        headerRow.createCell(5).setCellValue("Yesterday Delivery")
        headerRow.createCell(6).setCellValue("Daily Imps Needed")
        headerRow.createCell(7).setCellValue("Lifetime Budget")
        headerRow.createCell(8).setCellValue("Lifetime Delivery")
        for(x <- 0 to 8) headerRow.getCell(x).setCellStyle(boldStyle)


        rowCount += 1

        f._2.foreach(l => {
          val lineRow = sheet.createRow(rowCount)
          lineRow.createCell(0).setCellValue(s"(${l.id}) ${l.name}")
          lineRow.createCell(1).setCellValue(l.start.toString(dtf))
          lineRow.createCell(2).setCellValue(l.end.toString(dtf))
          lineRow.createCell(3).setCellValue(l.daysLeft)
          lineRow.createCell(4).setCellValue(l.dailyBudget)
          lineRow.createCell(5).setCellValue(l.yestDelivery)
          lineRow.createCell(6).setCellValue(l.dailyImpsNeeded)
          lineRow.createCell(7).setCellValue(l.ltBudget)
          lineRow.createCell(8).setCellValue(l.ltDelivery)
          for (x <- 3 to 8) lineRow.getCell(x).setCellType(Cell.CELL_TYPE_NUMERIC)
          rowCount += 1
        }
        )

        rowCount += 2

      })
      for (x <- 0 to 8) sheet.autoSizeColumn(x)
    }

    wb
  }
}
