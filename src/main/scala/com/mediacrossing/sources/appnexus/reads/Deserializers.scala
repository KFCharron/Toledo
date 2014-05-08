package com.mediacrossing.sources.appnexus.reads

import scalaz.{\/-, \/}

object Deserializers {

  val identity: String => \/[String, String] =
    s => \/-(s)
}
