package com.mediacrossing.pacingreport

import org.apache.poi.ss.usermodel._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.joda.time.format.DateTimeFormat
import scala.collection.immutable.SortedMap

class PacingReport {
  def generate(flightMap: Map[String, List[LineItem]]) : Workbook = {

    val wb = new HSSFWorkbook

    val noBudgetFlights = (
      "AdX & Search",
      SortedMap(
        flightMap
          .mapValues(lineList => lineList.filter(line => line.name.contains("AdX") ||
                                                         line.name.contains("Search")))
          .filter(m => m._2.size > 0)
          .toSeq:_*
      )
    )

    // Sum all lines, and filter out ones that don't meet bad criteria
    val pacingConcernFlights = (
      "Pacing Watchlist",
      SortedMap(
        flightMap
          .mapValues(lineList => lineList.filterNot(line => line.name.contains("AdX") ||
                                                            line.name.contains("Search")))
          .filter(m => m._2.size > 0)
          .filter(flight => {
            flight._2.map(_.yestDelivery).sum < (.9f * flight._2.map(_.dailyImpsNeeded).foldLeft(0L)(_+_))
          })
          .toSeq:_*
      )
    )

    val allFlights = (
      "All Flights",
      SortedMap(flightMap.toSeq:_*)
    )

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

    val numberStyle = wb.createCellStyle()
    val numberFormat = wb.createDataFormat()
    numberStyle.setDataFormat(numberFormat.getFormat("#,##0"))

    val dtf = DateTimeFormat.forPattern("MM/dd/YYYY")

    {noBudgetFlights :: pacingConcernFlights :: allFlights :: Nil}
      .foreach(a => writePacingSheet(a._1, a._2))

    // Write method that writes a sheet with the given flight map
    def writePacingSheet(title: String, flights: Map[String, List[LineItem]]) : Unit = {
      val sheet = wb.createSheet(title)
      var rowCount = 1
      flights.foreach(f => {

        val flightHeaderRow = sheet.createRow(rowCount)
        for (x <- 0 to 9) {
          flightHeaderRow.createCell(x).setCellStyle(flightHeaderStyle)
        }
        flightHeaderRow.getCell(0).setCellValue(f._1)

        rowCount += 1

        val headerRow = sheet.createRow(rowCount)
        headerRow.createCell(0).setCellValue("Line Item")
        headerRow.createCell(1).setCellValue("Start")
        headerRow.createCell(2).setCellValue("End")
        headerRow.createCell(3).setCellValue("Days Left")
        headerRow.createCell(4).setCellValue("Budget Remaining")
        headerRow.createCell(5).setCellValue("Daily Budget")
        headerRow.createCell(6).setCellValue("Yesterday Delivery")
        headerRow.createCell(7).setCellValue("Daily Imps Needed")
        headerRow.createCell(8).setCellValue("Lifetime Budget")
        headerRow.createCell(9).setCellValue("Lifetime Delivery")
        for(x <- 0 to 9) headerRow.getCell(x).setCellStyle(boldStyle)

        rowCount += 1

        f._2.foreach(l => {
          val lineRow = sheet.createRow(rowCount)
          lineRow.createCell(0).setCellValue(s"(${l.id}) ${l.name}")
          lineRow.createCell(1).setCellValue(l.start.toString(dtf))
          lineRow.createCell(2).setCellValue(l.end.toString(dtf))
          lineRow.createCell(3).setCellValue(l.daysLeft)
          lineRow.createCell(4).setCellValue(l.budgetLeft)
          lineRow.createCell(5).setCellValue(l.dailyBudget)
          lineRow.createCell(6).setCellValue(l.yestDelivery)
          lineRow.createCell(7).setCellValue(l.dailyImpsNeeded)
          lineRow.createCell(8).setCellValue(l.ltBudget)
          lineRow.createCell(9).setCellValue(l.ltDelivery)
          for (x <- 3 to 9) {
            lineRow.getCell(x).setCellType(Cell.CELL_TYPE_NUMERIC)
            lineRow.getCell(x).setCellStyle(numberStyle)
          }
          rowCount += 1
        }
        )

        rowCount += 1

        val totalDailyBudget = f._2.map(l => l.dailyBudget).sum
        val totalYesterdayDelivery = f._2.map(l => l.yestDelivery).sum
        val totalImpsNeeded = f._2.map(l => l.dailyImpsNeeded).sum
        val totalLifetimeBudget = f._2.map(l => l.ltBudget).sum
        val totalLifetimeDelivery = f._2.map(l => l.ltDelivery).sum
        val maxDays = f._2.map(_.daysLeft).max
        val totalBudgetLeft = f._2.map(_.budgetLeft).sum

        val totalRow = sheet.createRow(rowCount)
        totalRow.createCell(0).setCellValue("Flight Totals:")
        totalRow.createCell(3).setCellValue(maxDays)
        totalRow.createCell(4).setCellValue(totalBudgetLeft)
        totalRow.createCell(5).setCellValue(totalDailyBudget)
        totalRow.createCell(6).setCellValue(totalYesterdayDelivery)
        totalRow.createCell(7).setCellValue(totalImpsNeeded)
        totalRow.createCell(8).setCellValue(totalLifetimeBudget)
        totalRow.createCell(9).setCellValue(totalLifetimeDelivery)
        for (x <- 3 to 9) {
          totalRow.getCell(x).setCellType(Cell.CELL_TYPE_NUMERIC)
          totalRow.getCell(x).setCellStyle(numberStyle)
        }

        rowCount += 2

      })
      for (x <- 0 to 9) sheet.autoSizeColumn(x)
    }

    wb
  }
}
