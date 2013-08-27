package com.mediacrossing.segmenttargeting.profiles

import java.util
import com.mediacrossing.segmenttargeting.{HTTPRequest, JSONParse, Profile}
import scala.collection.JavaConverters._

class TruncatedProfileRepository(http: HTTPRequest,
                                 profileCount: Int) extends ProfileRepository {

  def findBy(advertiserIdAndProfileIds: util.List[(String, String)]): util.List[Profile] =
    (
      for {
        (advertiserId, profileId) <- advertiserIdAndProfileIds.asScala.take(profileCount)
      } yield {
        http.requestProfile(profileId, advertiserId)

        val parser = new JSONParse
        val p = new Profile
        p.setFrequencyTargets(parser.populateFrequencyTarget(http.getJSONData))
        p.setDaypartTargetList(parser.populateDaypartTarget(http.getJSONData))
        p.setGeographyTargets(parser.populateGeographyTarget(http.getJSONData))
        p.setSegmentGroupTargets(parser.populateSegmentGroupTargetList(http.getJSONData))

        p
      })
      .asJava
}
