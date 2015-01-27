package code.csv

import code.model._
import code.json._

object OrderStatusCSV {

  def apply() = {
    val orderStatus = OrderStatus.findAll.sortWith(_.lotNo.get < _.lotNo.get)
    val lines = orderStatus.map { record =>
      val requireCount = (record.inputCount.get - (record.inputCount.get * 0.04)).toLong
      s""""${record.lotNo}","${record.product}",${record.inputCount},$requireCount,${record.step1},""" + 
      s"""${record.step2},${record.step3},${record.step4},${record.step5}"""
    }

    """"製令編號","規格","投入數","需求數","加締捲曲","組立","老化","選別","加工切腳"""" + "\n" +
    lines.mkString("\n")
  }

}

