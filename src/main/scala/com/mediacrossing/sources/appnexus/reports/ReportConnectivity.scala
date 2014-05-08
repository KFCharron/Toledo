package com.mediacrossing.sources.appnexus.reports

import com.mediacrossing.goldengate._
import scala.concurrent.{Await, Future, ExecutionContext}
import play.api.libs.json.Json
import scalaz.\/
import scala.concurrent.duration._

object ReportConnectivity {

  import ReportRequest.reportRequestW

  def requestReportBlocking[Req, Resp](client: FailSafeHttpClient)
                                      (request: Req)
                                      (implicit marshaller: ReportRequestMarshaller[Req],
                                       deserializer: String => \/[String, Resp],
                                       ec: ExecutionContext,
                                       requestTimeout: Duration): Resp =
    Await.result(
      awaitable =
        requestReport(client = client)(request = request),
      atMost = requestTimeout)

  def requestReport[Req, Resp](client: FailSafeHttpClient)
                              (request: Req)
                              (implicit marshaller: ReportRequestMarshaller[Req],
                               deserializer: String => \/[String, Resp],
                               ec: ExecutionContext): Future[Resp] =
    marshaller.marshal(request) match {
      case (queryString, r) =>
        client
          .write(
            method = POST,
            path = s"/an/report?$queryString",
            entity =
              Json
                .toJson(r)
                .toString)
          .map {
          case Success(_, response) =>
            deserializer(response).fold(
              error =>
                sys.error(
                  s"Unable to request $request due to deserialization error:  $error"),
              success =>
                success)

          case Redirection(status, _) =>
            sys.error(s"Unable to request $request due to HTTP 302")

          case e: HttpError =>
            sys.error(s"Unable to request $request due to $e")
        }
    }

}
