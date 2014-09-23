package com.mediacrossing.formatvalidation

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import org.apache.poi.ss.usermodel.Workbook
import java.io.{File, FileOutputStream}
import scala.collection.JavaConversions._
import com.mediacrossing.publishercheckup.PaymentRule
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.JsSuccess
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import scala.collection.mutable
import org.joda.time.{DateTimeZone, LocalDate}

object RunFormatValidation extends App {

  // Logs
  val LOG = LoggerFactory.getLogger(RunFormatValidation.getClass)
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

  val paymentRules: List[mutable.Buffer[(PaymentRule, String)]] = {

    val excludedPubIds: List[String] = List("176863", "311754", "336335", "395998", "401244", "434706", "420529", "437359")

    mxConn.requestAllPublishers()
      .toList
      .filterNot(pub => excludedPubIds.contains(pub.getId))
      .filter(pub => pub.getStatus.equals("active"))
      .map(p => anConn.requestPaymentRules(p.getId).map(pr => ( pr, p.getPublisherName + " (" + p.getId + ")" )))

  }

  def checkPaymentRules(pRules: List[mutable.Buffer[(PaymentRule, String)]]): List[(PaymentRule, String)] = {
    pRules
      .flatten
      .filterNot { case (rule, pubName) =>
      (rule.getName.toLowerCase.contains("cpm") && rule.getPricingType.contains("cpm")) ||
        (rule.getName.toLowerCase.contains("netrevshare") && rule.getPricingType.contains("revshare")) ||
        (rule.getName.toLowerCase.contains("grossrevshare") && rule.getPricingType.contains("revshare") ||
        (rule.getName.toLowerCase.contains("default") || rule.getName.toLowerCase.contains("base payment rule")))
      }
  }

  def checkPubs(pRules: List[mutable.Buffer[(PaymentRule, String)]]): List[String] = {
    pRules.filter(b => b.size <= 1).map(b => b.head._2)
  }

  val activeLineItemsFromMx : List[LineItem] = {
    val lineR = (
      (__ \ "id").read[String] ~
        (__ \ "name").read[String] ~
        (__ \ "status").read[String] ~
        (__ \ "campaignType").read[Option[String]] ~
        (__ \ "advertiserXRev").read[Option[Float]] ~
        (__ \ "channelType").read[String] ~
        (__ \ "liveRailLineItemId").read[Option[String]]
      ).apply(LineItem.apply _)

    Json.parse(mxConn.requestAllLineItemJson)
      .validate(list(lineR)) match {
      case e @ JsError(_) => sys.error(JsError.toFlatJson(e).toString())
      case JsSuccess(v, _) => v.filter(li => li.status.equals("active"))
    }
  }

  def checkLineItems(mxLines: List[LineItem]) : List[LineItem] = {
    mxLines.filterNot(li => li.campType.isDefined &&
      (li.campType.get.equals("exchange") ||
        li.campType.get.equals("direct") ||
        (li.campType.get.equals("crossed") && !li.xRev.isEmpty))
    )
  }

  def checkLiveRailLineItems(mxLines: List[LineItem]) : List[(String, String, String)] = {

    val anLiveRailLineIds: List[String] = mxLines
      .filter(line => line.liveRailId.isDefined)
      .map(l => l.liveRailId.get.substring(4))

    val liveRailR = (
        (__ \ "id").read[String] ~
        (__ \ "name").read[String] ~
        (__ \ "status").read[String]
      ).tupled

    val mxLiveRailLines = Json.parse(mxConn.requestAllLiveRailLineItemJson())
      .validate(list(liveRailR)) match {
        case e @ JsError(_) => sys.error(JsError.toFlatJson(e).toString())
        case JsSuccess(v, _) => v
      }

    mxLiveRailLines
      .filter(l => l._3.equals("active"))
      .filterNot(l => anLiveRailLineIds.contains(l._1.substring(4)))
  }

