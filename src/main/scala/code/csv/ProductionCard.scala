package code.csv

import java.text.SimpleDateFormat

import code.model._
import net.liftweb.common._

/**
 *  此 Singleton 物件為用來產生網站上「生產管理卡」中工單號的資料的 CSV 檔
 */
object ProductionCard {

  /**
   *  特定工單的生產管理卡的 CSV 檔
   *
   *  @param      lotNo    工單號
   *  @return              該工單號的生產管理卡的 CSV 檔
   */
  def apply(lotNo: String) = {
    val orderStatus = OrderStatus.find("lotNo", lotNo)

    orderStatus match {
      case Full(record) =>

        val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")


        val requireCount = (record.inputCount.get / 1.04).toLong

        // 完成日期
        val step1DoneTime = record.step1DoneTimeString      // 加締
        val step2DoneTime = record.step2DoneTimeString      // 組立
        val step3DoneTime = record.step3DoneTimeString      // 老化
        val step4DoneTime = record.step4DoneTimeString      // 選別
        val step5DoneTime = record.step5DoneTimeString      // 加工切腳

        // 接送日期
        val step1StartTime = record.step1StartTimeString    // 加締
        val step2StartTime = record.step2StartTimeString    // 組立
        val step3StartTime = record.step3StartTimeString    // 老化
        val step4StartTime = record.step4StartTimeString    // 選別
        val step5StartTime = record.step5StartTimeString    // 加工切腳
  
        // 作業者
        val step1Worker = record.step1WorkerName            // 加締 
        val step2Worker = record.step2WorkerName            // 組立
        val step3Worker = record.step3WorkerName            // 老化
        val step4Worker = record.step4WorkerName            // 選別
        val step5Worker = record.step5WorkerName            // 加工切腳

        """"製令編號","料號","客戶","規格","投入數","需求數"""" + "\n" +
        s""""${record.lotNo}","${record.partNo}","${record.customer}","${record.product}",${record.inputCount},${requireCount}""" + "\n" +
         """"工程","接送日期","接送數量","機器","作業者","完成日期"""" + "\n" +
        s""""加締卷取","$step1StartTime",${record.step1}, "${record.step1machineID}", "${step1Worker}", "${step1DoneTime}""""  + "\n" +
        s""""組立","$step2StartTime",${record.step2}, "${record.step2machineID}", "${step2Worker}", "${step2DoneTime}""""  + "\n" +
        s""""老化","$step3StartTime",${record.step3}, "${record.step3machineID}", "${step3Worker}", "${step3DoneTime}""""  + "\n" +
        s""""選別","$step4StartTime",${record.step4}, "${record.step4machineID}", "${step4Worker}", "${step4DoneTime}""""  + "\n" +
        s""""加工","$step5StartTime",${record.step5}, "${record.step5machineID}", "${step5Worker}", "${step5DoneTime}""""

      case _ => "查無資料"
    }

  }

}

