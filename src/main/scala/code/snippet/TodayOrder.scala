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
  val statusToDescription = Map(
    1 -> "生產中",
    2 -> "維修中",
    3 -> "維修完成",
    4 -> "生產完成",
    5 -> "鎖機中",
    6 -> "解除鎖機",
    7 -> "強制停機（已達目標數）",
    8 -> "強制停機（未達目標數）"
  )

  def showEmptyBox() = {
    S.error("目前無今日工單資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  def render = {

    order.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".row" #> order.map { record =>

          val step1Status = statusToDescription.get(record.step1Status.get).getOrElse("-")
          val step2Status = statusToDescription.get(record.step2Status.get).getOrElse("-")
          val step3Status = statusToDescription.get(record.step3Status.get).getOrElse("-")
          val step4Status = statusToDescription.get(record.step4Status.get).getOrElse("-")
          val step5Status = statusToDescription.get(record.step5Status.get).getOrElse("-")

          ".lotNo *" #> record.lotNo &
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
