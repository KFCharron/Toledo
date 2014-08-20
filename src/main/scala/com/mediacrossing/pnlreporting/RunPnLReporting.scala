package com.mediacrossing.pnlreporting

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{AppNexusService, MxService}
import scala.collection.JavaConversions._
import com.mediacrossing.dailycheckupsreport.ServingFee
import com.mediacrossing.dailycheckupsreport
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.{File, FileOutputStream}
import org.apache.poi.ss.usermodel.{CellStyle, IndexedColors}


object RunPnLReporting extends App {

  val LOG = LoggerFactory.getLogger(RunPnLReporting.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val props = new ConfigurationProperties(args)
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }
  val anConn = new AppNexusService(props.getPutneyUrl)

  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

  // Get Report, Convert To Data Rows, Campaign 0s?
  val data = anConn.requestAnalyticsReport()
    .toList
    .tail
    .map(l => AnalyticsDataRow(campaignId = l(0),
                               campaignName = l(1),
                               lineId = l(2),
                               lineName = l(3),
                               adName = l(5),
                               adId = l(4),
                               campType = l(6),
                               imps = l(7).toInt,
                               clicks = l(8).toInt,
                               convsRaw = l(9).toInt,
                               mediaCost = l(10).toFloat,
                               networkRevenueRaw = l(11).toFloat,
                               sellerName = l(12),
                               day = dateFormat.parseDateTime(l(13)).withTimeAtStartOfDay()))
    .filterNot(r => r.campaignId.equals("0") || r.adId.equals("151391"))
    .groupBy(r => r.campaignId)


  // Request List of Camps From Khaju
  val camps: Map[String, List[dailycheckupsreport.Campaign]] = mxConn.requestAllCampaigns()
    .toList
    .groupBy(c => c.getId)

  val finalCamps = data.map(d => {
    Campaign(
      d._2,
      camps
        .get(d._1)
        .get
        .head
        .getServingFeeList
        .toList
        .filterNot(f => f.getBrokerName.equals("MediaCrossing"))
    )
  })

  // ********* WORKBOOK CREATION

  val numCamps = finalCamps.size
  val wb = new HSSFWorkbook()
  val df = wb.createDataFormat()

  val currency = wb.createCellStyle()
  currency.setDataFormat(df.getFormat("$#,##0.00"))

