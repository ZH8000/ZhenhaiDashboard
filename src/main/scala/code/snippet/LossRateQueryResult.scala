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


/**
 *  用來顯示「損耗查詢」結果第一頁機台排名列表的 Snippet
 */
class LossRateQueryResult {

  /**
   *  用來代表 MongoDB 裡的 raw data 的資料
   *
   *  @param    machineID       機台編號
   *  @param    countQty        良品數
   *  @param    badQty          不良品數
   *  @param    lossMoney       損耗金額
   */
  case class RawData(machineID: String, countQty: Long, badQty: Long, lossMoney: BigDecimal)

  /**
   *  用來代表顯示排名結果的表格的其中一列
   *
   *  @param    machineID     機台編號
   *  @param    countQty      良品數
   *  @param    badQty        不良品數
   *  @param    lossMoney     損耗金額
   */
  case class ResultTableRow(machineID: String, countQty: Long, badQty: Long, lossRate: Double, lossMoney: BigDecimal)

  /**
   *  取得兩個以 yyyy-MM-dd 表示的日期間的
   *
   *  @param    firstDateString       第一個日期
   *  @param    secondDateString      第二個日期
   *  @return                         兩個人期中間隔了幾個日曆天
   */
  def getDaysBetween(firstDateString: String, secondDateString: String): Int = {
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val firstDate = dateFormatter.parseDateTime(firstDateString)
    val secondDate = dateFormatter.parseDateTime(secondDateString)
    Days.daysBetween(new LocalDate(secondDate), new LocalDate(firstDate)).getDays.abs
  }

  /**
   *  取得排序過後的損耗率列表
   *
   *  @param    startDate     開始日期
   *  @param    endDate       結束日期
   *  @param    machineType   機台類型（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   */
  def getSortedLossRate(startDate: String, endDate: String, machineType: String): List[ResultTableRow] = {
    val table = MongoDB.zhenhaiDB("dailyLossRate")
    
    // 查詢 shiftDate 介於 startDate 與 endDate 之間，並且
    // 其 machineType 符合設定的資料
    val rawMongoDBRecord = table.find($and("shiftDate" $gte startDate $lte endDate, "machineType" $eq machineType.toInt)).toList

    // 將 MongoDB 回傳的資料轉為 List[RawData] 以方便處理
    val rawDataList = rawMongoDBRecord.map { record =>
      val fullProductCode = Try{record.get("partNo").toString.substring(10, 15)}.getOrElse("Unknown")
      val badQty = Try{record.get("event_qty").toString.toLong}.getOrElse(0L)
      val productCost = ProductCost.getProductCost(fullProductCode).getOrElse(BigDecimal(0))
      val lossMoney = productCost * badQty
      val machineID = record.get("machineID").toString
      val countQty = Try{record.get("count_qty").toString.toLong}.getOrElse(0L)

      RawData(machineID, countQty, badQty, lossMoney)
    }

    // 將上述的資料，以 machineID 做 Grouping，產生一個 Map[machineID, List[RawData]]
    val groupedData = rawDataList.groupBy(_.machineID)

    // 將上述的資料中每一台機台的資料做加總，產生一個 List[ResultTableRow]
    val aggreatedData = groupedData.map { case (machineID, data) =>
      val totalCountQty = data.map(_.countQty).sum
      val totalBadQty = data.map(_.badQty).sum
      val lossRate = totalBadQty / (totalCountQty + totalBadQty).toDouble
      val totalLossMoney = data.map(_.lossMoney).sum
      ResultTableRow(machineID, totalCountQty, totalBadQty, lossRate, totalLossMoney)
    }

    // 最後以損耗由小到大排序
    aggreatedData.toList.sortWith(_.lossRate > _.lossRate)
  }

  /**
   *  顯示查詢結果表格的 Binding
   *
   *  @param    tableData       損耗率結果的排名
   *  @return                   用來 Binding
   */
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
      ".order *"     #> (index + 1) // zipWithIndex 是從 0 開始，因此要加 1 第一筆才會顯示「1」
    }
  }


  /**
   *  顯示查詢結果
   */
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


