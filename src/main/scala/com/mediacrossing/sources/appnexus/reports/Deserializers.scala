package com.mediacrossing.sources.appnexus.reports

import scalaz.{\/-, \/}

object Deserializers {

  implicit val csv: String => \/[String, List[CsvRow]] =
    s =>
      \/-(
        for (row <- s.split("\n").toList) yield
          row.split(","))
}
