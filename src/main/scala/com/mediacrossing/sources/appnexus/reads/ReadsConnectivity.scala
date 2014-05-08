package com.mediacrossing.sources.appnexus.reads

import com.mediacrossing.goldengate._
import scalaz.\/
import scala.concurrent.{Future, Await, ExecutionContext}
import scala.concurrent.duration.Duration
import com.mediacrossing.goldengate.Redirection
import com.mediacrossing.goldengate.Success

object ReadsConnectivity {

  type UrlPath = String

  def blockingGet[Resp](client: FailSafeHttpClient)
                       (path: UrlPath,
                        deserializer: String => \/[String, Resp])
                       (implicit ec: ExecutionContext,
                        requestTimeout: Duration): Resp =
    Await.result(
      awaitable =
        get(client = client)(
          path = path,
          deserializer = deserializer),
      atMost = requestTimeout)

  def get[Resp](client: FailSafeHttpClient)
               (path: UrlPath,
                deserializer: String => \/[String, Resp])
               (implicit ec: ExecutionContext): Future[Resp] = {
    client
      .read(
        method = GET,
        path = path)
      .map {
      case Success(_, response) =>
        deserializer(response).fold(
          error =>
            sys.error(
              s"Unable to request $path from putney due to deserialization error:  $error"),
          success =>
            success)

      case Redirection(status, _) =>
        sys.error(s"Unable to request $path from putney due to HTTP 302")

      case e: HttpError =>
        sys.error(s"Unable to request $path from putney due to $e")
    }
  }
}
