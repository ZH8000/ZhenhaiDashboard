package code.snippet

import java.text.SimpleDateFormat

import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import scala.xml.NodeSeq

/**
 *  用來顯示「今日工單」狀態的歷史記錄
 */
class ProductionStatusHistory {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  val customers = Customer.getCustomers

  /**
   *  從 URL 取出的查詢日期（工班日期）
   */
  val Array(_, shiftDate) = S.uri.drop(1).split("/")

  /**
   *  此工班日期中的工單狀態
   */
  val order = ProductionStatusHistory.findAll("shiftDate", shiftDate).sortWith(_.lotNo.get < _.lotNo.get)

  /**
   *  顯示「無此日工單資料」的錯誤訊息並隱藏 HTML 中 class="dataBlock" 的所有子節點
   */
  def showEmptyBox() = {
    S.error("無此日工單資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  /**
   *  顯示當天的工單狀態歷史紀錄
   */
  def render = {

    order.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".tableDate" #> shiftDate &
        ".row" #> order.map { record =>

          val orderStatusHolder = code.model.OrderStatus.find("lotNo", record.lotNo.get)
          val step1Status = ProductionStatus.getStatus(orderStatusHolder, 1, record.step1Status.get)
          val step2Status = ProductionStatus.getStatus(orderStatusHolder, 2, record.step2Status.get)
          val step3Status = ProductionStatus.getStatus(orderStatusHolder, 3, record.step3Status.get)
          val step4Status = ProductionStatus.getStatus(orderStatusHolder, 4, record.step4Status.get)
          val step5Status = ProductionStatus.getStatus(orderStatusHolder, 5, record.step5Status.get)
          val lotNoEncoded = urlEncode(record.lotNo.get)
          val productionCardURL = s"/productionCard/$lotNoEncoded"

          ".lotNo [href]" #> productionCardURL &
          ".lotNo *" #> record.lotNo &
          ".partNo *" #> record.partNo &
          ".customer *" #> customers.get(record.customerCode).getOrElse("Unknown") &
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
