package code.csv

import java.text.SimpleDateFormat

import code.model._
import code.snippet.LossRateQueryResult
import code.snippet.LossRateQueryDetial

import net.liftweb.common._

/**
 *  此物件用來產生網站上首頁「損耗率查詢」的結果的 CSV 檔
 */
object LossRate {

  /**
   *  @param    startDate     起始日期
   *  @param    endDate       結束日期
   *  @param    machineType   機器種類
   */
  def apply(startDate: String, endDate: String, machineType: String) = {
    val resultDataList = LossRateQueryResult.getSortedLossRate(startDate, endDate, machineType)

    s""""機台","良品數","不良品數","損耗率(%)","損耗金額(RMB)","損耗排名""""  + "\n" +
    resultDataList.zipWithIndex.map { case (data, index) =>
      f""""${data.machineID}",${data.countQty},${data.badQty},${data.lossRate * 100}%.2f,"${data.lossMoney}%.2f","$index""""
    }.mkString("\n")

  }

  /**
   *  @param    startDate     起始日期
   *  @param    endDate       結束日期
   *  @param    machineType   機器種類
   *  @param    machineID     機台編號
   */
  def apply(startDate: String, endDate: String, machineType: String, machineID: String): String = {

    val resultDataList = LossRateQueryDetial.getSortedLossRate(startDate, endDate, machineID)

    s""""日期","良品數","不良品數","損耗率(%)","損耗金額(RMB)""""  + "\n" +
    resultDataList.map { data =>
      f""""${data.shiftDate}",${data.countQty},${data.badQty},${data.lossRate * 100}%.2f,"${data.lossMoney}%.2f""""
    }.mkString("\n")
  }
}

