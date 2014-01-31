package com.mediacrossing.activelineitemreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.MxService
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.format.DateTimeFormat
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.{File, FileOutputStream}
import org.apache.poi.ss.usermodel.IndexedColors

object RunActiveLineItemReport extends App {


  val LOG = LoggerFactory.getLogger(RunActiveLineItemReport.getClass)
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

  LOG.debug(mxConn.requestAllLineItemJson())
  val lis = Json.parse(mxConn.requestAllLineItemJson)
    .validate(list(lineR)) match {
    case e @ JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v.filter(p => {
      val end = form.parseDateTime(p.endDate.getOrElse("2970-01-01T01:00:00Z"))
      end.isAfter(new DateTime()) && end.getYear != 2970
    }).sortWith(_.advertiserId < _.advertiserId)
  }

  val wb = new HSSFWorkbook()

  val sheet = wb.createSheet("Active Line Items")
  val headRow = sheet.createRow(0)
  headRow.createCell(0).setCellValue("Advertiser ID")
  headRow.createCell(1).setCellValue("Line Item ID")
  headRow.createCell(2).setCellValue("Name")
  headRow.createCell(3).setCellValue("Status")
  headRow.createCell(4).setCellValue("Start Date")
  headRow.createCell(5).setCellValue("End Date")
  val red = wb.createCellStyle()
  val font = wb.createFont()
  font.setColor(IndexedColors.RED.getIndex)
  font.setBoldweight(1000)
  red.setFont(font)
  var rowCount = 1
  lis.foreach(l => {
    val start = form.parseDateTime(l.startDate)
    val end = form.parseDateTime(l.endDate.get)
    val d = sheet.createRow(rowCount)
    d.createCell(0).setCellValue(l.advertiserId)
    d.createCell(1).setCellValue(l.id)
    d.createCell(2).setCellValue(l.name)
    d.createCell(3).setCellValue(l.status)
    d.createCell(4).setCellValue(start.getMonthOfYear + "/" + start.getDayOfMonth + "/" + start.getYear)
    d.createCell(5).setCellValue(end.getMonthOfYear + "/" + end.getDayOfMonth + "/" + end.getYear)
    if (end.isBefore(new DateTime().plusWeeks(1))) d.getCell(5).setCellStyle(red)
    rowCount += 1
  })

  for(x <- 0 to 6) sheet.autoSizeColumn(x)
  val today = new DateTime
  val out = new FileOutputStream(new File(props.getOutputPath, "Active_Line_Item_Report.xls"))
  wb.write(out)
  out.close()

}
case class LineItem(id: String, name: String, timeZone: String, status: String, advertiserId: String,
                    startDate: String, endDate: Option[String])
