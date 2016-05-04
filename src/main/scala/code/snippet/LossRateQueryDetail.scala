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

/**
 *  用來顯示「損耗查詢」結果第二頁機台每日結果的 Snippet
 */
class LossRateQueryDetial {

  /**
   *  用來代表 MongoDB 查詢結果的原始資料
   *
   *  @param    shiftDate     工班日期
   *  @param    countQty      良品數
   *  @param    badQty        不良品數
   *  @param    lossMoney     損耗金額
   */
  case class RawData(shiftDate: String, countQty: Long, badQty: Long, lossMoney: BigDecimal)

  /**
   *  用來代表
   *
   *  @param    shiftDate     工班日期
   *  @param    countQty      良品數
   *  @param    badQty        不良品數
   *  @param    lossRate      損耗率
   *  @param    lossMoney     損耗金額
   */
  case class ResultTableRow(shiftDate: String, countQty: Long, badQty: Long, lossRate: Double, lossMoney: BigDecimal)

  /**
   *  取得排序過後的表格資料
   *
   *  @param    startDate     開始日期
   *  @param    endDate       結束日期
   *  @param    machineID     機台編號
   *  @return                 結果列表
   */
  def getSortedLossRate(startDate: String, endDate: String, machineID: String): List[ResultTableRow] = {
    val table = MongoDB.zhenhaiDB("dailyLossRate")

    // 查詢資料表裡的 shiftDate 在 startDate 到 endDate 之間，並且 machineID 符合設定的資料
    val rawMongoDBRecord = table.find($and("shiftDate" $gte startDate $lte endDate, "machineID" $eq machineID)).toList

    // 將上述的資料轉為 List[RawData] 以方便處理
    val rawDataList = rawMongoDBRecord.map { record =>
      val shiftDate = record.get("shiftDate").toString
      val fullProductCode = Try{record.get("partNo").toString.substring(10, 15)}.getOrElse("Unknown")
      val badQty = Try{record.get("event_qty").toString.toLong}.getOrElse(0L)
      val productCost = ProductCost.getProductCost(fullProductCode).getOrElse(BigDecimal(0))
      val lossMoney = productCost * badQty
      val countQty = Try{record.get("count_qty").toString.toLong}.getOrElse(0L)
      RawData(shiftDate, countQty, badQty, lossMoney)
    }

    // 將上述資料以 shiftDate 做 grouping，成為 Map[shiftDate, List[RawData]]
    val groupedData = rawDataList.groupBy(_.shiftDate)

    // 最後將上述 Map 中每一個 shiftDate 的 Value 的 List[RawData] 列表做加總，
    // 最後轉為一個 List[ResultTableRow]，每一個 element 是一個 shiftDate
    val aggreatedData = groupedData.map { case (shiftDate, data) =>
      val totalCountQty = data.map(_.countQty).sum
      val totalBadQty = data.map(_.badQty).sum
      val lossRate = totalBadQty / (totalCountQty + totalBadQty).toDouble
      val totalLossMoney = data.map(_.lossMoney).sum
      ResultTableRow(shiftDate, totalCountQty, totalBadQty, lossRate, totalLossMoney)
    }

    // 最後以 shiftDate 做降冪排序
    aggreatedData.toList.sortWith(_.shiftDate < _.shiftDate)
  }

  /**
   *  顯示表格的 Binding
   *
   *  @param    tableData     結果表格的資料
   *  @return                 用來 Binding 的設定
   */
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

  /**
   *  顯示結果表格
   */
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


