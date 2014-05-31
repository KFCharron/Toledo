package com.mediacrossing.dailypacingreport

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{AppNexusService, MxService}
import org.joda.time.DateTime
import scala.collection.JavaConversions._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import scala.collection.immutable.Iterable
import scala.collection.mutable


object RunPacing extends App {

  val props = new ConfigurationProperties(args)
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }
  val anConn = new AppNexusService(
    props.getPutneyUrl)

  val dateFormat: DateTimeFormatter = DateTimeFormat
    .forPattern("yy-mm-dd")

  val adR = (
    (__ \ "id").read[String] ~
      (__ \ "name").read[String] ~
      (__ \ "status").read[String]
    ).apply(BaseAdvertiser.apply _)

  val liveAdvertisers = Json.parse(mxConn.requestAllAdvertiserJson)
    .validate(list(adR)) match {
    case e@JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v.filter(p => p.status.equals("active") &&
      !p.id.equals("186199") &&
      !p.id.equals("151391"))
  }

  val lineR = (
    (__ \ "name").read[String] ~
      (__ \ "id").read[String] ~
      (__ \ "status").read[String] ~
      (__ \ "startDate").read[String] ~
      (__ \ "endDate").read[String] ~
      (__ \ "advertiserId").read[String] ~
      (__ \ "lifetimeBudget").read[String]
    ).apply(BaseLineItem.apply _)

  val liveLineItems = Json.parse(mxConn.requestAllLineItemJson)
    .validate(list(lineR)) match {
    case e@JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v.filter(p => p.status.equals("active"))





























    // Query MX for all adverts.
    // Filter one's that are active
    // Set Date Range to Earliest Start and Today

    //  val dataRows: List[DataRow] = anConn.requestDailyPacingReport()
    //    .toList
    //    .tail
    //    .map(line => DataRow(advertiserName = line(0),
    //                        lineItemName = line(1),
    //                        day = dateFormat.parseDateTime(line(2)),
    //                        imps = line(3).toInt))
    //
    //  val advertisers: Iterable[Map[String, List[DataRow]]] = dataRows.groupBy(r => r.advertiserName).map(a => a._2.groupBy(l => l.lineItemName))


  }
}
case class DataRow(advertiserName: String, lineItemName: String, day: DateTime, imps: Int)
case class BaseAdvertiser(name: String, id: String, status: String)
case class BaseLineItem(name: String, id: String, status: String,
                        start: String, end: String, adId: String, budget: String) {
  val form = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
  val startDate = form.parseDateTime(start)
  val endDate = form.parseDateTime(end)
  val lifetimeBudget = budget.toInt

}

