package com.mediacrossing.dataproviderreport

case class DataProvider (name: String, campaignList: List[CampaignDP]) {
  def totalImps = campaignList.map(_.imps).sum
  def totalClicks = campaignList.map(_.clicks).sum
  def averageCpm = campaignList.map(_.cpm).sum / campaignList.size
}
