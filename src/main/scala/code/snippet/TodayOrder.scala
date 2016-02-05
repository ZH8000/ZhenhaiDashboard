package code.snippet

import java.text.SimpleDateFormat
import java.util.Date

import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import scala.xml.NodeSeq

/**
 *  用來顯示網頁上的「今日工單」的 Snippet
 */
class TodayOrder {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  /**
   *  今日工單的資料
   */ 
  val order = ProductionStatus.findAll("lastUpdatedShifted", dateFormatter.format(today.getTime)).sortWith(_.lotNo.get < _.lotNo.get)

  /**
   *  顯示「目前無今日工單資料」的錯誤訊息和隱藏 HTML 中 class="dataBlock" 元素的所有子節點
   */
  def showEmptyBox() = {
    S.error("目前無今日工單資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  /**
   *  用來顯示「今日工單」的表格
   */
  def render = {

    order.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".tableDate" #> dateFormatter.format(new Date) &
        ".row" #> order.map { record =>

          val orderStatusHolder = code.model.OrderStatus.find("lotNo", record.lotNo.get)
          val step1Status = ProductionStatus.getStatus(orderStatusHolder, 1, record.step1Status.get)
          val step2Status = ProductionStatus.getStatus(orderStatusHolder, 2, record.step2Status.get)
          val step3Status = ProductionStatus.getStatus(orderStatusHolder, 3, record.step3Status.get)
          val step4Status = ProductionStatus.getStatus(orderStatusHolder, 4, record.step4Status.get)
          val step5Status = ProductionStatus.getStatus(orderStatusHolder, 5, record.step5Status.get)
          val urlEncodedLotNo = urlEncode(record.lotNo.get)
          val productionCardURL = s"/productionCard/$urlEncodedLotNo"

          ".lotNo *" #> record.lotNo &
          ".lotNo [href]" #> productionCardURL &
          ".partNo *" #> record.partNo &
          ".customer *" #> record.customer &
          ".product *" #> record.product &
          ".step1Status *" #> step1Status &
          ".step2Status *" #> step2Status &
          ".step3Status *" #> step3Status &
          ".step4Status *" #> step4Status &
          ".step5Status *" #> step5Status
        }
    }
  }
}
