package com.mediacrossing.pnlmetadatareport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.{File, FileOutputStream}
import org.joda.time.{DateTimeZone, LocalDate}

object RunPnlMetadataReport extends App {

  // Logs
  val LOG = LoggerFactory.getLogger(RunPnlMetadataReport.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  // Properties
  val props = new ConfigurationProperties(args)
  val fileOutputPath = props.getOutputPath
  val anConn = new AppNexusService(
    props.getPutneyUrl
  )
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }

  // Get All Active Payment Rules
  val paymentRules = mxConn.requestAllPublishers()
    .toList
    .filter(pub => pub.getStatus.equals("active"))
    .map(p => anConn.requestPaymentRules(p.getId).map(pr => ( pr, p.getPublisherName + " (" + p.getId + ")" )))
    .flatten

  // Get All Active Line Items
  implicit val reportingLabelR = Json.reads[ReportingLabel]
  val lineItemR: Reads[LineItem] =
    (
      (__ \ "id").read[Int]
        .map(id => id.toString) ~
        (__ \ "name").read[String] ~
        (__ \ "code").readNullable[String] ~
        (__ \ "state").read[String] ~
        (__ \ "advertiser_id").read[Int]
          .map(id => id.toString) ~
        (__ \ "revenue_type").read[String] ~
        (__ \ "revenue_value").read[BigDecimal] ~
        (__ \ "comments").readNullable[String] ~
        (__ \ "labels").readNullable(list[ReportingLabel])
          .map(_.getOrElse(Nil))
      )
      .apply(LineItem.apply _)

  val lineItemsR: Reads[List[LineItem]] =
    (__ \ "response" \ "line-items").read(list(lineItemR))

  val lineItems = Json.parse(anConn.requestActiveLineItemJson())
      .validate(lineItemsR) match {
      case e @ JsError(_) => sys.error(JsError.toFlatJson(e).toString())
      case JsSuccess(v, _) => v.filter(li => li.status.equals("active"))
    }


  // Get All Live Rail Line Items
  val liveRailR = (
    (__ \ "id").read[String] ~
      (__ \ "name").read[String] ~
      (__ \ "status").read[String] ~
      (__ \ "advertiserId").read[String]
    ).tupled

  val mxLiveRailLines = Json.parse(mxConn.requestAllLiveRailLineItemJson())
    .validate(list(liveRailR)) match {
    case e @ JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v.filter(t => t._3.equals("active"))
  }

  // Report : Sheet For Each
  val workbook = {

    val wb = new HSSFWorkbook()
    val boldStyle = wb.createCellStyle()
    val boldFont = wb.createFont()
    boldFont.setBoldweight(1000)
    boldStyle.setFont(boldFont)

    // PR Sheet
    val prSheet = wb.createSheet("Payment Rules")
    val headers = {"Publisher" :: "ID" :: "Payment Rule" :: "Pricing Type" :: "Pricing Value" :: Nil}
    val headerRow = prSheet.createRow(0)
    headers.foreach(h => {
      headerRow.createCell(headers.indexOf(h)).setCellValue(h)
      headerRow.getCell(headers.indexOf(h)).setCellStyle(boldStyle)
    })
    paymentRules.foreach(r => {
      val row = prSheet.createRow(paymentRules.indexOf(r) + 1)
      row.createCell(0).setCellValue(r._2)
      row.createCell(1).setCellValue(r._1.getId)
      row.createCell(2).setCellValue(r._1.getName)
      row.createCell(3).setCellValue(r._1.getPricingType)
      row.createCell(4).setCellValue(r._1.getRevshare)
    })
    headers.foreach(h => prSheet.autoSizeColumn(headers.indexOf(h)))

    // LI Sheet
    val liSheet = wb.createSheet("Line Items")
    val liHeaders = {"Advertiser" :: "ID" :: "Name" :: "Code" :: "Label" ::
      "Revenue Type" :: "Revenue Value" :: "Comments" :: Nil}
    val liHeaderRow = liSheet.createRow(0)
    liHeaders.foreach(h => {
      liHeaderRow.createCell(liHeaders.indexOf(h)).setCellValue(h)
      liHeaderRow.getCell(liHeaders.indexOf(h)).setCellStyle(boldStyle)
    })
    lineItems.foreach(l => {
      val row = liSheet.createRow(lineItems.indexOf(l) + 1)
      row.createCell(0).setCellValue(l.advertiserId)
      row.createCell(1).setCellValue(l.id)
      row.createCell(2).setCellValue(l.name)
      row.createCell(3).setCellValue(l.code.getOrElse(""))
      row.createCell(4).setCellValue(l.labels.map(l => l.name + ": " + l.value).mkString(", "))
      row.createCell(5).setCellValue(l.revenueType)
      row.createCell(6).setCellValue(l.revenueValue.toString())
      row.createCell(7).setCellValue(l.comments.getOrElse(""))
    })
    liHeaders.foreach(h => liSheet.autoSizeColumn(liHeaders.indexOf(h)))

    // LR Sheet
    val lrSheet = wb.createSheet("LiveRail")
    val lrHeaders = {"Advertiser" :: "ID" :: "Name" :: Nil}
    val lrHeaderRow = lrSheet.createRow(0)
    lrHeaders.foreach(h => {
      lrHeaderRow.createCell(lrHeaders.indexOf(h)).setCellValue(h)
      lrHeaderRow.getCell(lrHeaders.indexOf(h)).setCellStyle(boldStyle)
    })
    mxLiveRailLines.foreach(l => {
      val row = lrSheet.createRow(mxLiveRailLines.indexOf(l) + 1)
      row.createCell(0).setCellValue(l._4)
      row.createCell(1).setCellValue(l._1)
      row.createCell(2).setCellValue(l._2)
    })
    lrHeaders.foreach(h => lrSheet.autoSizeColumn(lrHeaders.indexOf(h)))

    wb
  }

  val fileOut = new FileOutputStream(new File(props.getOutputPath, "PnL_Metadata_Report_" + new LocalDate(DateTimeZone.UTC).toString + ".xls"))
  workbook.write(fileOut)
  fileOut.close()

}
case class LineItem(id: String,
                    name: String,
                    code: Option[String],
                    status: String,
                    advertiserId: String,
                    revenueType: String,
                    revenueValue: BigDecimal,
                    comments: Option[String],
                    labels: List[ReportingLabel])

case class ReportingLabel(id: Int,
                          value: String,
                          name: String)
