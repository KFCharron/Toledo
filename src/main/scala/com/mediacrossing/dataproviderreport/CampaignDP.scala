package com.mediacrossing.dataproviderreport

import com.mediacrossing.dailycheckupsreport.ServingFee

case class CampaignDP(id: String, name: String, imps: Int, clicks: Int, cpm: Double, servingFees: List[ServingFee])
