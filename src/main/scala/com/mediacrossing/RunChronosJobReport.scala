package com.mediacrossing

import play.api.libs.functional.syntax._
import play.api.libs.json._
import org.joda.time.format.DateTimeFormat
import com.mediacrossing.properties.ConfigurationProperties
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import play.api.libs.json.Reads._
import play.api.libs.json.JsSuccess
import com.mediacrossing.connections.HTTPRequest
import org.joda.time.{DateTimeZone, LocalDate, DateTime}
import java.io.{File, FileOutputStream}

object RunChronosJobReport extends App {

  val outPath = new ConfigurationProperties(args).getOutputPath

  // Query Chronos For all config
  val jobR = (
    (__ \ "name").read[String] ~
      (__ \ "lastSuccess").read[String] ~
      (__ \ "lastError").read[String] ~
      (__ \ "schedule").read[String]
    ).apply(ChronosJob.apply _)

  val jobs = Json.parse(new HTTPRequest().getRequest("http://mesos-test-01.mx:4400/scheduler/jobs"))
    .validate(list(jobR)) match {
    case e@JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v
  }


  // For each job, list in wb
  val today = new DateTime().withTimeAtStartOfDay()
  val wb = new HSSFWorkbook()
  val sheet = wb.createSheet("Chronos Jobs Report")
  var header = sheet.createRow(0)
  val jobSuccessCount = jobs.count(j => j.successDate.isAfter(today))
  header.createCell(0).setCellValue("Successful Jobs Today:")
  header.createCell(1).setCellValue(jobSuccessCount)
  header = sheet.createRow(1)
  val jobFailureCount = jobs.count(j => j.errorDate.isAfter(today))
  header.createCell(0).setCellValue("Failed Jobs Today:")
  header.createCell(1).setCellValue(jobFailureCount)
  val jobHeaders = sheet.createRow(3)
  jobHeaders.createCell(0).setCellValue("Name")
  jobHeaders.createCell(1).setCellValue("Last Error")
  jobHeaders.createCell(2).setCellValue("Last Success")
  jobHeaders.createCell(3).setCellValue("Schedule")
  var rowCount = 4
  jobs.foreach(j => {
    val row = sheet.createRow(rowCount)
    row.createCell(0).setCellValue(j.name)
    row.createCell(1).setCellValue(j.lastError)
    row.createCell(2).setCellValue(j.lastSuccess)
    row.createCell(3).setCellValue(j.scheduleRaw)
    sheet.autoSizeColumn(0)
    sheet.autoSizeColumn(1)
    sheet.autoSizeColumn(2)
    sheet.autoSizeColumn(3)
    rowCount += 1
  })

  val todayUtc = new LocalDate(DateTimeZone.UTC)
  wb.write(new FileOutputStream(new File(outPath, "Chronos_Daily_" + todayUtc.toString + ".xls")))


}
case class ChronosJob(name: String, successString: String, errorString: String, scheduleRaw: String) {
  val timestamp = DateTimeFormat.forPattern("yyyy-M-d'T'HH:mm:ss.SSS'z'")
  val lastSuccess = {
    if(successString.size <= 1) "None"
    else {
      val d = timestamp.parseDateTime(successString)
      d.getMonthOfYear + "/" + d.getDayOfMonth
    }
  }
  val lastError = {
    if(errorString.size <= 1) "None"
    else {
      val d = timestamp.parseDateTime(errorString)
      d.getMonthOfYear + "/" + d.getDayOfMonth
    }
  }
  val successDate = {
    if(successString.size <= 1) new DateTime().withYearOfCentury(1)
    else timestamp.parseDateTime(successString)
  }
  val errorDate = {
    if(errorString.size <= 1) new DateTime().withYearOfCentury(1)
    else timestamp.parseDateTime(errorString)
  }
  val scheduleString = scheduleRaw.substring(0, scheduleRaw.length-4)
  val frequency = scheduleRaw.substring(scheduleRaw.length-4, scheduleRaw.length)
}
