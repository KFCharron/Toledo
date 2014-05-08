package com.mediacrossing.sources.appnexus.reads.buy

import com.mediacrossing.sources.appnexus.reports.buy.SiteDomainPerformance
import scalaz._
import com.mediacrossing.goldengate.FailSafeHttpClient
import com.mediacrossing.sources.appnexus.{PutneyReadRequestTimeout, PutneyClient}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import com.mediacrossing.sources.appnexus.reads.ReadsConnectivity._
import com.mediacrossing.sources.appnexus.reads.Deserializers.identity

trait ConfiguredBuyReadRequest {

  def profiles(r: ProfileRequest)
              (implicit c: @@[FailSafeHttpClient, PutneyClient],
               ec: ExecutionContext,
               requestTimeout: @@[Duration, PutneyReadRequestTimeout]): String =
    blockingGet(client = c)(
      path =
        s"/an/profile?id=${r.profileId}&" +
          s"advertiser_id=${r.advertiserId}",
      deserializer = identity)
}

class JConfiguredBuyReadRequest extends ConfiguredBuyReadRequest
