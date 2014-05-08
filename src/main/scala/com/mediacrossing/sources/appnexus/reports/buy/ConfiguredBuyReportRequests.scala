package com.mediacrossing.sources.appnexus.reports.buy

import com.mediacrossing.goldengate.FailSafeHttpClient
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import com.mediacrossing.sources.appnexus.{PutneyReportRequestTimeout, PutneyClient, reports}
import com.mediacrossing.sources.appnexus.reports.{CsvRow, Deserializers}
import reports.ReportConnectivity._
import scalaz.@@

trait ConfiguredBuyReportRequests {

  def siteDomainPerformance(r: SiteDomainPerformance.Request)
                           (implicit c: @@[FailSafeHttpClient, PutneyClient],
                            ec: ExecutionContext,
                            requestTimeout: @@[Duration, PutneyReportRequestTimeout]): List[CsvRow] = {
    requestReportBlocking(client = c)(
      request = r,
      deserializer = Deserializers.csv,
      marshaller = SiteDomainPerformance.marshaller)
  }
}

class JConfiguredBuyReportRequests extends ConfiguredBuyReportRequests
