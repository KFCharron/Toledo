package com.mediacrossing.publishersummary

import org.slf4j.LoggerFactory
import com.mediacrossing.properties.ConfigurationProperties
import com.mediacrossing.connections.{MxService, AppNexusService}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scala.collection.JavaConversions._
import scala.collection.mutable


object RunPublisherSummary extends App {

  val LOG = LoggerFactory.getLogger(RunPublisherSummary.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  val props = new ConfigurationProperties(args)
  val anConn = new AppNexusService(
    props.getPutneyUrl)
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }

  val pubR = (
    (__ \ "id").read[String] ~
      (__ \ "name").read[String] ~
      (__ \ "status").read[String]
    ).apply(PubJson.apply _)

  val jList = Json.parse(mxConn.requestAllPublisherJson)
    .validate(list(pubR))
    //check for jsSuccess
    .get.filter(p => p.status == "active")

  val dayPubs = for {p <- jList} yield {
    val data = anConn.getPublisherSummary("yesterday", p.id).tail
    val kept = intTypeFilter("Kept", data, 3)
    val resold = intTypeFilter("Resold", data, 3)
    val default = intTypeFilter("Default", data, 3)
    val psa = intTypeFilter("PSA Default Error", data, 3)
    val revKept = floatTypeFilter("Kept", data, 4)
    val revResold = floatTypeFilter("Resold", data, 4)
    val rpmKept = floatTypeFilter("Kept", data, 5)
    val rpmResold = floatTypeFilter("Resold", data, 5)
    val netProfit = data.foldLeft(0.0f)(_ + _(6).toFloat)
    //val pnl = netProfit - (revResold - )
  }

  def intTypeFilter(imp: String, data: mutable.Buffer[Array[String]], index: Int) : Int = {
    if (data.filter(d => d(2).equals(imp)).size == 0) 0
    else data.filter(d => d(2).equals(imp)).head(index).toInt
  }
  def floatTypeFilter(imp: String, data: mutable.Buffer[Array[String]], index: Int) : Float = {
    if (data.filter(d => d(2).equals(imp)).size == 0) 0
    else data.filter(d => d(2).equals(imp)).head(index).toFloat
  }

}
case class Publisher(name: String,
                      id: String,
                      imps: Integer,
                      kept: Integer,
                      resold: Integer,
                      default: Integer,
                      psa: Integer,
                      revKept: Float,
                      revResold: Float,
                      rpmKept: Float,
                      rpmResold: Float,
                      pnl: Float)
case class PubJson(id: String,
                    name: String,
                    status: String)
