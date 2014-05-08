package com.mediacrossing.sources.appnexus.reports

import ReportRequest._
import play.api.libs.json.Writes

case class ReportRequest(reportType: ReportType,
                         interval: Interval,
                         columns: List[ReportColumn],
                         orders: List[(OrderBy, Direction)],
                         format: ReportFormat,
                         timeZone: TimeZone)

object ReportRequest {

  type OrderBy = String

  type ReportFormat = String

  type Direction = String

  type TimeZone = String

  type Interval = String

  type ReportColumn = String

  type ReportType = String

  private val ordersW: Writes[(OrderBy, Direction)] = {
    import play.api.libs._
    import json._
    import functional.syntax._
    import Writes._
    (
      (__ \ "order_by").write[String] ~
        (__ \ "direction").write[String])
      .tupled
  }

  implicit val reportRequestW: Writes[ReportRequest] = {
    import play.api.libs._
    import json._
    import functional.syntax._
    import Writes._

    (__ \ "report").write(
      (
        (__ \ "report_type").write[ReportType] ~
          (__ \ "report_interval").write[Interval] ~
          (__ \ "columns").write(list[ReportColumn]) ~
          (__ \ "orders").write(list(ordersW)) ~
          (__ \ "format").write[ReportFormat] ~
          (__ \ "timezone").write[TimeZone])
        .apply(unlift(ReportRequest.unapply)))
  }
}
