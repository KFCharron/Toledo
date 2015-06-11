package com.mediacrossing.segquery

import com.mediacrossing.connections.{MxService, AppNexusService}
import com.mediacrossing.properties.ConfigurationProperties
import org.joda.time.DateTime
import scala.collection.JavaConversions._

object RunQuarterlyDataReport extends App {

  val properties: ConfigurationProperties = new ConfigurationProperties(args)
  val anConn: AppNexusService = new AppNexusService(properties.getPutneyUrl)
  val mxConn: MxService = new MxService(properties.getMxUrl, properties.getMxUsername, properties.getMxPassword)

  val adId = "186356"
  val beforeDate = new DateTime(2015, 4, 1, 0, 0)
  val afterDate = new DateTime(2014, 12, 31, 11, 59)

  mxConn.requestAllLineItems()
    .toList
    .filter(l => l.getAdvertiserId.equals(adId) &&
                  l.getEndDate.isAfter(afterDate) &&
                  l.getEndDate.isBefore(beforeDate) &&
                  !l.getName.toLowerCase.contains("youtube") &&
                  !l.getName.toLowerCase.contains("preroll") &&
                  !l.getName.toLowerCase.contains("pre-roll") &&
                  !l.getName.toLowerCase.contains("search") &&
                    l.getName.toLowerCase.contains("ax"))
    .map(l => l.getId + ",")
    .foreach(println)

}
