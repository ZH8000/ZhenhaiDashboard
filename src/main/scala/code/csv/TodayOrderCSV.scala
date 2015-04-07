package code.csv

import code.model._
import code.json._

import java.text.SimpleDateFormat
import net.liftweb.util.Helpers._

object TodayOrderCSV {

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  def apply() = {
    val todayString = dateFormatter.format(today.getTime)
    val todayOrders = ProductionStatus.findAll("lastUpdatedShifted", todayString).sortWith(_.lotNo.get < _.lotNo.get)
    val lines = todayOrders.map { record =>
      s""""${record.lotNo}","${record.customer}","${record.product}","${record.status}""""
    }

    s""""製令編號","客戶","產品","生產狀態"""" + "\n" + lines.mkString("\n")
  }

}

