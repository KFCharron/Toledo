package com.mediacrossing.conversionwatch

import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import org.joda.time.{DateTimeZone, LocalDate, DateTime}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.joda.time.format.DateTimeFormat
import java.io.{File, FileOutputStream}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.JsSuccess

object RunConversionWatch extends App {

  // Global Vars
  val LOG = LoggerFactory.getLogger(RunConversionWatch.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })
  val props = new ConfigurationProperties(args)
  val fileOutputPath = props.getOutputPath
  val anConn = new AppNexusService(
    props.getPutneyUrl
  )
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }

  val wb = new HSSFWorkbook()
  val boldStyle = wb.createCellStyle()
  val boldFont = wb.createFont()
  boldFont.setBoldweight(1000)
  boldStyle.setFont(boldFont)
  val sheet = wb.createSheet("Concerning Pixels")
  val header = sheet.createRow(0)
  header.createCell(0).setCellValue("Pixel")
  header.createCell(1).setCellValue("Last Fired")
  header.createCell(2).setCellValue("State")
  header.createCell(3).setCellValue("Advertiser")
  header.createCell(4).setCellValue("Created")
  header.createCell(5).setCellValue("Modified")
  for(c <- header) c.setCellStyle(boldStyle)
  var rowCount = 1

  val activeAdvertisers = mxConn.requestAllAdvertisers()
    .toList
    .filter(_.isLive)
    .map(a => s"${a.getAdvertiserName} (${a.getAdvertiserID})")

  // Date 3 days from now
  val lookbackDate = new DateTime().withTimeAtStartOfDay().minusDays(3)
  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
  val pixels = anConn.requestPixelReport()
    .toList
    .tail
    .map(row => ReportData(row(0), dateFormat.parseDateTime(row(1)).withTimeAtStartOfDay(), row(2)))

  val pixelR = (
    (__ \ "id").read[Int] ~
      (__ \ "name").read[String] ~
      (__ \ "state").read[String] ~
      (__ \ "created_on").read[String] ~
      (__ \ "last_modified").read[String]
    ).apply(ApiData.apply _)

  val pixelIds = pixels.map(_.id)
  val pixelUrl = s"/pixel?id=${pixelIds.mkString(",")}"
  val fullUrl = props.getPutneyUrl + pixelUrl
  val apiPixels = Json.parse(anConn.requests.getRequest(fullUrl)).\("response").\("pixels")
    .validate(list(pixelR)) match {
    case e@JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v
  }
  val apiPixelMap = apiPixels.map(p => p.id -> p).toMap

  val finalPixels = pixels.map(p => {
    println(p.id)
    val apiPixel = apiPixelMap.get(p.id).get
    DeadPixel(
      id = p.id,
      lastFired = p.lastFired,
      advertiser = p.advertiser,
      name = apiPixel.name,
      state = apiPixel.state,
      created = dateFormat.parseDateTime(apiPixel.created),
      modified = dateFormat.parseDateTime(apiPixel.modified)
    )
  }
  ).foreach(p => {
    val row = sheet.createRow(rowCount)
    rowCount += 1
    row.createCell(0).setCellValue(s"${p.name} (${p.id})")
    row.createCell(1).setCellValue(p.lastFired.toString(dateFormat))
    row.createCell(2).setCellValue(p.state)
    row.createCell(3).setCellValue(p.advertiser)
    row.createCell(4).setCellValue(p.created.toString(dateFormat))
    row.createCell(5).setCellValue(p.modified.toString(dateFormat))
  })


  for (x <- 0 to 5) sheet.autoSizeColumn(x)

  val nowString = new LocalDate(DateTimeZone.UTC).toString
  val fileOut = new FileOutputStream(new File(fileOutputPath, s"Conversion_Watch_$nowString.xls"))
  wb.write(fileOut)
  fileOut.close()

}
case class ReportData(id: String, lastFired: DateTime, advertiser: String)
case class ApiData(rawId: Int, name: String, state: String, created: String, modified: String) {
  val id = rawId.toString
}
case class DeadPixel(id: String, lastFired: DateTime, advertiser: String,
                     name: String, state: String, created: DateTime, modified: DateTime)