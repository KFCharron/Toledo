package com.mediacrossing.segmenttargeting.profiles

import java.util
import com.mediacrossing.segmenttargeting._
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

class PartitionedProfileRepository(http: HTTPRequest,
                                   partitionSize: Int,
                                   requestDelay: Duration) extends ProfileRepository {

  def findBy(advertiserIdAndProfileIds: util.List[(String, String)]): util.List[Profile] =
    (
      for {
        partition <- advertiserIdAndProfileIds.asScala.grouped(partitionSize)
      } yield {
        for {
          (advertiserId, profileId) <- partition
        } yield {
          http.requestProfile(profileId, advertiserId)

          val parser = new JSONParse
          val p = new Profile
          p.setFrequencyTarget(parser.populateFrequencyTarget(http.getJSONData))
          p.setDaypartTargetList(parser.populateDaypartTarget(http.getJSONData))
          p.setGeographyTarget(parser.populateGeographyTarget(http.getJSONData))
          p.setSegmentGroupTargets(parser.populateSegmentGroupTargetList(http.getJSONData))

          Thread.sleep(requestDelay.toMillis)

          p
        }
      })
      .toList
      .flatten
      .asJava
}