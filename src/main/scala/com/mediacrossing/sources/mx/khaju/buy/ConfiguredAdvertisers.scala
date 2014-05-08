package com.mediacrossing.sources.mx.khaju.buy

import com.mediacrossing.campaignbooks.{DataParse, Advertiser}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import com.mediacrossing.goldengate._
import KhajuConnectivity.blockingGet
import scalaz.{\/-, @@}

trait ConfiguredAdvertisers {

  def all(implicit client: @@[FailSafeHttpClient, KhajuClient],
          ec: ExecutionContext,
          requestTimeout: Duration): java.util.List[Advertiser] =
    blockingGet(client = client)(
      path = "/api/catalog/advertisers",
      deserializer =
        resp =>
          \/-(DataParse.populateAdvertiserList(resp)))
}

class JConfiguredAdvertisers extends ConfiguredAdvertisers
