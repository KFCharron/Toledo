package com.mediacrossing.sources.appnexus.reports

trait ReportRequestMarshaller[R] {

  type QueryString = String

  def marshal(r: R): (QueryString, ReportRequest)
}