  val greenCurrency = wb.createCellStyle()
  greenCurrency.setDataFormat(df.getFormat("$#,##0.00"))
  greenCurrency.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex)
  greenCurrency.setFillPattern(CellStyle.SOLID_FOREGROUND)

  val greenNumber = wb.createCellStyle()
  greenNumber.setDataFormat(df.getFormat("#,##0"))
  greenNumber.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex)
  greenNumber.setFillPattern(CellStyle.SOLID_FOREGROUND)

  val number = wb.createCellStyle()
  number.setDataFormat(df.getFormat("#,##0"))

  val bold = wb.createCellStyle()
  val boldFont = wb.createFont()
  boldFont.setBoldweight(1000)
  bold.setFont(boldFont)

  // ***** Summary Sheet

  val homeSheet = wb.createSheet("Summary")
  var row = homeSheet.createRow(0)

  row.createCell(0).setCellValue("Metrics")
  row.createCell(1).setCellValue("Yesterday")
  row.createCell(2).setCellValue("7 days")
  row.createCell(3).setCellValue("30 days")
  row.foreach(c => c.setCellStyle(bold))

  row = homeSheet.createRow(1)
  row.createCell(0).setCellValue("Imps:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestImps).sum)
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekImps).sum)
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthImps).sum)
  row.foreach(c => c.setCellStyle(number))

  row = homeSheet.createRow(2)
  row.createCell(0).setCellValue("Clicks:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestClicks).sum)
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekClicks).sum)
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthClicks).sum)
  row.foreach(c => c.setCellStyle(number))

  row = homeSheet.createRow(3)
  row.createCell(0).setCellValue("Convs:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestConvs).sum)
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekConvs).sum)
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthConvs).sum)
  row.foreach(c => c.setCellStyle(number))

  row = homeSheet.createRow(4)
  row.createCell(0).setCellValue("Revenue:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestRevenue).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekRevenue).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthRevenue).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))


  row = homeSheet.createRow(5)
  row.createCell(0).setCellValue("Media Cost:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestMediaCost).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekMediaCost).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthMediaCost).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))


  row = homeSheet.createRow(6)
  row.createCell(0).setCellValue("AppNexus Media Cost:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestAnMediaCost).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekAnMediaCost).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthAnMediaCost).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(7)
  row.createCell(0).setCellValue("AppNexus Commission:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestAnCommission).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekAnCommission).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthAnCommission).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(8)
  row.createCell(0).setCellValue("AppNexus Imps:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestAnImps).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekAnImps).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthAnImps).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(number))

  row = homeSheet.createRow(9)
  row.createCell(0).setCellValue("AppNexus eCPM:")
  row.createCell(1).setCellFormula("B7/B9 * 1000")
  row.createCell(2).setCellFormula("C7/C9 * 1000")
  row.createCell(3).setCellFormula("D7/D9 * 1000")
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(10)
  row.createCell(0).setCellValue("MX Media Cost:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestMxMediaCost).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekMxMediaCost).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthMxMediaCost).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(11)
  row.createCell(0).setCellValue("MX Commission:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestMxCommission).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekMxCommission).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthMxCommission).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(12)
  row.createCell(0).setCellValue("MX Imps:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestMxImps).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekMxImps).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthMxImps).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(number))

  row = homeSheet.createRow(13)
  row.createCell(0).setCellValue("MX eCPM:")
  row.createCell(1).setCellFormula("B11/B13 * 1000")
  row.createCell(2).setCellFormula("C11/C13 * 1000")
  row.createCell(3).setCellFormula("D11/D13 * 1000")
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(14)
  row.createCell(0).setCellValue("AdX Media Cost:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestAdXMediaCost).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekAdXMediaCost).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthAdXMediaCost).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(15)
  row.createCell(0).setCellValue("AdX Commission:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestAdXCommission).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekAdXCommission).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthAdXCommission).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(16)
  row.createCell(0).setCellValue("AdX Imps:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestAdXImps).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekAdXImps).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthAdXImps).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(number))

  row = homeSheet.createRow(17)
  row.createCell(0).setCellValue("AdX eCPM:")
  row.createCell(1).setCellFormula("B15/B17 * 1000")
  row.createCell(2).setCellFormula("C15/C17 * 1000")
  row.createCell(3).setCellFormula("D15/D17 * 1000")
  row.foreach(c => c.setCellStyle(currency))


  val uniqueServingFeeNames = finalCamps.map(c => c.monthServingFees.map(s => s._1)).flatten.toSet
  var rowCount = 18
  uniqueServingFeeNames.foreach(n => {
    if (n.equals("BlueKai")) {
      row = homeSheet.createRow(rowCount)
      row.createCell(0).setCellValue("BlueKai Imps:")
      row.createCell(1).setCellValue(finalCamps.map(c => c.yestBlueKaiImps).sum)
      row.createCell(2).setCellValue(finalCamps.map(c => c.weekBlueKaiImps).sum)
      row.createCell(3).setCellValue(finalCamps.map(c => c.monthBlueKaiImps).sum)
      row.foreach(c => c.setCellStyle(number))
      rowCount += 1
    } else if (n.equals("Brilig")) {
      row = homeSheet.createRow(rowCount)
      row.createCell(0).setCellValue("Brilig Imps:")
      row.createCell(1).setCellValue(finalCamps.map(c => c.yestBriligImps).sum)
      row.createCell(2).setCellValue(finalCamps.map(c => c.weekBriligImps).sum)
      row.createCell(3).setCellValue(finalCamps.map(c => c.monthBriligImps).sum)
      row.foreach(c => c.setCellStyle(number))
      rowCount += 1
    } else if (n.equals("Lotame")) {
      row = homeSheet.createRow(rowCount)
      row.createCell(0).setCellValue("Lotame Imps:")
      row.createCell(1).setCellValue(finalCamps.map(c => c.yestLotameImps).sum)
      row.createCell(2).setCellValue(finalCamps.map(c => c.weekLotameImps).sum)
      row.createCell(3).setCellValue(finalCamps.map(c => c.monthLotameImps).sum)
      row.foreach(c => c.setCellStyle(number))
      rowCount += 1
    }
    row = homeSheet.createRow(rowCount)
    row.createCell(0).setCellValue(n + ":")
    row.createCell(1).setCellValue(finalCamps.map(c => c.yestServingFees.get(n).getOrElse(0.0d)).foldLeft(0.0)(_+_))
    row.createCell(2).setCellValue(finalCamps.map(c => c.weekServingFees.get(n).getOrElse(0.0d)).foldLeft(0.0)(_+_))
    row.createCell(3).setCellValue(finalCamps.map(c => c.monthServingFees.get(n).getOrElse(0.0d)).foldLeft(0.0)(_+_))
    row.foreach(c => c.setCellStyle(currency))
    rowCount += 1
  })

  row = homeSheet.createRow(rowCount)
  row.createCell(0).setCellValue("Total Cost:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestTotalCost).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekTotalCost).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthTotalCost).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))

  row = homeSheet.createRow(rowCount+1)
  row.createCell(0).setCellValue("Gross Profit:")
  row.createCell(1).setCellValue(finalCamps.map(c => c.yestGrossProfit).foldLeft(0.0)(_+_))
  row.createCell(2).setCellValue(finalCamps.map(c => c.weekGrossProfit).foldLeft(0.0)(_+_))
  row.createCell(3).setCellValue(finalCamps.map(c => c.monthGrossProfit).foldLeft(0.0)(_+_))
  row.foreach(c => c.setCellStyle(currency))

  for (x <- 0 to 3) homeSheet.autoSizeColumn(x)


  // *********** Advertiser Summary

  val adSummary = wb.createSheet("Advertisers")
  var adRow = adSummary.createRow(0)
  adRow.createCell(0).setCellValue("Advertiser")
  adRow.createCell(1).setCellValue("Imps")
  adRow.createCell(2).setCellValue("Clicks")
  adRow.createCell(3).setCellValue("Convs")
  adRow.createCell(4).setCellValue("Revenue")
  adRow.createCell(5).setCellValue("Media Cost")
  adRow.createCell(6).setCellValue("AN Media Cost")
  adRow.createCell(7).setCellValue("AN Commission")
  adRow.createCell(8).setCellValue("AN Imps")
  adRow.createCell(9).setCellValue("AN eCPM")
  adRow.createCell(10).setCellValue("MX Media Cost")
  adRow.createCell(11).setCellValue("MX Commission")
  adRow.createCell(12).setCellValue("MX Imps")
  adRow.createCell(13).setCellValue("MX eCPM")
  adRow.createCell(14).setCellValue("AdX MediaCost")
  adRow.createCell(15).setCellValue("AdX Commission")
  adRow.createCell(16).setCellValue("AdX Imps")
  adRow.createCell(17).setCellValue("AdX eCPM")
  var cellCount = 18
  uniqueServingFeeNames.foreach(n => {
    if (n.equals("BlueKai")) {
      adRow.createCell(cellCount).setCellValue("BlueKai Imps")
      cellCount += 1
    } else if (n.equals("Brilig")) {
      adRow.createCell(cellCount).setCellValue("Brilig Imps")
      cellCount += 1
    } else if (n.equals("Lotame")) {
      adRow.createCell(cellCount).setCellValue("Lotame Imps")
      cellCount += 1
    }
    adRow.createCell(cellCount).setCellValue(n)
    cellCount += 1
  })
  adRow.createCell(cellCount).setCellValue("Amazon Cost")
  adRow.createCell(cellCount + 1).setCellValue("Total Cost")
  adRow.createCell(cellCount + 2).setCellValue("Gross Profit")

  adRow.foreach(c => c.setCellStyle(bold))

  rowCount = 1

  val advertisers = finalCamps.groupBy(c => c.adName)
  advertisers.foreach(a => {
    adRow = adSummary.createRow(rowCount)
    adRow.createCell(0).setCellValue(a._1)
    adRow.createCell(1).setCellValue(a._2.map(c => c.yestImps).sum)
    adRow.getCell(1).setCellStyle(number)
    adRow.createCell(2).setCellValue(a._2.map(c => c.yestClicks).sum)
    adRow.getCell(2).setCellStyle(number)
    adRow.createCell(3).setCellValue(a._2.map(c => c.yestConvs).sum)
    adRow.getCell(3).setCellStyle(number)
    adRow.createCell(4).setCellValue(a._2.map(c => c.yestRevenue).foldLeft(0.0d)(_+_))
    adRow.getCell(4).setCellStyle(currency)
    adRow.createCell(5).setCellValue(a._2.map(c => c.yestMediaCost).foldLeft(0.0d)(_+_))
    adRow.getCell(5).setCellStyle(currency)
    adRow.createCell(6).setCellValue(a._2.map(c => c.yestAnMediaCost).foldLeft(0.0d)(_+_))
    adRow.getCell(6).setCellStyle(greenCurrency)
    adRow.createCell(7).setCellValue(a._2.map(c => c.yestAnCommission).foldLeft(0.0d)(_+_))
    adRow.getCell(7).setCellStyle(greenCurrency)
    adRow.createCell(8).setCellValue(a._2.map(c => c.yestAnImps).sum)
    adRow.getCell(8).setCellStyle(greenNumber)
    adRow.createCell(9).setCellFormula("G" + (rowCount+1) + "/I" + (rowCount+1))
    adRow.getCell(9).setCellStyle(greenCurrency)
    adRow.createCell(10).setCellValue(a._2.map(c => c.yestMxMediaCost).foldLeft(0.0d)(_+_))
    adRow.getCell(10).setCellStyle(currency)
    adRow.createCell(11).setCellValue(a._2.map(c => c.yestMxCommission).foldLeft(0.0d)(_+_))
    adRow.getCell(11).setCellStyle(currency)
    adRow.createCell(12).setCellValue(a._2.map(c => c.yestMxImps).sum)
    adRow.getCell(12).setCellStyle(number)
    adRow.createCell(13).setCellFormula("K" + (rowCount+1) + "/M" + (rowCount+1))
    adRow.getCell(13).setCellStyle(currency)
    adRow.createCell(14).setCellValue(a._2.map(c => c.yestAdXMediaCost).foldLeft(0.0d)(_+_))
    adRow.getCell(14).setCellStyle(greenCurrency)
    adRow.createCell(15).setCellValue(a._2.map(c => c.yestAdXCommission).foldLeft(0.0d)(_+_))
    adRow.getCell(15).setCellStyle(greenCurrency)
    adRow.createCell(16).setCellValue(a._2.map(c => c.yestAdXImps).sum)
    adRow.getCell(16).setCellStyle(greenNumber)
    adRow.createCell(17).setCellFormula("O" + (rowCount+1) + "/Q" + (rowCount+1))
    val allServingFees = a._2.map(c => (c.yestImps, c.servingFees))
    adSummary.getRow(0).foreach(c => {
      val name = c.getStringCellValue
      if (uniqueServingFeeNames.contains(name)) {
        // Then it is a serving fee, sum all serving fees here for advert
        adRow.createCell(c.getColumnIndex).setCellValue(0.0f)
        adRow.getCell(c.getColumnIndex).setCellStyle(currency)
        allServingFees.foreach(list => list._2.foreach(fee => {
          if(fee.getBrokerName.equals(name)) {
            val previousSum = adRow.getCell(c.getColumnIndex).getNumericCellValue
            adRow.getCell(c.getColumnIndex)
              .setCellValue(previousSum +
              (fee.getValue.toFloat / 1000)
                * list._1)
          }
        }))
        adRow.getCell(c.getColumnIndex).setCellStyle(currency)
      }
      else if(c.getStringCellValue.equals("Brilig Imps")) {
        adRow.createCell(c.getColumnIndex).setCellValue(a._2.map(_.yestBriligImps).sum)
        adRow.getCell(c.getColumnIndex).setCellStyle(number)
      }
      else if(c.getStringCellValue.equals("BlueKai Imps")) {
        adRow.createCell(c.getColumnIndex).setCellValue(a._2.map(_.yestBlueKaiImps).sum)
        adRow.getCell(c.getColumnIndex).setCellStyle(number)
      }
      else if(c.getStringCellValue.equals("Lotame Imps")) {
        adRow.createCell(c.getColumnIndex).setCellValue(a._2.map(_.yestLotameImps).sum)
        adRow.getCell(c.getColumnIndex).setCellStyle(number)
      }
    })
    adRow.createCell(cellCount).setCellValue(a._2.map(c => c.yestAmazonCost).foldLeft(0.0d)(_+_))
    adRow.getCell(cellCount).setCellStyle(currency)
    adRow.createCell(cellCount + 1).setCellValue(a._2.map(c => c.yestTotalCost).foldLeft(0.0d)(_+_))
    adRow.getCell(cellCount + 1).setCellStyle(currency)
    adRow.createCell(cellCount + 2).setCellValue(a._2.map(c => c.yestGrossProfit).foldLeft(0.0d)(_+_))
    adRow.getCell(cellCount + 2).setCellStyle(currency)
    rowCount += 1

    // ********* Create sheet for advertiser that lists each campaign
    val campSheet = wb.createSheet(a._1)
    var campRow = campSheet.createRow(0)
    campRow.createCell(0).setCellValue("Campaign")
    campRow.createCell(1).setCellValue("Imps")
    campRow.createCell(2).setCellValue("Clicks")
    campRow.createCell(3).setCellValue("Convs")
    campRow.createCell(4).setCellValue("Revenue")
    campRow.createCell(5).setCellValue("Media Cost")
    campRow.createCell(6).setCellValue("AN Media Cost")
    campRow.createCell(7).setCellValue("AN Commission")
    campRow.createCell(8).setCellValue("AN Imps")
    campRow.createCell(9).setCellValue("AN eCPM")
    campRow.createCell(10).setCellValue("MX Media Cost")
    campRow.createCell(11).setCellValue("MX Commission")
    campRow.createCell(12).setCellValue("MX Imps")
    campRow.createCell(13).setCellValue("MX eCPM")
    campRow.createCell(14).setCellValue("AdX MediaCost")
    campRow.createCell(15).setCellValue("AdX Commission")
    campRow.createCell(16).setCellValue("AdX Imps")
    campRow.createCell(17).setCellValue("AdX eCPM")
    cellCount = 18
    uniqueServingFeeNames.foreach(n => {
      if (n.equals("BlueKai")) {
        campRow.createCell(cellCount).setCellValue("BlueKai Imps")
        cellCount += 1
      } else if (n.equals("Brilig")) {
        campRow.createCell(cellCount).setCellValue("Brilig Imps")
        cellCount += 1
      } else if (n.equals("Lotame")) {
        campRow.createCell(cellCount).setCellValue("Lotame Imps")
        cellCount += 1
      }
      campRow.createCell(cellCount).setCellValue(n)
      cellCount += 1
    })
    campRow.createCell(cellCount).setCellValue("Amazon Cost")
    campRow.createCell(cellCount + 1).setCellValue("Total Cost")
    campRow.createCell(cellCount + 2).setCellValue("Gross Profit")
    campRow.createCell(cellCount + 3).setCellValue("GP Margin")

    campRow.foreach(c => c.setCellStyle(bold))

    var campRowCount = 1
    a._2.filterNot(c => c.yestImps == 0 && c.yestClicks == 0 && c.yestConvs == 0).foreach(c => {
      campRow = campSheet.createRow(campRowCount)
      campRow.createCell(0).setCellValue(c.campName + " (" + c.campId + ")")
      campRow.createCell(1).setCellValue(c.yestImps)
      campRow.getCell(1).setCellStyle(number)
      campRow.createCell(2).setCellValue(c.yestClicks)
      campRow.getCell(2).setCellStyle(number)
      campRow.createCell(3).setCellValue(c.yestConvs)
      campRow.getCell(3).setCellStyle(number)
      campRow.createCell(4).setCellValue(c.yestRevenue)
      campRow.getCell(4).setCellStyle(currency)
      campRow.createCell(5).setCellValue(c.yestMediaCost)
      campRow.getCell(5).setCellStyle(currency)
      campRow.createCell(6).setCellValue(c.yestAnMediaCost)
      campRow.getCell(6).setCellStyle(greenCurrency)
      campRow.createCell(7).setCellValue(c.yestAnCommission)
      campRow.getCell(7).setCellStyle(greenCurrency)
      campRow.createCell(8).setCellValue(c.yestAnImps)
      campRow.getCell(8).setCellStyle(greenNumber)
      campRow.createCell(9).setCellFormula("G" + (campRowCount+1) + "/I" + (campRowCount+1))
      campRow.getCell(9).setCellStyle(greenCurrency)
      campRow.createCell(10).setCellValue(c.yestMxMediaCost)
      campRow.getCell(10).setCellStyle(currency)
      campRow.createCell(11).setCellValue(c.yestMxCommission)
      campRow.getCell(11).setCellStyle(currency)
      campRow.createCell(12).setCellValue(c.yestMxImps)
      campRow.getCell(12).setCellStyle(number)
      campRow.createCell(13).setCellFormula("K" + (campRowCount+1) + "/M" + (campRowCount+1))
      campRow.getCell(13).setCellStyle(currency)
      campRow.createCell(14).setCellValue(c.yestAdXMediaCost)
      campRow.getCell(14).setCellStyle(greenCurrency)
      campRow.createCell(15).setCellValue(c.yestAdXCommission)
      campRow.getCell(15).setCellStyle(greenCurrency)
      campRow.createCell(16).setCellValue(c.yestAdXImps)
      campRow.getCell(16).setCellStyle(greenNumber)
      campRow.createCell(17).setCellFormula("O" + (campRowCount+1) + "/Q" + (campRowCount+1))
      campRow.getCell(17).setCellStyle(greenCurrency)
      campSheet.getRow(0).foreach(col => {
        val name = col.getStringCellValue
        if(c.servingFees.map(_.getBrokerName).contains(name)) {
          val cost = c.servingFees.map(f => (f.getBrokerName, f.getValue.toFloat/1000*c.yestImps)).toMap.get(name).getOrElse(-1f)
          campRow.createCell(col.getColumnIndex).setCellValue(cost)
          campRow.getCell(col.getColumnIndex).setCellStyle(currency)
        }
        else if(col.getStringCellValue.equals("Brilig Imps")) {
          campRow.createCell(col.getColumnIndex).setCellValue(c.yestBriligImps)
          campRow.getCell(col.getColumnIndex).setCellStyle(number)
        }
        else if(col.getStringCellValue.equals("BlueKai Imps")) {
          campRow.createCell(col.getColumnIndex).setCellValue(c.yestBlueKaiImps)
          campRow.getCell(col.getColumnIndex).setCellStyle(number)
        }
        else if(col.getStringCellValue.equals("Lotame Imps")) {
          campRow.createCell(col.getColumnIndex).setCellValue(c.yestLotameImps)
          campRow.getCell(col.getColumnIndex).setCellStyle(number)
        }
      })
      campRow.createCell(cellCount).setCellValue(c.yestAmazonCost)
      campRow.getCell(cellCount).setCellStyle(currency)
      campRow.createCell(cellCount + 1).setCellValue(c.yestTotalCost)
      campRow.getCell(cellCount + 1).setCellStyle(currency)
      campRow.createCell(cellCount + 2).setCellValue(c.yestGrossProfit)
      campRow.getCell(cellCount + 2).setCellStyle(currency)
      campRowCount += 1
    })
    for(x <- 0 to cellCount + 3) campSheet.autoSizeColumn(x)
  })
  for(x <- 0 to cellCount + 3) adSummary.autoSizeColumn(x)

  val fileOut: FileOutputStream = new FileOutputStream(new File(props.getOutputPath, "Test_PnL.xls"))
  wb.write(fileOut)
  fileOut.close()

}

// ********** Analytics Row

case class AnalyticsDataRow(campaignId: String,
                             campaignName: String,
                             lineId: String,
                             lineName: String,
                             adName: String,
                             adId: String,
                             campType: String,
                             imps: Int,
                             clicks: Int,
                             convsRaw: Int,
                             mediaCost: Float,
                             networkRevenueRaw: Float,
                             sellerName: String,
                             day: DateTime) {
  // Removes Test Conversions
  val convs = if (lineName.contains("TEST")) 0 else convsRaw
  val networkRevenue = if(adName.equals("MediaCrossing Inc")) mediaCost + (.05 * (imps/1000)) else networkRevenueRaw

}

// *********** Campaign

case class Campaign(dataRows: List[AnalyticsDataRow],
                     servingFees: List[ServingFee]) {
  val campName = dataRows.head.campaignName
  val campId = dataRows.head.campaignId
  val adName = dataRows.head.adName
  // FIXME CHANGE BACK
  private val today = new DateTime().withTimeAtStartOfDay().withYear(2014).withMonthOfYear(7).withDayOfMonth(11)

  private val yestRows = dataRows.filter(r => r.day.isEqual(today.minusDays(1)))
  private val yestMxRows = yestRows.filter(r => r.sellerName.equals("MediaCrossing Inc."))
  private val yestAdXRows = yestRows.filter(r => r.sellerName.equals("Google AdExchange"))
  private val yestAnRows = yestRows.filter(r =>
    !r.sellerName.equals("MediaCrossing Inc.") && !r.sellerName.equals("Google AdExchange"))

  val yestImps = yestRows.foldLeft(0)(_ + _.imps)
  val yestClicks = yestRows.foldLeft(0)(_ + _.clicks)
  val yestConvs = yestRows.foldLeft(0)(_ + _.convs)
  val yestRevenue = yestRows.foldLeft(0.0)(_ + _.networkRevenue)
  val yestMediaCost = yestRows.foldLeft(0.0)(_ + _.mediaCost)
  val yestAnMediaCost = yestAnRows.foldLeft(0.0)(_ + _.mediaCost)
  val yestAnCommission = .115f * yestAnMediaCost
  val yestAnImps = yestAnRows.foldLeft(0)(_ + _.imps)
  val yestAnCpm = (yestAnMediaCost / yestAnImps) * 1000
  val yestMxMediaCost = yestMxRows.foldLeft(0.0)(_ + _.mediaCost)
  val yestMxCommission = .115f * yestMxMediaCost
  val yestMxImps = yestMxRows.foldLeft(0)(_ + _.imps)
  val yestMxCpm = (yestMxImps / 1000f) * .017f
  val yestAdXMediaCost = yestAdXRows.foldLeft(0.0)(_ + _.mediaCost)
  val yestAdXCommission = .115f * yestAdXMediaCost
  val yestAdXImps = yestAdXRows.foldLeft(0)(_ + _.imps)
  val yestAdXCpm = .1f * yestAdXMediaCost

  val yestServingFees = servingFees
    .map(f =>
    (f.getBrokerName, {
      if (f.getBrokerName.equals("Peer39")) .15 * yestMediaCost
      else (f.getValue.toFloat / 1000) * yestImps
    })
    )
    .toMap

  val yestBlueKaiImps = {
    if (yestServingFees.contains("BlueKai")) yestImps
    else 0
  }
  val yestBriligImps = {
    if (yestServingFees.contains("Brilig")) yestImps
    else 0
  }
  val yestLotameImps = {
    if (yestServingFees.contains("Lotame")) yestImps
    else 0
  }
  val yestAmazonCost = yestImps * (.095f / 1000)
  // TODO Yesterday Total Cost
  val yestTotalCost = {
    val campType = dataRows.head.campType
    if (campType.equals("cross")) {
      //val crossRev = .4 * (yestRevenue - (yestRevenue * .135 - .1))

      0
    }
    else if (campType.equals("direct")) {
      // TODO
      0
    }
    else {
      yestMediaCost +
        yestServingFees.map(f => f._2).foldLeft(0.0)(_ + _) +
        yestAnCommission +
        yestMxCommission
    }
  }

  // TODO
  val yestGrossProfit = yestRevenue - yestTotalCost

  //TODO calculate daily PnL for each stream
  /*
    If campType.equals direct or crossed, do the calcs, else, it's normal
   */


  private val weekRows = dataRows.filter(r => r.day.isAfter(today.minusDays(8)))
  private val weekMxRows = weekRows.filter(r => r.sellerName.equals("MediaCrossing Inc."))
  private val weekAdXRows = weekRows.filter(r => r.sellerName.equals("Google AdExchange"))
  private val weekAnRows = weekRows.filter(r =>
    !r.sellerName.equals("MediaCrossing Inc.") && !r.sellerName.equals("Google AdExchange"))

  val weekImps = weekRows.foldLeft(0)(_ + _.imps)
  val weekClicks = weekRows.foldLeft(0)(_ + _.clicks)
  val weekConvs = weekRows.foldLeft(0)(_ + _.convs)
  val weekRevenue = weekRows.foldLeft(0.0)(_ + _.networkRevenue)
  val weekMediaCost = weekRows.foldLeft(0.0)(_ + _.mediaCost)
  val weekAnMediaCost = weekAnRows.foldLeft(0.0)(_ + _.mediaCost)
  val weekAnCommission = .115f * weekAnMediaCost
  val weekAnImps = weekAnRows.foldLeft(0)(_ + _.imps)
  val weekAnCpm = (weekAnMediaCost / weekAnImps) * 1000
  val weekMxMediaCost = weekMxRows.foldLeft(0.0)(_ + _.mediaCost)
  val weekMxCommission = .115f * weekMxMediaCost
  val weekMxImps = weekMxRows.foldLeft(0)(_ + _.imps)
  val weekMxCpm = (weekMxImps / 1000f) * .017f
  val weekAdXMediaCost = weekAdXRows.foldLeft(0.0)(_ + _.mediaCost)
  val weekAdXCommission = .115f * weekAdXMediaCost
  val weekAdXImps = weekAdXRows.foldLeft(0)(_ + _.imps)
  val weekAdXCpm = .1f * weekAdXMediaCost

  val weekServingFees = servingFees
    .map(f =>
    (f.getBrokerName, {
      if (f.getBrokerName.equals("Peer39")) .15 * weekMediaCost
      else (f.getValue.toFloat / 1000) * weekImps
    })
    )
    .toMap

  val weekBlueKaiImps = {
    if (weekServingFees.contains("BlueKai")) weekImps
    else 0
  }
  val weekBriligImps = {
    if (weekServingFees.contains("Brilig")) weekImps
    else 0
  }
  val weekLotameImps = {
    if (weekServingFees.contains("Lotame")) weekImps
    else 0
  }

  val weekAmazonCost = weekImps * (.095f / 1000)
  // TODO WEEK TOTAL COST
  val weekTotalCost = {
    val campType = dataRows.head.campType
    if (campType.equals("cross")) {
      // TODO
      0
    }
    else if (campType.equals("direct")) {
      // TODO
      0
    }
    else {
      weekMediaCost +
        weekServingFees.map(f => f._2).foldLeft(0.0)(_ + _) +
        weekAnCommission +
        weekMxCommission
    }
  }

  // TODO Week PnL for each Stream
  val weekGrossProfit = weekRevenue - weekTotalCost


  private val monthRows = dataRows
  private val monthMxRows = monthRows.filter(r => r.sellerName.equals("MediaCrossing Inc."))
  private val monthAdXRows = monthRows.filter(r => r.sellerName.equals("Google AdExchange"))
  private val monthAnRows = monthRows.filter(r =>
    !r.sellerName.equals("MediaCrossing Inc.") && !r.sellerName.equals("Google AdExchange"))

  val monthImps = monthRows.foldLeft(0)(_ + _.imps)
  val monthClicks = monthRows.foldLeft(0)(_ + _.clicks)
  val monthConvs = monthRows.foldLeft(0)(_ + _.convs)
  val monthRevenue = monthRows.foldLeft(0.0)(_ + _.networkRevenue)
  val monthMediaCost = monthRows.foldLeft(0.0)(_ + _.mediaCost)
  val monthAnMediaCost = monthAnRows.foldLeft(0.0)(_ + _.mediaCost)
  val monthAnCommission = .115f * monthAnMediaCost
  val monthAnImps = monthAnRows.foldLeft(0)(_ + _.imps)
  val monthAnCpm = (monthAnMediaCost / monthAnImps) * 1000
  val monthMxMediaCost = monthMxRows.foldLeft(0.0)(_ + _.mediaCost)
  val monthMxCommission = .115f * monthMxMediaCost
  val monthMxImps = monthMxRows.foldLeft(0)(_ + _.imps)
  val monthMxCpm = (monthMxImps / 1000f) * .017f
  val monthAdXMediaCost = monthAdXRows.foldLeft(0.0)(_ + _.mediaCost)
  val monthAdXCommission = .115f * monthAdXMediaCost
  val monthAdXImps = monthAdXRows.foldLeft(0)(_ + _.imps)
  val monthAdXCpm = .1f * monthAdXMediaCost

  val monthServingFees = servingFees
    .map(f =>
    (f.getBrokerName, {
      if (f.getBrokerName.equals("Peer39")) .15 * monthMediaCost
      else (f.getValue.toFloat / 1000) * monthImps
    })
    )
    .toMap

  val monthBlueKaiImps = {
    if (monthServingFees.contains("BlueKai")) monthImps
    else 0
  }
  val monthBriligImps = {
    if (monthServingFees.contains("Brilig")) monthImps
    else 0
  }
  val monthLotameImps = {
    if (monthServingFees.contains("Lotame")) monthImps
    else 0
  }
  val monthAmazonCost = monthImps * (.095f / 1000)

  // TODO Total Cost
  val monthTotalCost = {
    val campType = dataRows.head.campType
    if (campType.equals("cross")) {
      // TODO
      0
    }
    else if (campType.equals("direct")) {
      // TODO
      0
    }
    else {
      monthMediaCost +
        monthServingFees.map(f => f._2).foldLeft(0.0)(_ + _) +
        monthAnCommission +
        monthMxCommission
    }
  }

  // TODO Month PnL for each Stream
  val monthGrossProfit = monthRevenue - monthTotalCost
  
}
