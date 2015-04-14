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

      val orderStatusHolder = code.model.OrderStatus.find("lotNo", record.lotNo.get)
      val step1Status = ProductionStatus.getStatus(orderStatusHolder, 1, record.step1Status.get)
      val step2Status = ProductionStatus.getStatus(orderStatusHolder, 2, record.step2Status.get)
      val step3Status = ProductionStatus.getStatus(orderStatusHolder, 3, record.step3Status.get)
      val step4Status = ProductionStatus.getStatus(orderStatusHolder, 4, record.step4Status.get)
      val step5Status = ProductionStatus.getStatus(orderStatusHolder, 5, record.step5Status.get)

      s""""${record.lotNo}","${record.customer}","${record.product}","${step1Status}",""" ++
      s""""${step2Status}", "${step3Status}", "${step4Status}", "${step5Status}""""
    }

    s""""製令編號","客戶","產品","加締捲取","組立","老化","選別","加工切角"""" + "\n" + lines.mkString("\n")
  }

}

