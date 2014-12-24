package code.snippet

import code.model._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import java.text.SimpleDateFormat
import scala.xml.NodeSeq

class TodayOrder {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  val order = DailyOrder.findAll("timestamp", dateFormatter.format(today.getTime)).sortWith(_.lotNo.get < _.lotNo.get)

  def showEmptyBox() = {
    S.error("目前無今日工單資料")
    ".dataBlock" #> NodeSeq.Empty
  }

  def render = {

    order.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".row" #> order.map { record =>
          ".lotNo *" #> record.lotNo &
          ".customer *" #> record.customer &
          ".product *" #> record.product &
          ".status *" #> record.status
        }
    }
  }
}
