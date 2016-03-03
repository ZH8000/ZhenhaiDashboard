package code.csv

import code.model._


/**
 *  此 Singleton 物件為用來產生網站上「訂單狀態」中特定日期的記錄 CSV 檔
 */
object OrderStatusCSV {

  /**
   *  特定日期的訂單狀態的 CSV 檔
   *
   *  @param      date      維修紀錄的日期
   *  @return               該日期的維修紀錄的 CSV 檔
   */
  def apply(date:String) = {

    // 取得訂單狀態，每一筆記錄為 List 中的一個 Element
    val orderStatus = OrderStatus.findAll("shiftDate", date).sortWith(_.lotNo.get < _.lotNo.get)

    // 將每個 Element 轉換成一行 CSV 字串
    val lines = orderStatus.map { record =>

      // 條碼上的所需生產數量，為真實需求量的 1.03 倍，所以需要將其轉換成為真實的需求數量
      val requireCount = (record.inputCount.get - (record.inputCount.get * 0.03)).toLong

      s""""${record.lotNo}","${record.customer}","${record.product}",${record.inputCount},$requireCount,${record.step1},""" + 
      s"""${record.step2},${record.step3},${record.step4},${record.step5}"""
    }

    """"製令編號","客戶","規格","投入數","需求數","加締捲曲","組立","老化","選別","加工切腳"""" + "\n" +
    lines.mkString("\n")
  }

}

