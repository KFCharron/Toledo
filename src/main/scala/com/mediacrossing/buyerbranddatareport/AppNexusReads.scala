package com.mediacrossing.buyerbranddatareport

import play.api.libs._
import functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

object AppNexusReads {

  val pubR = (
    (__ \ "id").read[String] ~
      (__ \ "name").read[String] ~
      (__ \ "status").read[String] ~
      (__ \ "siteIds").read(list[String]) ~
      (__ \ "placementIds").read(list[String])
    ).apply(PubJson.apply _)
}
