package com.mediacrossing.segmenttargeting.profiles

import java.util
import scala.collection.JavaConverters._
import com.mediacrossing.dailycheckupsreport.{Profile, JSONParse}
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository
import com.mediacrossing.connections.HTTPRequest

class TruncatedProfileRepository(http: HTTPRequest,
                                 profileCount: Int) extends ProfileRepository {

  def findBy(advertiserIdAndProfileIds: util.List[(String, String)]): util.List[Profile] =
    (
      for {
        (advertiserId, profileId) <- advertiserIdAndProfileIds.asScala.take(profileCount)
      } yield {
        val json = http.getRequest("http://api.appnexus.com/profile?id=" + profileId + "&advertiser_id=" + advertiserId)

        val p = new Profile
        p.setFrequencyTarget(JSONParse.populateFrequencyTarget(json))
        p.setDaypartTargetList(JSONParse.populateDaypartTarget(json))
        p.setGeographyTarget(JSONParse.populateGeographyTarget(json))
        p.setSegmentGroupTargets(JSONParse.populateSegmentGroupTargetList(json))

        p
      })
      .asJava
}
