package com.mediacrossing.monthlybillingsummary

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}

object RunMonthlyBillingSummary extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val adverts = mxConn.requestAllAdvertisers()
  val camps = mxConn.requestAllCampaigns()



  // get month report for 1770 imps, spend
  // get month report for AdEx Imps, spend
  // get month report for Total AN imps (total imps - self selling and adEx), spend
  // all grouped by advertiser
  // use total an imps to calc each data provider imp count for each advertiser


}
