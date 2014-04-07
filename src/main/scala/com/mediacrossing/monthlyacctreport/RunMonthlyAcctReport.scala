package com.mediacrossing.monthlyacctreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{AppNexusService, MxService}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import play.api.libs.json.Reads._
import org.joda.time.{DateTimeZone, LocalDate, DateTime}
import scala.collection.JavaConversions._
import com.mediacrossing.activelineitemreport.LineItem
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.{File, FileOutputStream}
import org.apache.poi.ss.usermodel.{CellStyle, IndexedColors, DataFormatter}

object RunMonthlyAcctReport extends App {

  /** Get all line-items active and ending within the past month from MX.
    * Name
    * Start Date
    * End Date
    * Imp Count from each month running
  **/

  val LOG = LoggerFactory.getLogger(RunMonthlyAcctReport.getClass)
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
  val anConn = new AppNexusService(props.getAppNexusUrl, props.getAppNexusUsername, props.getAppNexusPassword)

  val lineR = (
    (__ \ "id").read[String] ~
      (__ \ "name").read[String] ~
      (__ \ "timeZone").read[String] ~
      (__ \ "status").read[String] ~
      (__ \ "advertiserId").read[String] ~
      (__ \ "startDate").read[String] ~
      (__ \ "endDate").read[Option[String]]
    ).apply(LineItem.apply _)

  val form = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  val anForm = DateTimeFormat.forPattern("yyyy-MM-dd")

  val lis = Json.parse(mxConn.requestAllLineItemJson)
    .validate(list(lineR)) match {
    case e @ JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v.filter(p => {
      val end = form.parseDateTime(p.endDate.getOrElse("2970-01-01T01:00:00Z"))
      end.isAfter(new DateTime().withDayOfMonth(1).minusMonths(1)) && end.getYear != 2970
    }).sortWith(_.advertiserId < _.advertiserId)
  }

  val dataCsv = anConn.requestAcctReport()
    .toList
    .tail
    .map(l =>
      new CsvData(anForm.parseDateTime(l(1)),
        l(0),
        l(2).toInt))
  val lineToData = lis
    .map(li =>
      (li, dataCsv
        .filter(l =>
          l.lineId.equals(li.id) &&
          !(l.month.getMonthOfYear == new DateTime().getMonthOfYear))))
    .filter(d => d._2.size != 0)

  val wb = new HSSFWorkbook()
  val df = wb.createDataFormat()
  val numStyle = wb.createCellStyle()
  numStyle.setDataFormat(df.getFormat("#,##0"))
  val normalStyle = wb.createCellStyle()
  val greenStyle = wb.createCellStyle()
  greenStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex)
  greenStyle.setFillPattern(CellStyle.SOLID_FOREGROUND)
  val greenNumStyle = wb.createCellStyle()
  greenNumStyle.cloneStyleFrom(greenStyle)
  greenNumStyle.setDataFormat(df.getFormat("#,##0"))
  val sheet = wb.createSheet("Mary's Special Report")
  val headers = sheet.createRow(1)
  headers.createCell(0).setCellValue("Line Item")
  headers.createCell(1).setCellValue("Start")
  headers.createCell(2).setCellValue("End")
  headers.createCell(3).setCellValue("Monthly Imps:")
  var rowCount = 3
  sheet.createRow(2).createCell(0).setCellStyle(normalStyle)
  for (li <- lineToData) {
    val dataRow = sheet.createRow(rowCount)
    val impRow = sheet.createRow(rowCount + 1)
    dataRow.createCell(0).setCellValue(li._1.name)
    dataRow.getCell(0).setCellStyle(normalStyle)
    dataRow.createCell(1).setCellValue(li._1.startDate)
    val start = form.parseDateTime(li._1.startDate).toLocalDate
    val end = form.parseDateTime(li._1.endDate.getOrElse("NONE")).toLocalDate
    dataRow.createCell(1).setCellValue(start.toString())
    dataRow.createCell(2).setCellValue(end.toString())
    dataRow.createCell(3)
    var cellCount = 4
    for (m <- li._2) {
      dataRow.createCell(cellCount).setCellValue(m.month.getMonthOfYear + "/" + m.month.getYear)
      impRow.createCell(cellCount).setCellValue(m.imps.intValue())
      impRow.getCell(cellCount).setCellStyle(numStyle)
      cellCount = cellCount + 1
    }
    if (lineToData.indexOf(li) != 0) {
      if (lineToData(lineToData.indexOf(li)-1)._1.advertiserId != li._1.advertiserId) {
        if(sheet.getRow(rowCount - 1).getCell(0).getCellStyle != greenStyle) {
          for (c <- dataRow) c.setCellStyle(greenStyle)
          for (c <- impRow) c.setCellStyle(greenNumStyle)
        } else {
          for (c <- dataRow) c.setCellStyle(normalStyle)
          for (c <- impRow) c.setCellStyle(numStyle)
        }
      } else {
          for (c <- dataRow) c.setCellStyle(sheet.getRow(c.getRowIndex-1).getCell(0).getCellStyle)
          for (c <- impRow) c.setCellStyle(sheet.getRow(c.getRowIndex-1).getCell(4).getCellStyle)
      }
    }
    rowCount+=2;
    sheet.createRow(rowCount-1).createCell(0).setCellStyle(normalStyle)
  }

  for(x <- 0 to 20) sheet.autoSizeColumn(x)
  val today = new LocalDate(DateTimeZone.UTC)
  val out = new FileOutputStream(new File(props.getOutputPath, "Monthly_Accounting_Report_" + today.toString+ ".xls"))
  wb.write(out)
  out.close()
}
case class CsvData(month: DateTime, lineId: String, imps: Integer)
