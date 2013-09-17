package com.mediacrossing.segmenttargeting.profiles

import java.util
import com.mediacrossing.segmenttargeting.{HTTPConnection, JSONParse, Profile}
import scala.collection.JavaConverters._

class TruncatedProfileRepository(http: HTTPConnection,
                                 profileCount: Int) extends ProfileRepository {

  def findBy(advertiserIdAndProfileIds: util.List[(String, String)]): util.List[Profile] =
    (
      for {
        (advertiserId, profileId) <- advertiserIdAndProfileIds.asScala.take(profileCount)
      } yield {
        http.requestProfile(profileId, advertiserId)

        val parser = new JSONParse
        val p = new Profile
        p.setFrequencyTarget(parser.populateFrequencyTarget(http.getJSONData))
        p.setDaypartTargetList(parser.populateDaypartTarget(http.getJSONData))
        p.setGeographyTarget(parser.populateGeographyTarget(http.getJSONData))
        p.setSegmentGroupTargets(parser.populateSegmentGroupTargetList(http.getJSONData))

        p
      })
      .asJava
}
