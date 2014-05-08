package com.mediacrossing.sources.appnexus.reports.buy

import com.mediacrossing.sources.appnexus.reports.{ReportRequest, ReportRequestMarshaller}
import com.mediacrossing.sources.appnexus.reports.ReportRequest.{TimeZone, Interval}

object SiteDomainPerformance {

  // TODO Ideally:
  // * All configurable properties are exposed
  // * Properties are more strongly typed
  case class Request(advertiserId: String,
                     interval: Interval = "yesterday",
                     timeZone: TimeZone = "EST5EDT")

  implicit val marshaller =
    new ReportRequestMarshaller[Request] {

      override def marshal(r: Request): (QueryString, ReportRequest) =
        s"advertiser_id=${r.advertiserId}" ->
          ReportRequest(
            reportType = "site_domain_performance",
            interval = r.interval,
            columns =
              "site_domain" ::
                "imps" ::
                "clicks" ::
                "click_thru_pct" ::
                "post_view_convs" ::
                "post_click_convs" ::
                "cpm" ::
                Nil,
            orders =
              ("imps", "DESC") ::
                Nil,
            format = "csv",
            timeZone = "EST5EDT")

    }
}
