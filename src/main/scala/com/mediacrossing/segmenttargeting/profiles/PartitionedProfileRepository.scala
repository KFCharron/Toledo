package com.mediacrossing.segmenttargeting.profiles

import java.util
import com.mediacrossing.segmenttargeting._
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import java.io.IOException
import org.slf4j.LoggerFactory

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

          while (!success) {
            try {
              http.requestProfile(profileId, advertiserId)
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


          val parser = new JSONParse
          val p = new Profile
          p.setFrequencyTarget(parser.populateFrequencyTarget(http.getJSONData))
          p.setDaypartTargetList(parser.populateDaypartTarget(http.getJSONData))
          p.setGeographyTarget(parser.populateGeographyTarget(http.getJSONData))
          p.setSegmentGroupTargets(parser.populateSegmentGroupTargetList(http.getJSONData))

          p
        }
      })
      .toList
      .flatten
      .asJava
}