package com.mediacrossing.sources.appnexus.reports.buy

import com.mediacrossing.goldengate.FailSafeHttpClient
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import com.mediacrossing.sources.appnexus.{PutneyClient, reports}
import com.mediacrossing.sources.appnexus.reports.{CsvRow, Deserializers}
import reports.ReportConnectivity._
import scalaz.@@

trait ConfiguredBuyReportRequests {

  def siteDomainPerformance(r: SiteDomainPerformance.Request)
                           (implicit c: @@[FailSafeHttpClient, PutneyClient],
                            ec: ExecutionContext,
                            requestTimeout: Duration): List[CsvRow] = {
    import SiteDomainPerformance.marshaller
    import Deserializers.csv

    requestReportBlocking(client = c)(request = r)
  }
}
