package com.mediacrossing.lifetimesegmentreport

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{AppNexusService, MxService}
import scala.collection.JavaConversions._
import scala.collection.mutable

object RunLifetimeSegmentReport extends App {


  val LOG = LoggerFactory.getLogger(RunLifetimeSegmentReport.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val props = new ConfigurationProperties(args)
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }
  val anConn = new AppNexusService(props.getPutneyUrl)

  // Request All Camps From MX
  val camps: List[(String, String)] = mxConn.requestAllCampaigns()
    .toList
    .map(c => (c.getId, c.getProfileID))

  // Request All Profiles From MX, match included segments to each Camp
  val idSegmentTupleList = anConn.requestAllProfileSegments.toList


  // Request AN Report for LT Imps For All Camps
  val report: List[Array[String]] = anConn.getLifetimeCampaignImpsAndSpend()
    .toList
    .tail





}