package code.csv

import code.model._
import code.json._

import java.text.SimpleDateFormat
import net.liftweb.util.Helpers._

/**
 *  此 Singleton 物件為用來產生網站上「今日工單」中的 CSV 檔
 */
object TodayOrderCSV {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  /**
   *  今日工單的資料的 CSV 檔
   *
   *  @return   該工單號的資料的 CSV 檔
   */
  def apply() = {

    val todayString = dateFormatter.format(today.getTime)

    // 取得今日的工單的狀態，並依照工單號排序，每一筆工單的狀態為 List 中的一個 Element
    val todayOrders = ProductionStatus.findAll("lastUpdatedShifted", todayString).sortWith(_.lotNo.get < _.lotNo.get)

    // 將上述的 List 中的每一個 Element 轉成一行 CSV 得字串
    val lines = todayOrders.map { record =>

      val orderStatusHolder = code.model.OrderStatus.find("lotNo", record.lotNo.get)
      val step1Status = ProductionStatus.getStatus(orderStatusHolder, 1, record.step1Status.get)
      val step2Status = ProductionStatus.getStatus(orderStatusHolder, 2, record.step2Status.get)
      val step3Status = ProductionStatus.getStatus(orderStatusHolder, 3, record.step3Status.get)
      val step4Status = ProductionStatus.getStatus(orderStatusHolder, 4, record.step4Status.get)
      val step5Status = ProductionStatus.getStatus(orderStatusHolder, 5, record.step5Status.get)

      s""""${record.lotNo}","${record.customer}","${record.product}","${step1Status}",""" ++
      s""""${step2Status}", "${step3Status}", "${step4Status}", "${step5Status}""""
    }

    // CSV 標頭
    val header = s""""製令編號","客戶","產品","加締捲取","組立","老化","選別","加工切角"""" + "\n" 

    // 將 lines 中的 Element 的每一個字串用 \n 連接，並加上 CSV 標頭成完一整個
    // 大的完整的 CSV 字串
    header + lines.mkString("\n")
  }

}

