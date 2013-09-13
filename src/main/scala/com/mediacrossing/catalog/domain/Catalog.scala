package com.mediacrossing.catalog.domain

object Catalog {

  case class Advertiser(id: String,
                        name: String,
                        status: String,
                        lineItemIds: List[String],
                        campaignIds: List[String])

}