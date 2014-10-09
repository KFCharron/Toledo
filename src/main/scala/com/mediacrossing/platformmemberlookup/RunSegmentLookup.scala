package com.mediacrossing.platformmemberlookup

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.AppNexusService
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object RunSegmentLookup extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)

  val segR = (
    (__ \ "id").read[Int] ~
    (__ \ "code").read[Option[String]] ~
    (__ \ "short_name").read[Option[String]] ~
    (__ \ "price").read[Float] ~
    (__ \ "advertiser_id").read[Option[Int]]
    ).apply(Segment.apply _)

  val members = Json.parse(anConn.requests.getRequest(properties.getPutneyUrl + "/segment"))
    .\("response").\("segments")
    .validate(list(segR)) match {
    case e@JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v
  }

  val outputFilename = "/Users/charronkyle/Desktop/All_Segments.csv"
  val pw = new java.io.PrintWriter(outputFilename)
  pw.println("Name, ID, Code, Price, Advertiser ID")
  members.foreach(m => pw.println(s"${m.name}, ${m.id}, ${m.code.getOrElse("N/A")}, ${m.price}, ${m.adId.getOrElse("N/A")}"))

  pw.close()


}
case class Segment(id: Int, code: Option[String], rawName: Option[String], price: Float, adId: Option[Int]) {
  val name = rawName.getOrElse("N/A").replace(",", " ")
}
