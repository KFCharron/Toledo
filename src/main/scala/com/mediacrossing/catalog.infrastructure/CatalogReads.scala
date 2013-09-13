package com.mediacrossing.catalog.infrastructure

import play.api.libs._
import json._
import Reads._
import functional.syntax._
import com.mediacrossing.catalog.domain.Catalog.Advertiser

object CatalogReads {

  val advertiserR: Reads[Advertiser] =
    (
      (__ \ "id").read[String] ~
        (__ \ "name").read[String] ~
        (__ \ "status").read[String] ~
        (__ \ "lineItemIds").read(list[String]) ~
        (__ \ "campaignIds").read(list[String]))
  .apply(Advertiser.apply _)

}
