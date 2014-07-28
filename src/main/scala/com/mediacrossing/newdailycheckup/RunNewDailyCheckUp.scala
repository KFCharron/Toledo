package com.mediacrossing.newdailycheckup

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import com.mediacrossing.dailycheckupsreport.{XlsWriter, JSONParse, DataStore}

object RunNewDailyCheckUp extends App {

  // Request all campaigns
  val LOG = LoggerFactory.getLogger(RunNewDailyCheckUp.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val props = new ConfigurationProperties(args)
  val fileOutputPath = props.getOutputPath
  val anConn = new AppNexusService(
    props.getPutneyUrl
  )
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }

  val dataStore = new DataStore
  dataStore.setCampaignArrayList(mxConn.requestAllCampaigns())

  val camps = dataStore.getLiveCampaignArrayList
    .toList

  val groupByAdvert = camps
    .map(c => (c.getAdvertiserID, c.getProfileID))
    .groupBy(t => t._1)
    .map(m => (m._1, m._2.map(t => t._2)))

  val pros = groupByAdvert.map(m => {
    // Request Profiles for Advertiser
    // TODO CHANGE URL BEFORE RELEASE
    val json = anConn.requests.getRequest("http://localhost:8888/an/profile?advertiser_id=" + m._1)
    JSONParse.parseProfiles(json).toList
  })
    .flatten
    .toList

  for {i <- 0 to pros.size-1} {
    for { x <- 0 to camps.size-1} {
      if (pros.get(i).getId.equals(camps.get(x).getProfileID)) camps.get(x).setProfile(pros.get(i))
    }

  }

  XlsWriter.writeAllReports(dataStore.getLiveCampaignArrayList, fileOutputPath)

}
