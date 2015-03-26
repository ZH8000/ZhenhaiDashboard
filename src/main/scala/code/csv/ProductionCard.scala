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

        val step1DoneTime = dateFormatter.format(new Date(record.step1DoneTime.get * 1000L))
        val step2DoneTime = dateFormatter.format(new Date(record.step2DoneTime.get * 1000L))
        val step3DoneTime = dateFormatter.format(new Date(record.step3DoneTime.get * 1000L))
        val step4DoneTime = dateFormatter.format(new Date(record.step4DoneTime.get * 1000L))
        val step5DoneTime = dateFormatter.format(new Date(record.step5DoneTime.get * 1000L))

        val step1StartTime = dateFormatter.format(new Date(record.step1StartTime.get * 1000L))
        val step2StartTime = dateFormatter.format(new Date(record.step2StartTime.get * 1000L))
        val step3StartTime = dateFormatter.format(new Date(record.step3StartTime.get * 1000L))
        val step4StartTime = dateFormatter.format(new Date(record.step4StartTime.get * 1000L))
        val step5StartTime = dateFormatter.format(new Date(record.step5StartTime.get * 1000L))

         """"製令編號","料號","客戶","規格","投入數","需求數"""" + "\n" +
        s""""${record.lotNo}","${record.partNo}","${record.customer}","${record.product}",${record.inputCount},${requireCount}""" + "\n" +
         """"工程","接送日期","接送數量","機器","作業者","完成日期"""" + "\n" +
        s""""加締卷取","$step1StartTime",${record.step1}, "${record.step1MachineID}", "${record.step1WorkerID}", "${step1DoneTime}""""  + "\n" +
        s""""組立","$step2StartTime",${record.step2}, "${record.step2MachineID}", "${record.step2WorkerID}", "${step2DoneTime}""""  + "\n" +
        s""""老化","$step3StartTime",${record.step3}, "${record.step3MachineID}", "${record.step3WorkerID}", "${step3DoneTime}""""  + "\n" +
        s""""選別","$step4StartTime",${record.step4}, "${record.step4MachineID}", "${record.step4WorkerID}", "${step4DoneTime}""""  + "\n" +
        s""""選別","$step5StartTime",${record.step5}, "${record.step5MachineID}", "${record.step5WorkerID}", "${step5DoneTime}""""

      case _ => "查無資料"
    }

  }

}

