package com.mediacrossing.briligsegmentfilter

import java.io.{File, FileOutputStream, FileReader, BufferedReader}
import au.com.bytecode.opencsv.CSVReader
import scala.collection.JavaConversions._
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.AppNexusService
import org.apache.poi.hssf.usermodel.HSSFWorkbook

object RunBriligSegmentFilter extends App{


  val props = new ConfigurationProperties(args)
  val anConn = new AppNexusService(
    props.getAppNexusUrl,
    props.getAppNexusUsername,
    props.getAppNexusPassword,
    20,
    props.getRequestDelayInSeconds)

  //read in brilig csv
  val reader: CSVReader = new CSVReader(new BufferedReader(new FileReader
  ("/Users/charronkyle/Desktop/Brili_MediaCrossing Taxonomy_updated_1_2_14_low_zero_imp_LI's_removed.csv")))
  val csvData = reader.readAll.toList.tail

  val wb = new HSSFWorkbook()
  val sheet = wb.createSheet("Not Found")
  val found = wb.createSheet("Found")
  val header = sheet.createRow(0)
  header.createCell(0).setCellValue("Data Provider")
  header.createCell(1).setCellValue("Segment ID")
  header.createCell(2).setCellValue("Name")
  header.createCell(3).setCellValue("Inventory")
  header.createCell(4).setCellValue("Reach")
  header.createCell(5).setCellValue("Taxonomy")
  header.createCell(6).setCellValue("Description")
  val fHeader = found.createRow(0)
  fHeader.createCell(0).setCellValue("Data Provider")
  fHeader.createCell(1).setCellValue("Segment ID")
  fHeader.createCell(2).setCellValue("Name")
  fHeader.createCell(3).setCellValue("Inventory")
  fHeader.createCell(4).setCellValue("Reach")
  fHeader.createCell(5).setCellValue("Taxonomy")
  fHeader.createCell(6).setCellValue("Description")
  var count = 1
  var fCount = 1
  //for each line, query AN
  csvData.foreach(line => {
    if(!anConn.checkForSegment(line(1))){
      val d = sheet.createRow(count)
      d.createCell(0).setCellValue(line(0))
      d.createCell(1).setCellValue(line(1))
      d.createCell(2).setCellValue(line(2))
      d.createCell(3).setCellValue(line(3))
      d.createCell(4).setCellValue(line(4))
      d.createCell(5).setCellValue(line(5))
      d.createCell(6).setCellValue(line(6))
      count+=1
    } else {
      val r = found.createRow(fCount)
      r.createCell(0).setCellValue(line(0))
      r.createCell(1).setCellValue(line(1))
      r.createCell(2).setCellValue(line(2))
      r.createCell(3).setCellValue(line(3))
      r.createCell(4).setCellValue(line(4))
      r.createCell(5).setCellValue(line(5))
      r.createCell(6).setCellValue(line(6))
      fCount+=1
    }
  })

  //export wb
  val fileOut = new FileOutputStream(new File(props.getOutputPath, "SegmentsNotFound.xls"))
  wb.write(fileOut)
  fileOut.close
}
