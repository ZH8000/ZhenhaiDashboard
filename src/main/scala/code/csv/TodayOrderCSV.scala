package code.csv

import code.model._
import code.json._

import java.text.SimpleDateFormat
import net.liftweb.util.Helpers._

object TodayOrderCSV {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  def apply() = {
    val todayOrders = DailyOrder.findAll("timestamp", dateFormatter.format(today.getTime)).sortWith(_.lotNo.get < _.lotNo.get)
    val lines = todayOrders.map { record =>
      s""""${record.lotNo}","${record.order}","${record.product}","${record.status}""""
    }

    s""""製令編號","訂單","產品","生產狀態"""" + "\n" + lines.mkString("\n")
  }

}

