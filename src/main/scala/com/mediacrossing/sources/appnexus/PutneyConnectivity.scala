package com.mediacrossing.sources.appnexus

import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.goldengate.{Hosts, PutneyClientConfig, FailSafeHttpClient}
import scala.concurrent.duration.Duration
import org.slf4j.LoggerFactory
import scalaz.{Tag, @@}

sealed trait PutneyClient

sealed trait PutneyReportRequestTimeout

sealed trait PutneyReadRequestTimeout

trait PutneyConnectivity {

  val configuration: ConfigurationProperties

  implicit val putneyClient: @@[FailSafeHttpClient, PutneyClient] = {
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
        PutneyClientConfig(
          clientServiceName = "toledo-putney-client",
          hosts = Hosts(configuration.putneyHosts()),
          parallelism = 2,
          logger = Logger)))
  }

  implicit val reportRequestTimeout: @@[Duration, PutneyReportRequestTimeout] =
    Tag(configuration.putneyReportRequestTimeout)

  implicit val readRequestTimeout: @@[Duration, PutneyReadRequestTimeout] =
  Tag(configuration.putneyReportRequestTimeout)

}

class JPutneyConnectivity(c: ConfigurationProperties) extends PutneyConnectivity {

  override val configuration: ConfigurationProperties = c
}
