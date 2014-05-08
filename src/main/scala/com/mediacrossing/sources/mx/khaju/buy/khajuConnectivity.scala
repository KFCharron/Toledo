package com.mediacrossing.sources.mx.khaju.buy

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.goldengate._
import org.slf4j.LoggerFactory
import scalaz.{\/, @@, Tag}
import scala.concurrent.{Await, ExecutionContext, Future}
import com.mediacrossing.goldengate.Redirection
import com.mediacrossing.goldengate.BaseClientConfig
import com.mediacrossing.goldengate.Success
import scala.concurrent.duration.Duration

sealed trait KhajuClient

sealed trait KhajuRequestTimeout

trait KhajuConnectivity {

  val configuration: ConfigurationProperties

  implicit val khajuClient: @@[FailSafeHttpClient, KhajuClient] = {
    val logger = LoggerFactory.getLogger(this.getClass)

    case object Logger {
      def warn(e: Throwable): Unit = {
        logger.warn(e.getMessage, e)
      }

      def debug(s: String): Unit = {
        logger.debug(s)
      }
    }

    Tag(
      new FailSafeHttpClient(
        BaseClientConfig(
          clientServiceName = "toledo-khaju-client",
          hosts = Hosts(configuration.putneyHosts()),
          parallelism = 2,
          logger = Logger)))
  }

  implicit val requestTimeout: @@[Duration, KhajuRequestTimeout] =
    Tag(configuration.khajuRequestTimeout)

}

class JKhajuConnectivity(c: ConfigurationProperties) extends KhajuConnectivity {

  override val configuration: ConfigurationProperties = c
}

object KhajuConnectivity {

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
              s"Unable to request $path from khaju due to deserialization error:  $error"),
          success =>
            success)

      case Redirection(status, _) =>
        sys.error(s"Unable to request $path from khaju due to HTTP 302")

      case e: HttpError =>
        sys.error(s"Unable to request $path from khaju due to $e")
    }
  }
}
