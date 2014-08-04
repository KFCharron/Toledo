package com.mediacrossing.segmenttargeting.profiles

import java.util
import scala.collection.JavaConverters._
import java.io.IOException
import org.slf4j.LoggerFactory
import com.mediacrossing.dailycheckupsreport.{Segment, Profile, JSONParse}
import com.mediacrossing.dailycheckupsreport.profiles.ProfileRepository
import com.mediacrossing.connections.HTTPRequest

class PutneyProfileRepository(http: HTTPRequest) extends ProfileRepository {

  private val log = LoggerFactory.getLogger(classOf[HTTPRequest])

  def findBy(advertiserIdAndProfileIds: util.List[(String, String)], putneyUrl: String): util.List[Profile] =
    (
      for {
        (advertiserId) <- advertiserIdAndProfileIds.asScala.groupBy{
          case(adId, proId) => adId}.map(m => m._1)
      } yield {
        // FIXME
        val profileId = ""
        var success = false
        var requestCount = 1
        var json = ""
        while (!success) {
          try {
            json = http.getRequest(putneyUrl + "/profile?advertiser_id=" + advertiserId)
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
              if (requestCount > 9) System.exit(1)
          }
        }

        JSONParse.parseProfiles(json).asScala
      })
      .toList
      .flatten
      .asJava
}