package code.snippet

import code.model._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import java.text.SimpleDateFormat
import scala.xml.NodeSeq

class TodayOrder {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  val order = ProductionStatus.findAll("lastUpdatedShifted", dateFormatter.format(today.getTime)).sortWith(_.lotNo.get < _.lotNo.get)

  def showEmptyBox() = {
    S.error("目前無今日工單資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  def render = {

    order.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".row" #> order.map { record =>

          val orderStatusHolder = code.model.OrderStatus.find("lotNo", record.lotNo.get)
          val step1Status = ProductionStatus.getStatus(orderStatusHolder, 1, record.step1Status.get)
          val step2Status = ProductionStatus.getStatus(orderStatusHolder, 2, record.step2Status.get)
          val step3Status = ProductionStatus.getStatus(orderStatusHolder, 3, record.step3Status.get)
          val step4Status = ProductionStatus.getStatus(orderStatusHolder, 4, record.step4Status.get)
          val step5Status = ProductionStatus.getStatus(orderStatusHolder, 5, record.step5Status.get)

          ".lotNo *" #> record.lotNo &
          ".partNo *" #> record.partNo &
          ".customer *" #> record.customer &
          ".product *" #> record.product &
          ".status *" #> record.status &
          ".step1Status *" #> step1Status &
          ".step2Status *" #> step2Status &
          ".step3Status *" #> step3Status &
          ".step4Status *" #> step4Status &
          ".step5Status *" #> step5Status
        }
    }
  }
}
