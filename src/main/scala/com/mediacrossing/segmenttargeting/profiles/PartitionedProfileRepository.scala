package com.mediacrossing.segmenttargeting.profiles

import java.util
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import java.io.IOException
import org.slf4j.LoggerFactory
import com.mediacrossing.dailycheckupsreport.{Profile, JSONParse}
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository
import com.mediacrossing.connections.HTTPRequest

class PartitionedProfileRepository(http: HTTPRequest,
                                   partitionSize: Int,
                                   requestDelay: Duration) extends ProfileRepository {

  private val log = LoggerFactory.getLogger(classOf[HTTPRequest])

  def findBy(advertiserIdAndProfileIds: util.List[(String, String)]): util.List[Profile] =
    (
      for {
        partition <- advertiserIdAndProfileIds.asScala.grouped(partitionSize)
      } yield {
        Thread.sleep(requestDelay.toMillis)
        for {
          (advertiserId, profileId) <- partition
        } yield {
          var success = false
          var requestCount = 1
          var json = ""
          while (!success) {
            try {
              json = http.getRequest("http://api.appnexus.com/profile?id=" + profileId + "&advertiser_id=" + advertiserId)
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

                Thread.sleep(65 * 1000)
            }
          }


          val p = new Profile
          p.setFrequencyTarget(JSONParse.populateFrequencyTarget(json))
          p.setDaypartTargetList(JSONParse.populateDaypartTarget(json))
          p.setGeographyTarget(JSONParse.populateGeographyTarget(json))
          p.setSegmentGroupTargets(JSONParse.populateSegmentGroupTargetList(json))

          p
        }
      })
      .toList
      .flatten
      .asJava
}