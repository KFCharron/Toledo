package com.mediacrossing.platformmemberlookup

import com.mediacrossing.connections.AppNexusService
import com.mediacrossing.properties.ConfigurationProperties
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.JsSuccess


object RunMemberLookup extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)

  val platR = (
    (__ \ "name").read[String] ~
      (__ \ "primary_type").read[String]
    ).apply(PlatformMember.apply _)

  val members = Json.parse(anConn.requests.getRequest(properties.getPutneyUrl + "/platform-member"))
    .\("response").\("platform-members")
    .validate(list(platR)) match {
    case e@JsError(_) => sys.error(JsError.toFlatJson(e).toString())
    case JsSuccess(v, _) => v
  }

  val outputFilename = "/Users/charronkyle/Desktop/Platform_Members.csv"
  val pw = new java.io.PrintWriter(outputFilename)

  members.foreach(m => pw.println(s"${m.name}, ${m.kind}"))

  pw.close()


}
case class PlatformMember(rawName: String, kind: String) {
  val name = rawName.replace(",", " ")
}
