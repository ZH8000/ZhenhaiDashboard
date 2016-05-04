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

class LossRateQueryDetial {

  case class RawData(shiftDate: String, countQty: Long, badQty: Long, lossMoney: BigDecimal)
  case class ResultTableRow(shiftDate: String, countQty: Long, badQty: Long, lossRate: Double, lossMoney: BigDecimal)

  def getSortedLossRate(startDate: String, endDate: String, machineID: String): List[ResultTableRow] = {
    val table = MongoDB.zhenhaiDB("dailyLossRate")
    val rawMongoDBRecord = table.find($and("shiftDate" $gte startDate $lte endDate, "machineID" $eq machineID)).toList
    val rawDataList = rawMongoDBRecord.map { record =>
      val shiftDate = record.get("shiftDate").toString
      val fullProductCode = Try{record.get("partNo").toString.substring(10, 15)}.getOrElse("Unknown")
      val badQty = Try{record.get("event_qty").toString.toLong}.getOrElse(0L)
      val productCost = ProductCost.getProductCost(fullProductCode).getOrElse(BigDecimal(0))
      val lossMoney = productCost * badQty
      val countQty = Try{record.get("count_qty").toString.toLong}.getOrElse(0L)
      RawData(shiftDate, countQty, badQty, lossMoney)
    }
    val groupedData = rawDataList.groupBy(_.shiftDate)
    val aggreatedData = groupedData.map { case (shiftDate, data) =>
      val totalCountQty = data.map(_.countQty).sum
      val totalBadQty = data.map(_.badQty).sum
      val lossRate = totalBadQty / (totalCountQty + totalBadQty).toDouble
      val totalLossMoney = data.map(_.lossMoney).sum
      ResultTableRow(shiftDate, totalCountQty, totalBadQty, lossRate, totalLossMoney)
    }

    aggreatedData.toList.sortWith(_.shiftDate < _.shiftDate)
  }

  def showDataTable(tableData: List[ResultTableRow]) = {
    val startDate = S.param("startDate").getOrElse("")
    val endDate = S.param("endDate").getOrElse("")
    val machineType = S.param("machineType").getOrElse("")
    val machineID = S.param("machineID").getOrElse("")

    tableData.map { record =>
      val List(year, month, date) = record.shiftDate.split("-").map(_.toInt).toList
      val lossRatePercent = (record.lossRate * 100)
      ".date *" #> record.shiftDate &
      ".date [href]" #> s"/daily/$year/$month/$machineType/$date/$machineID" &
      ".countQty *"  #> record.countQty &
      ".badQty *"    #> record.badQty &
      ".lossRate *"  #> "%.02f".format(lossRatePercent) &
      ".lossMoney *" #> record.lossMoney
    }
  }

  def render = {
    val startDateHolder = S.param("startDate")
    val endDateHolder = S.param("endDate")
    val machineIDHolder = S.param("machineID")
    val machineTypeHolder = S.param("machineType")

    val resultData = for {
      startDate <- startDateHolder.filter(_.trim.size > 0)
      endDate <- endDateHolder.filter(_.trim.size > 0)
      machineTypeHolder <- machineTypeHolder.filter(_.trim.size > 0)
      machineID <- machineIDHolder.filter(_.trim.size > 0)
    } yield {
      getSortedLossRate(startDate, endDate, machineID)
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


