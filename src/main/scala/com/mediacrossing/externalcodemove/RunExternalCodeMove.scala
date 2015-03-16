package com.mediacrossing.externalcodemove

import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.properties.ConfigurationProperties
import scala.collection.JavaConversions._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._


object RunExternalCodeMove extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  /*mxConn.requestAllLineItems().toList.filter(l => l.getName.substring(0, 3).equals("006")).foreach(l => {
    val adId = l.getAdvertiserId
    val nameSplit = l.getName.split("-")
    val newName = nameSplit(1)
    val sfId = nameSplit(0)
    val kindSplit = l.getName.split("_")
    val kind = kindSplit(kindSplit.size - 1).replace("[","").replace(":",";").replace("]","")
    println(kind)
    val json = "{\"line-item\":{\"name\":\"" + newName + "\", \"code\":\"" + sfId + " " + kind + "\"}}"
    anConn.requests.putRequest(properties.getPutneyUrl + "/line-item?id=" + l.getId + "&advertiser_id=" + adId, json)
  })*/

  /*mxConn.requestAllCampaigns().toList.filter(c => c.getName.substring(0, 3).equals("006")).foreach(c => {
    val adId = c.getAdvertiserID
    val nameSplit = c.getName.split("-")
    val newName = nameSplit(1)
    val sfId = nameSplit(0)
    val json = "{\"campaign\":{\"name\":\"" + newName + "\", \"code\":\"" + sfId + " " + c.getId + "\"}}"
    anConn.requests.putRequest(properties.getPutneyUrl + "/campaign?id=" + c.getId + "&advertiser_id=" + adId, json)

  })*/

  val creativeR: Reads[Creative] =
    (
      (__ \ "name").read[String] ~
        (__ \ "size_in_bytes").read[Int].map(id => id.toString)
      )
      .apply(Creative.apply _)

  val creativesR: Reads[List[Creative]] =
    (__ \ "response" \ "creatives").read(list(creativeR))

  val creatives = Json.parse(anConn.requests.getRequest(properties.getPutneyUrl + "/creative?advertiser_id=186356"))
    .validate(creativesR) match {
    case e @ JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v
  }

  creatives.foreach(c => println(c.name + ", " + c.size))
}
case class Creative(name:String, size: String)