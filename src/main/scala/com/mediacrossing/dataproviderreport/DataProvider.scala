package com.mediacrossing.dataproviderreport

import scala.collection.mutable

class DataProvider (name: String) {
  def getName = name

  val campaignList = mutable.Buffer[CampaignDP]()
  def getCampaignList = campaignList
  def getTotalImps = campaignList.map(_.getImps).sum
  def getTotalClicks = campaignList.map(_.getClicks).sum
  def getAverageCpm = campaignList.map(_.getCpm).sum / campaignList.size
}
