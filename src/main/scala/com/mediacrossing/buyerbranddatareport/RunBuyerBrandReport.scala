package com.mediacrossing.buyerbranddatareport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import play.api.libs._
import json._
import Reads._
import functional.syntax._
import scala.collection.JavaConversions._
import org.joda.time.{DateTimeZone, LocalDate, DateTime}
import org.joda.time.format.DateTimeFormat
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io._
import scala.collection.mutable._
import scala.collection.mutable
import org.apache.poi.ss.util.WorkbookUtil
import scala.Serializable

object RunBuyerBrandReport extends App{

  //logging
  val LOG = LoggerFactory.getLogger(RunBuyerBrandReport.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  //init variables
  val p = new ConfigurationProperties(args)
  val anConn = new AppNexusService(
    p.getAppNexusUrl,
    p.getAppNexusUsername,
    p.getAppNexusPassword,
    p.getPartitionSize,
    p.getRequestDelayInSeconds)
  val mxConn = {
    if (p.getMxUsername == null) new MxService(p.getMxUrl)
    else new MxService(p.getMxUrl, p.getMxUsername, p.getMxPassword)
  }

  //for faster debugging
  val development = false
  if (development) {
    try{
      def ReadObjectFromFile[A](filename: String)(implicit m:scala.reflect.Manifest[A]) = {
        val input = new ObjectInputStream(new FileInputStream(filename))
        val obj = input.readObject()
        obj match {
          case x if m.runtimeClass.isInstance(x) => x.asInstanceOf[A]
          case _ => sys.error("Type not what was expected.")
        }
      }
      val pubs = ReadObjectFromFile[List[Publisher]]("/Users/charronkyle/Desktop/ReportData/BrandBuyerPubs.ser")
      val today = new LocalDate(DateTimeZone.UTC)
      val pubBooks = for{p <- pubs} yield new PubWithWorkbooks(p.id, p.name, new BrandBuyerReportWriter(p).writeReports)
      pubBooks.foreach(pub => {
        var fileOut = new FileOutputStream(new File(p.getOutputPath, pub.name + "_Brand_Report"+ today.toString + ".xls"))
        pub.workbooks._1.write(fileOut)
        fileOut = new FileOutputStream(new File(p.getOutputPath, pub.name + "_Buyer_Report"+ today.toString + ".xls"))
        pub.workbooks._2.write(fileOut)
        fileOut.close()
      })
      System.exit(0)
    } catch {
      case ioe: IOException => ioe.printStackTrace()
      System.exit(1)
    }
  }

  //parses json
  val pubR = (
    (__ \ "id").read[String] ~
      (__ \ "name").read[String] ~
      (__ \ "status").read[String] ~
      (__ \ "siteIds").read(list[String]) ~
      (__ \ "placementIds").read(list[String])
    ).apply(PubJson.apply _)

  //list of active publishers 
  val pubList = Json.parse(mxConn.requestAllPublisherJson).validate(list(pubR)).get.filter(p => p.status == "active")
  
  //date format 
  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

  //create a list of publishers
  val pubs = for {p <- pubList} yield {
    //request rows
    val brandRows = for { line <- anConn.requestBrandReport(p.id).toList.tail
    } yield DataRow(dateFormat.parseDateTime(line(0)), line(1), line(2), line(3).toInt,
        line(4).toInt, line(5).toDouble, line(6).toDouble, line(7), line(8))
    val buyerRows = for { line <- anConn.requestBuyerReport(p.id).toList.tail
    } yield DataRow(dateFormat.parseDateTime(line(0)), line(1), line(2), line(3).toInt,
        line(4).toInt, line(5).toDouble, line(6).toDouble, line(7), line(8))

    new Publisher(p.id, p.name, {
      val placements = mutable.Set[(String, String)]()
      brandRows.foreach(r =>placements.add(r.placementId, r.placementName))
      buyerRows.foreach(r =>placements.add(r.placementId, r.placementName))
      for {place <- placements} yield new Placement(place._1, place._2, {
        val brands = mutable.Set[(String, String)]()
        brandRows.foreach(b => brands.add(b.id, b.name))
        for {b <- brands} yield new BuyerBrand(b._1, b._2,
          brandRows.filter(d => d.placementId == place._1 && d.id == b._1))}, {
        val buyers = mutable.Set[(String, String)]()
        buyerRows.foreach(b => buyers.add(b.id, b.name))
        for {b <- buyers} yield new BuyerBrand(b._1, b._2,
          buyerRows.filter(d => d.placementId == place._1 && d.id == b._1))})
    }, {
        for {line <- anConn.requestPlacementReport(p.id).toList.tail} yield TotalRow(dateFormat.parseDateTime(line(0)),
          line(1).toInt, line(2).toInt, line(3).toDouble, line(4).toDouble, line(5), line(6))
    })
  }

  //save pub lists to hdd
  /*try {
    val out = new ObjectOutputStream(new FileOutputStream("/Users/charronkyle/Desktop/ReportData/BrandBuyerPubs.ser"))
    out.writeObject(pubs)
    out.close()
  } catch {
    case ioe: IOException => LOG.error("Serialization Failed!")
  }*/
  val today = new LocalDate(DateTimeZone.UTC)
  val pubBooks = for{p <- pubs} yield new PubWithWorkbooks(p.id, p.name, new BrandBuyerReportWriter(p).writeReports)
  pubBooks.foreach(pub => {
      var fileOut = new FileOutputStream(new File(p.getOutputPath, pub.name + "_Brand_Report"+ today.toString + ".xls"))
      pub.workbooks._1.write(fileOut)
      fileOut = new FileOutputStream(new File(p.getOutputPath, pub.name + "_Buyer_Report"+ today.toString + ".xls"))
      pub.workbooks._2.write(fileOut)
      fileOut.close()
  })

}
case class PubJson(id: String, name: String, status: String, siteIds: List[String], placementIds: List[String])
case class DataRow (day: DateTime, name: String, id: String, kept: Int, resold: Int, revenue: Double, rpm: Double,
                       placementId: String, placementName: String) extends Serializable
case class TotalRow (day: DateTime, kept: Int, resold: Int, revenue: Double,
                     rpm: Double, placementId: String, placementName: String) extends Serializable
case class Publisher(id: String, name: String, placements: Set[Placement], totals: List[TotalRow]) extends Serializable
case class Placement(id: String, name: String, buyers: Set[BuyerBrand], brands: Set[BuyerBrand]) extends Serializable
case class BuyerBrand(id:String, name: String, dataRows: List[DataRow]) extends Serializable
case class PubWithWorkbooks(id: String, name: String, workbooks: (HSSFWorkbook, HSSFWorkbook))

class BrandBuyerReportWriter(p: Publisher) {
  def writeReports = (writeBrandReport, writeBuyerReport)
  def writeBrandReport = {
    //create wb for every pub
      //brand wb
      val wb = new HSSFWorkbook()
      val sumSheet = wb.createSheet("Summary")
      val headRow = sumSheet.createRow(0)
      headRow.createCell(0).setCellValue("Placement")
      headRow.createCell(1).setCellValue("Metric")
      var dateCount = 1
      var cellCount = 2
      val date = DateTime.now()
      while(dateCount <= 7) {
        headRow.createCell(cellCount).setCellValue(date.minusDays(dateCount).getMonthOfYear + "/"
          + date.minusDays(dateCount).getDayOfMonth)
        dateCount += 1
        cellCount += 1
      }
      var rows = 1
      for {place <- p.placements} yield {
        val keptRow = sumSheet.createRow(rows)
        keptRow.createCell(0).setCellValue(place.name)
        keptRow.createCell(1).setCellValue("Kept")
        val resoldRow = sumSheet.createRow(rows+1)
        resoldRow.createCell(0).setCellValue(place.name)
        resoldRow.createCell(1).setCellValue("Resold")
        val revRow = sumSheet.createRow(rows+2)
        revRow.createCell(0).setCellValue(place.name)
        revRow.createCell(1).setCellValue("Revenue")
        val rpmRow = sumSheet.createRow(rows+3)
        rpmRow.createCell(0).setCellValue(place.name)
        rpmRow.createCell(1).setCellValue("RPM")
        cellCount = 2
        dateCount = 1
        while(dateCount <= 7) {
          keptRow.createCell(cellCount).setCellValue(0)
          resoldRow.createCell(cellCount).setCellValue(0)
          revRow.createCell(cellCount).setCellValue(0)
          rpmRow.createCell(cellCount).setCellValue(0)
            p.totals.foreach(t => {
              if (t.placementId == place.id && date.minusDays(dateCount).getDayOfMonth == t.day.getDayOfMonth) {
                keptRow.createCell(cellCount).setCellValue(t.kept)
                resoldRow.createCell(cellCount).setCellValue(t.resold)
                revRow.createCell(cellCount).setCellValue(t.revenue)
                rpmRow.createCell(cellCount).setCellValue(t.rpm)
              }
            })
          cellCount+=1
          dateCount+=1
        }
        rows += 4
      }
      //call placementSheets
      placementSheets()

      //create sheet for every placement
      def placementSheets() = for {place <- p.placements} yield {
        val placeSheet = wb.createSheet(WorkbookUtil.createSafeSheetName("("+place.id+")"+place.name))
        val headerRow = placeSheet.createRow(0)
        headerRow.createCell(0).setCellValue("Brands")
        headerRow.createCell(1).setCellValue("Metric")
        dateCount = 1
        cellCount = 2
        while(dateCount <= 7) {
          headerRow.createCell(cellCount).setCellValue(date.minusDays(dateCount).getMonthOfYear + "/"
            + date.minusDays(dateCount).getDayOfMonth)
          dateCount += 1
          cellCount += 1
        }

        var rowCount = 0

        //for each brand/buyer
        place.brands.foreach(b => {

          //kept row
          rowCount += 1
          val keptRow = placeSheet.createRow(rowCount)
          keptRow.createCell(0).setCellValue(b.name)
          keptRow.createCell(1).setCellValue("Kept")
          cellCount = 2
          dateCount = 1
          while(dateCount <= 7) {
            keptRow.createCell(cellCount).setCellValue(0)
            b.dataRows.foreach(d => {
              if (date.minusDays(dateCount).getDayOfMonth == d.day.getDayOfMonth) {
                keptRow.createCell(cellCount).setCellValue(d.kept)
              }
            })
            cellCount += 1
            dateCount += 1
          }
          rowCount += 1
          //resold row
          val resoldRow = placeSheet.createRow(rowCount)
          resoldRow.createCell(0).setCellValue(b.name)
          resoldRow.createCell(1).setCellValue("Resold")
          cellCount = 2
          dateCount = 1
          while(dateCount <= 7) {
            resoldRow.createCell(cellCount).setCellValue(0)
            b.dataRows.foreach(d => {
              if (date.minusDays(dateCount).getDayOfMonth == d.day.getDayOfMonth) {
                resoldRow.createCell(cellCount).setCellValue(d.resold)
              }
            })
            cellCount += 1
            dateCount += 1
          }
          rowCount +=1
          //rev row
          val revRow = placeSheet.createRow(rowCount)
          revRow.createCell(0).setCellValue(b.name)
          revRow.createCell(1).setCellValue("Revenue")
          cellCount = 2
          dateCount = 1
          while(dateCount <= 7) {
            revRow.createCell(cellCount).setCellValue(0)
            b.dataRows.foreach(d => {
              if (date.minusDays(dateCount).getDayOfMonth == d.day.getDayOfMonth) {
                revRow.createCell(cellCount).setCellValue(d.revenue)
              }
            })
            cellCount += 1
            dateCount += 1
          }
          rowCount +=1
          //rpm row
          val rpmRow = placeSheet.createRow(rowCount)
          rpmRow.createCell(0).setCellValue(b.name)
          rpmRow.createCell(1).setCellValue("RPM")
          cellCount = 2
          dateCount = 1
          while(dateCount <= 7) {
            rpmRow.createCell(cellCount).setCellValue(0)
            b.dataRows.foreach(d => {
              if (date.minusDays(dateCount).getDayOfMonth == d.day.getDayOfMonth) {
                rpmRow.createCell(cellCount).setCellValue(d.rpm)
              }
            })
            cellCount += 1
            dateCount += 1
          }
        })
      }
      wb
  }
  def writeBuyerReport = {
    //create wb for every pub
    //brand wb
    val wb = new HSSFWorkbook()
    val sumSheet = wb.createSheet("Summary")
    val headRow = sumSheet.createRow(0)
    headRow.createCell(0).setCellValue("Placement")
    headRow.createCell(1).setCellValue("Metric")
    var dateCount = 1
    var cellCount = 2
    val date = DateTime.now()
    while(dateCount <= 7) {
      headRow.createCell(cellCount).setCellValue(date.minusDays(dateCount).getMonthOfYear + "/"
        + date.minusDays(dateCount).getDayOfMonth)
      dateCount += 1
      cellCount += 1
    }
    var rows = 1
    for {place <- p.placements} yield {
      val keptRow = sumSheet.createRow(rows)
      keptRow.createCell(0).setCellValue(place.name)
      keptRow.createCell(1).setCellValue("Kept")
      cellCount = 2
      dateCount = 1
    }

    //call placementSheets
    placementSheets()

    //create sheet for every placement
    def placementSheets() = for {place <- p.placements} yield {
      val placeSheet = wb.createSheet(WorkbookUtil.createSafeSheetName("("+place.id+")"+place.name))
      val headerRow = placeSheet.createRow(0)
      headerRow.createCell(0).setCellValue("Buyer")
      headerRow.createCell(1).setCellValue("Metric")
      dateCount = 1
      cellCount = 2
      while(dateCount <= 7) {
        headerRow.createCell(cellCount).setCellValue(date.minusDays(dateCount).getMonthOfYear + "/"
          + date.minusDays(dateCount).getDayOfMonth)
        dateCount += 1
        cellCount += 1
      }

      var rowCount = 0

      //for each brand/buyer
      place.buyers.foreach(b => { b.dataRows.foreach(d => {

        //kept row
        rowCount += 1
        val keptRow = placeSheet.createRow(rowCount)
        keptRow.createCell(0).setCellValue(d.name)
        keptRow.createCell(1).setCellValue("Kept")
        cellCount = 2
        dateCount = 1
        while(dateCount <= 7) {
          if (date.minusDays(dateCount).dayOfMonth() == d.day.dayOfMonth) {
            keptRow.createCell(cellCount).setCellValue(d.kept)
          } else keptRow.createCell(cellCount).setCellValue(0)
          cellCount += 1
          dateCount += 1
        }
        rowCount += 1
        //resold row
        val resoldRow = placeSheet.createRow(rowCount)
        resoldRow.createCell(0).setCellValue(d.name)
        resoldRow.createCell(1).setCellValue("Resold")
        cellCount = 2
        dateCount = 1
        while(dateCount <= 7) {
          if (date.minusDays(dateCount).dayOfMonth() == d.day.dayOfMonth) {
            resoldRow.createCell(cellCount).setCellValue(d.resold)
          } else resoldRow.createCell(cellCount).setCellValue(0)
          cellCount += 1
          dateCount += 1
        }
        rowCount +=1
        //rev row
        val revRow = placeSheet.createRow(rowCount)
        revRow.createCell(0).setCellValue(d.name)
        revRow.createCell(1).setCellValue("Revenue")
        cellCount = 2
        dateCount = 1
        while(dateCount <= 7) {
          if (date.minusDays(dateCount).dayOfMonth() == d.day.dayOfMonth) {
            revRow.createCell(cellCount).setCellValue(d.revenue)
          } else revRow.createCell(cellCount).setCellValue(0)
          cellCount += 1
          dateCount += 1
        }
        rowCount +=1
        //rpm row
        val rpmRow = placeSheet.createRow(rowCount)
        rpmRow.createCell(0).setCellValue(d.name)
        rpmRow.createCell(1).setCellValue("RPM")
        cellCount = 2
        dateCount = 1
        while(dateCount <= 7) {
          if (date.minusDays(dateCount).dayOfMonth() == d.day.dayOfMonth) {
            rpmRow.createCell(cellCount).setCellValue(d.rpm)
          } else rpmRow.createCell(cellCount).setCellValue(0)
          cellCount += 1
          dateCount += 1
        }
      })
      })
    }
    wb
  }
}
