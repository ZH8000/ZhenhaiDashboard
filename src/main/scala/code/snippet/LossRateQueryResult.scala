package code.snippet

import com.mongodb.casbah.Imports._
import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common._
import scala.xml.NodeSeq
import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Try
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import org.joda.time.LocalDate



class LossRateQueryResult {

  case class RawData(machineID: String, countQty: Long, badQty: Long, lossMoney: BigDecimal)
  case class ResultTableRow(machineID: String, countQty: Long, badQty: Long, lossRate: Double, lossMoney: BigDecimal)

  def getDaysBetween(firstDateString: String, secondDateString: String): Int = {
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val firstDate = dateFormatter.parseDateTime(firstDateString)
    val secondDate = dateFormatter.parseDateTime(secondDateString)
    Days.daysBetween(new LocalDate(secondDate), new LocalDate(firstDate)).getDays.abs
  }

  def getSortedLossRate(startDate: String, endDate: String, machineType: String): List[ResultTableRow] = {
    val table = MongoDB.zhenhaiDB("dailyLossRate")
    val rawMongoDBRecord = table.find($and("shiftDate" $gte startDate $lte endDate, "machineType" $eq machineType.toInt)).toList
    val rawDataList = rawMongoDBRecord.map { record =>
      val fullProductCode = Try{record.get("partNo").toString.substring(10, 15)}.getOrElse("Unknown")
      val badQty = Try{record.get("event_qty").toString.toLong}.getOrElse(0L)
      val productCost = ProductCost.getProductCost(fullProductCode).getOrElse(BigDecimal(0))
      val lossMoney = productCost * badQty
      val machineID = record.get("machineID").toString
      val countQty = Try{record.get("count_qty").toString.toLong}.getOrElse(0L)

      RawData(machineID, countQty, badQty, lossMoney)
    }
    val groupedData = rawDataList.groupBy(_.machineID)
    val aggreatedData = groupedData.map { case (machineID, data) =>
      val totalCountQty = data.map(_.countQty).sum
      val totalBadQty = data.map(_.badQty).sum
      val lossRate = totalBadQty / (totalCountQty + totalBadQty).toDouble
      val totalLossMoney = data.map(_.lossMoney).sum
      ResultTableRow(machineID, totalCountQty, totalBadQty, lossRate, totalLossMoney)
    }

    aggreatedData.toList.sortWith(_.lossRate > _.lossRate)
  }

  def showDataTable(tableData: List[ResultTableRow]) = {
    val startDate = S.param("startDate").getOrElse("")
    val endDate = S.param("endDate").getOrElse("")
    val machineType = S.param("machineType").getOrElse("")

    tableData.zipWithIndex.map { case (record, index) =>
      val lossRatePercent = (record.lossRate * 100)
      ".machineID *" #> record.machineID &
      ".machineID [href]" #> s"/lossRate/detail?machineType=$machineType&machineID=${record.machineID}&startDate=$startDate&endDate=$endDate" &
      ".countQty *"  #> record.countQty &
      ".badQty *"    #> record.badQty &
      ".lossRate *"  #> "%.02f".format(lossRatePercent) &
      ".lossMoney *" #> record.lossMoney &
      ".order *"     #> index
    }
  }


  def render = {
    val startDateHolder = S.param("startDate")
    val endDateHolder = S.param("endDate")
    val machineTypeHolder = S.param("machineType")

    val resultData = for {
      startDate   <- startDateHolder.filter(_.trim.size > 0)
      endDate     <- endDateHolder.filter(_.trim.size > 0)
      machineType <- machineTypeHolder.filter(_.trim.size > 0)
    } yield {
      if (getDaysBetween(startDate, endDate) > 31) {
        S.error("僅提供開始與結束日期為一個月內的範圍的查詢。")
        Nil
      } else {
        getSortedLossRate(startDate, endDate, machineType)
      }
    }

    resultData match {
      case Full(tableData) if tableData.size > 0 => 
        ".dataRow" #> showDataTable(tableData)
      case _ =>
        S.error("查無資料，請重新設定查詢條件。")
        ".table" #> NodeSeq.Empty
    }
  }

}


