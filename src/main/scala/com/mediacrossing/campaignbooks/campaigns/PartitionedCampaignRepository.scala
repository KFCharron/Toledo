package com.mediacrossing.campaignbooks.campaigns

import com.mediacrossing.segmenttargeting.HTTPRequest
import scala.concurrent.duration.Duration
import java.util
import com.mediacrossing.campaignbooks.{DataParse, Campaign}
import scala.collection.JavaConverters._


class PartitionedCampaignRepository(http: HTTPRequest,
                                    partitionSize: Int,
                                    requestDelay: Duration) extends CampaignRepository {

  def findBy(campaignIds: util.List[String]): util.List[Float] =
    (
      for {
        partition <- campaignIds.asScala.grouped(partitionSize)
      } yield {
        Thread.sleep(requestDelay.toMillis)
        for {
            campaignId <- partition
        } yield {
          http.requestCampaign(campaignId)

          val parser = new DataParse
          val c = parser.addToDailyDeliveryArray(http.getJSONData)

          c
        }
      })
      .toList
      .flatten
      .asJava

}
