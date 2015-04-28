package code.csv

import code.model._
import code.json._

import net.liftweb.common._
import java.util.Date
import java.text.SimpleDateFormat

object ProductionCard {

  def apply(lotNo: String) = {
    val orderStatus = OrderStatus.find("lotNo", lotNo)

    orderStatus match {
      case Full(record) =>
        val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")
        val requireCount = (record.inputCount.get / 1.04).toLong

        val step1DoneTime = record.step1DoneTimeString
        val step2DoneTime = record.step2DoneTimeString
        val step3DoneTime = record.step3DoneTimeString
        val step4DoneTime = record.step4DoneTimeString
        val step5DoneTime = record.step5DoneTimeString

        val step1StartTime = record.step1StartTimeString
        val step2StartTime = record.step2StartTimeString
        val step3StartTime = record.step3StartTimeString
        val step4StartTime = record.step4StartTimeString
        val step5StartTime = record.step5StartTimeString

        val step1Worker = record.step1WorkerName
        val step2Worker = record.step2WorkerName
        val step3Worker = record.step3WorkerName
        val step4Worker = record.step4WorkerName
        val step5Worker = record.step5WorkerName

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

