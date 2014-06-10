package com.mediacrossing.segmenttargeting.profiles

import java.util
import scala.collection.JavaConverters._
import java.io.IOException
import org.slf4j.LoggerFactory
import com.mediacrossing.dailycheckupsreport.{Profile, JSONParse}
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository
import com.mediacrossing.connections.HTTPRequest

class PutneyProfileRepository(http: HTTPRequest) extends ProfileRepository {

  private val log = LoggerFactory.getLogger(classOf[HTTPRequest])

  def findBy(advertiserIdAndProfileIds: util.List[(String, String)]): util.List[Profile] =
    (
      for {
        (advertiserId, profileId) <- advertiserIdAndProfileIds.asScala
      } yield {
        var success = false
        var requestCount = 1
        var json = ""
        while (!success) {
          try {
            json = http.getRequest("http://putney-01.mx:8888/an/profile?id=" + profileId + "&advertiser_id=" + advertiserId)
            success = true

            log.info("Received profile [profileId = " + profileId +
              ", advertiserId = " + advertiserId + "] after " + requestCount +
              " request attempts")
          } catch {
            case e: IOException =>
              // Rate limit exceeded
              log.warn("Rate limit exceeded for profile [profileId = " + profileId +
                ", advertiserId = " + advertiserId + "], request attempts = " +
                requestCount, e)
              requestCount = requestCount + 1
          }
        }


        val p = new Profile
        p.setId(profileId)
        p.setLastModified(JSONParse.obtainLastModified(json))
        p.setFrequencyTarget(JSONParse.populateFrequencyTarget(json))
        p.setDaypartTargetList(JSONParse.populateDaypartTarget(json))
        p.setGeographyTarget(JSONParse.populateGeographyTarget(json))
        p.setSegmentGroupTargets(JSONParse.populateSegmentGroupTargetList(json))

        p
      })
      .asJava
}