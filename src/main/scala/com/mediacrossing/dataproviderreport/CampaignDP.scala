package com.mediacrossing.dataproviderreport

import com.mediacrossing.dailycheckupsreport.ServingFee
import scala.collection.mutable

class CampaignDP(id: String, name: String, imps: Int, clicks: Int, cpm: Float, servingFees: mutable.Buffer[ServingFee]) {
  def getId = id
  def getName = name
  def getImps = imps
  def getClicks = clicks
  def getCpm = cpm
  def getServingFees = servingFees
}