  def writeReport(pubs: List[String],
                  paymentRules: List[(PaymentRule, String)],
                  lineItems: List[LineItem],
                  liveRailLineItems: List[(String, String, String)]) : Workbook = {
    val wb = new HSSFWorkbook
    val sheet = wb.createSheet("Configuration Warnings")
    val boldStyle = wb.createCellStyle()
    val boldFont = wb.createFont()
    boldFont.setBoldweight(1000)
    boldStyle.setFont(boldFont)
    var rowCount = 0

    if (pubs.size > 0) {
      var header = sheet.createRow(rowCount)
      header.createCell(0).setCellValue("The Following Publishers Are Missing Payment Rules:")
      header.getCell(0).setCellStyle(boldStyle)
      rowCount += 1
      header = sheet.createRow(rowCount)
      header.createCell(1).setCellValue("Publisher")
      for( cell <- header ) cell.setCellStyle(boldStyle)
      rowCount += 1
      pubs.foreach(p => {
        val pubRow = sheet.createRow(rowCount)
        pubRow.createCell(1).setCellValue(p)
        rowCount += 1
      })
      rowCount += 2
    }

    if (paymentRules.size > 0) {
      var header = sheet.createRow(rowCount)
      header.createCell(0).setCellValue("Please Check The Following Payment Rules For Proper Configuration:")
      header.getCell(0).setCellStyle(boldStyle)
      rowCount += 1
      header = sheet.createRow(rowCount)
      header.createCell(1).setCellValue("Publisher")
      header.createCell(2).setCellValue("Payment Rule")
      header.createCell(3).setCellValue("Pricing Type")
      for( cell <- header ) cell.setCellStyle(boldStyle)
      rowCount += 1
      paymentRules.foreach(pr => {
        val paymentRow = sheet.createRow(rowCount)
        paymentRow.createCell(1).setCellValue(pr._2)
        paymentRow.createCell(2).setCellValue(pr._1.getName + " (" + pr._1.getId + ")")
        paymentRow.createCell(3).setCellValue(pr._1.getPricingType)
        rowCount += 1
      })
      rowCount += 2
    }

    if (lineItems.size > 0) {
      var header = sheet.createRow(rowCount)
      header.createCell(0).setCellValue("Please Check The Following Line Items For Proper Configuration:")
      header.getCell(0).setCellStyle(boldStyle)
      rowCount += 1
      header = sheet.createRow(rowCount)
      header.createCell(1).setCellValue("Line Item")
      header.createCell(2).setCellValue("Concern")
      for( cell <- header ) cell.setCellStyle(boldStyle)
      rowCount += 1
      lineItems.foreach(li => {
        val lineRow = sheet.createRow(rowCount)
        lineRow.createCell(1).setCellValue(li.name + " (" + li.id + ")")
        lineRow.createCell(2).setCellValue({
          if (li.campType.isEmpty) "Campaign Type Is Not Defined"
          else if(li.campType.get.equals("crossed")) "Campaign Type Is Crossed, But Revenue Not Set"
          else "Campaign Type Is Misspelled"
        })
        rowCount += 1
      })
      rowCount += 2
    }

    if (liveRailLineItems.size > 0) {
      var header = sheet.createRow(rowCount)
      header.createCell(0).setCellValue("Please Check The Following LiveRail Line Items In AppNexus:")
      header.getCell(0).setCellStyle(boldStyle)
      rowCount += 1
      header = sheet.createRow(rowCount)
      header.createCell(1).setCellValue("Line Item")
      for( cell <- header ) cell.setCellStyle(boldStyle)
      rowCount += 1
      liveRailLineItems.foreach(lr => {
        val liveRow = sheet.createRow(rowCount)
        liveRow.createCell(1).setCellValue(lr._2 + " (" + lr._1 + ")")
        rowCount += 1
      })
    }
    for(x <- 0 to 3) sheet.autoSizeColumn(x)
    wb
  }

  val pubsMissingPaymentRules = checkPubs(paymentRules)
  val brokenPaymentRules = checkPaymentRules(paymentRules)
  val lineItems = checkLineItems(activeLineItemsFromMx)
  val liveRailLineItems = checkLiveRailLineItems(activeLineItemsFromMx)

  if(!(brokenPaymentRules.size == 0 && lineItems.size == 0 && liveRailLineItems.size == 0)) {
    val report = writeReport(pubsMissingPaymentRules, brokenPaymentRules, lineItems, liveRailLineItems)
    val fileOut = new FileOutputStream(new File(props.getOutputPath, "Format_Validation_" + new LocalDate(DateTimeZone.UTC).toString + ".xls"))
    report.write(fileOut)
    fileOut.close()
  }
  else LOG.info("All Live Line Items Correctly Formatted For PnL Calculations. Exiting Program.")

}
case class LineItem(id: String,
                    name: String,
                    status: String,
                    campType: Option[String],
                    xRev: Option[Float],
                    channelType: String,
                    liveRailId: Option[String])
