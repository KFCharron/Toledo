package com.mediacrossing.rollingdomainimpreport

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.AppNexusService
import scala.collection.JavaConversions._
import org.joda.time.{DateTimeZone, LocalDate, DateTime}
import org.joda.time.format.DateTimeFormat
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.{File, FileOutputStream}

object RunRollingDomainImpReport extends App {

  val props = new ConfigurationProperties(args)
  val anConn = new AppNexusService(
    props.getPutneyUrl)

  val wb = new HSSFWorkbook()

  val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

  val adList: List[Advertiser] = anConn.requestRollingDomainImpsReport()
    .toList
    .tail
    .map(line => DataRow(advertiser = line(0),
                         domain = line(1),
                         day = line(2),
                         imps = line(3).toInt))
    .groupBy(_.advertiser)
    .toList
    .map(r =>
      Advertiser(name = r._1,
                 domains = r._2.groupBy(_.domain)
                               .toList
                               .map(d => Domain(name = d._1,
                                                dataList = d._2.map(i => DailyData(i.day, i.imps))))))

  adList.foreach(createSheet)

  def createSheet(ad: Advertiser) = {
    val sheet = wb.createSheet(ad.name)
    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("Domain")
    for (x <- 1 to 7) headerRow.createCell(x).setCellValue(new DateTime().minusDays(x).toString(dateFormat))
    ad.domains.foreach(d => {
      val line = sheet.createRow(sheet.getPhysicalNumberOfRows)
      line.createCell(0).setCellValue(d.name)
      d.dataList.foreach(i => for(x <- 1 to 7) {
        if (headerRow.getCell(x).getStringCellValue.equals(i.day))
          line.createCell(x).setCellValue(i.imps.intValue())
      })
    })
    for (x <- 0 to 7) sheet.autoSizeColumn(x)
  }

  val today = new LocalDate(DateTimeZone.UTC)
  wb.write(new FileOutputStream(new File(props.getOutputPath, "Daily_Rolling_Domain_Imps_" + today.toString + ".xls")))

}
case class DataRow(advertiser: String, domain: String, day: String, imps: Integer)
case class DailyData(day: String, imps: Integer)
case class Domain(name: String, dataList: List[DailyData])
case class Advertiser(name: String, domains: List[Domain])
