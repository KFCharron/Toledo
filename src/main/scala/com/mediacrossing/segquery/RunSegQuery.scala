package com.mediacrossing.segquery

import com.mediacrossing.properties.ConfigurationProperties
import org.slf4j.LoggerFactory
import com.mediacrossing.connections.{MxService, AppNexusService}
import scala.collection.JavaConversions._
import com.mediacrossing.dailycheckupsreport._
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import au.com.bytecode.opencsv.CSVReader
import java.io.FileReader
import java.util
import scala.collection.mutable

object RunSegQuery extends App {

  // Logging
  val LOG = LoggerFactory.getLogger(RunSegQuery.getClass)
  Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    def uncaughtException(t: Thread, e: Throwable) {
      LOG.error(e.getMessage, e)
    }
  })

  // Properties
  val props = new ConfigurationProperties(args)
  val fileOutputPath = props.getOutputPath
  val anConn = new AppNexusService(
    props.getPutneyUrl
  )
  val mxConn = {
    if (props.getMxUsername == null) new MxService(props.getMxUrl)
    else new MxService(props.getMxUrl, props.getMxUsername, props.getMxPassword)
  }

  // DataStore With All Campaigns
  val dataStore = new DataStore
  dataStore.setCampaignArrayList(mxConn.requestAllCampaigns())

  // ALL Campaigns
  val camps = dataStore.getCampaignArrayList
    .toList

  // Process data
  val reader: CSVReader = new CSVReader(new FileReader("/Users/charronkyle/Desktop/CampaignData.csv"))
  val csvData = reader.readAll
  csvData.remove(0)

  // FELD Advertiser IDs
  val adIds = {
    "214113" :: "186355" :: "186356" :: "186354" :: Nil
  }

  // Map[ Advertiser -> List(ProfileIds) ]
  val groupByAdvert: Map[String, List[String]] = camps
    .map(c => (c.getAdvertiserID, c.getProfileID))
    .groupBy(t => t._1)
    .map(m => (m._1, m._2.map(t => t._2)))
  // TODO Filter For FELD
  //  .filter(a => adIds.contains(a._1))

  // List Of All Profiles
  val pros: List[Profile] = groupByAdvert.map(m => {
    // Request Profiles for each Advertiser
    val json = anConn.requests.getRequest(props.getPutneyUrl + "/profile?advertiser_id=" + m._1)
    JSONParse.parseProfiles(json).toList
  })
    .flatten
    .toList

  // Attach The Profile To Each Campaign
  for {i <- 0 to pros.size - 1} {
    for {x <- 0 to camps.size - 1} {
      if (pros.get(i).getId.equals(camps.get(x).getProfileID)) camps.get(x).setProfile(pros.get(i))
    }

  }


  // Map[ CampaignId -> List(DataRows) ]
  val rowsByCamp: Map[String, List[DataRow]] = csvData.groupBy(_(1)).mapValues(_.toList.map(a => DataRow(a)))

  // List (CampaignId, List[Segments])
  val campToSegListTupleList: List[(String, List[Segment])] = dataStore.getCampaignArrayList
    // To Scala List
    .toList
    //Only Campaigns Live During Period
    .filter(c => {
    val dtf = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss'Z'")
    val start = dtf.parseDateTime(c.getStartDate)
    if (start.isAfter(new DateTime().withMonthOfYear(4).withDayOfMonth(1).withTimeAtStartOfDay())) {
      true
    } else false
  })
    .map(c => (
    c.getName + " (" + c.getId + ")", {
    c.getSegmentGroupTargetList
      .map(gtl => gtl.getSegmentArrayList)
      .flatten
      .toList
        }
      )
    )
  .map(t => (t._1, t._2.filter(s => s.getCode.contains("BRI") || s.getName.contains("Brilig"))))

//  val campSegMap: Map[String, List[Segment]] = campToSegListTupleList.toMap




//  val reportPre: mutable.Buffer[(Segment, DataRow)] = csvData
//    .map(row => DataRow(row))
//    .map(row => (campSegMap.get(row.campName), row))
//    .flatMap {
//      case (opsegments, data) => opsegments.map(s => (s,data))
//    }.flatMap {
//      case (segments, data) => segments.map(s => (s,data))
//    }
//
//  val next: Map[(Segment, String), mutable.Buffer[(Segment, DataRow)]] = reportPre.groupBy {
//      case (segment, data) => (segment, data.dateString)
//    }
//
//
//  val report = next.map {
//      case (key, data) =>
//        val agg = data.foldLeft((0, 0)) {
//          case ((totImps, totConvs), (_, row)) =>
//            (totImps + row.imps, totConvs + row.convs)
//        }
//        (key, agg)
//  }




  // A List of Tuples Containing A List of Rows to A List of Segments
  val rowsToListOfSegTupleList: List[(List[DataRow], List[Segment])] = campToSegListTupleList.map(t => (
    rowsByCamp.get(t._1).getOrElse(List[DataRow]()), t._2
    ))

  val segToListOfDataTupleList: List[(Segment, List[DataRow])] =
    rowsToListOfSegTupleList.flatMap(l => l._2.map(y => (y, l._1)))

  val segToDataListMap: Map[String, Map[String, List[(String, (Int, Int))]]] =
    segToListOfDataTupleList
      .groupBy(t => t._1.getName + " (" + t._1.getId + ")")
      .mapValues(lt => lt
            .flatMap(t => t._2)
            .map(dr => (dr.dateString, (dr.imps, dr.convs)))
            .groupBy(_._1))

  val play: Map[String, Map[String, List[(Int, Int)]]] = segToDataListMap
    .mapValues(m => m
          .mapValues(l => l
                  .map(t => t._2)))

  val play2: Map[String, Map[String, (Int, Int)]] = play.mapValues(m => m.mapValues(l => l.unzip).mapValues(tl => (tl._1.foldLeft(0)(_+_), tl._2.foldLeft(0)(_+_))))




  val output_file = "/Desktop/Brilig_Segment_Performance.csv"
  val home = System.getProperty("user.home")
  val path = (new java.io.File(home, output_file)).getAbsolutePath()
  val pw = new java.io.PrintWriter(path)

  val headers = {"Segment Name" :: "Month" :: "Imps" :: "Convs" :: Nil}.mkString(",")
  pw.println(headers)

//  report.foreach({
//    case((seg, month), (imps, convs)) =>
//      pw.println(s"${seg.getName},$month,$imps,$convs")
//  })

  play2.foreach(seg => {
    seg._2.foreach(month => {
      val row = {seg._1.replace(","," ") :: month._1 :: month._2._1.toString :: month._2._2.toString :: Nil}.mkString(",")
      pw.println(row)
    })
  })

  pw.close()

}
case class DataRow(r: Array[String]) {
  val dateString = r(0)
  val campName = r(1)
  val imps = r(2).toInt
  val convs = r(3).toInt
}

