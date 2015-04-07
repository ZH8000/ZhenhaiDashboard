package code.csv

import code.model._
import code.json._

import net.liftweb.common._
import java.util.Date
import java.text.SimpleDateFormat

object ProductionCard {

  def toDateString(dateFormatter: SimpleDateFormat, timestamp: Long) = {
    timestamp match {
      case -1 => "尚無資料"
      case _  => dateFormatter.format(new Date(timestamp * 1000L))
    }
  }

  def getWorker(mongoID: String) = {
    Worker.find(mongoID).map(worker => s"[${worker.workerID}] ${worker.name}").getOrElse("查無此人")
  }

  def apply(lotNo: String) = {
    val orderStatus = OrderStatus.find("lotNo", lotNo)


    orderStatus match {
      case Full(record) =>
        val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm")
        val requireCount = (record.inputCount.get / 1.04).toLong

        val step1DoneTime = toDateString(dateFormatter, record.step1DoneTime.get)
        val step2DoneTime = toDateString(dateFormatter, record.step2DoneTime.get)
        val step3DoneTime = toDateString(dateFormatter, record.step3DoneTime.get)
        val step4DoneTime = toDateString(dateFormatter, record.step4DoneTime.get)
        val step5DoneTime = toDateString(dateFormatter, record.step5DoneTime.get)

        val step1StartTime = toDateString(dateFormatter, record.step1StartTime.get)
        val step2StartTime = toDateString(dateFormatter, record.step2StartTime.get)
        val step3StartTime = toDateString(dateFormatter, record.step3StartTime.get)
        val step4StartTime = toDateString(dateFormatter, record.step4StartTime.get)
        val step5StartTime = toDateString(dateFormatter, record.step5StartTime.get)

        val step1Worker = getWorker(record.step1workerID.get)
        val step2Worker = getWorker(record.step2workerID.get)
        val step3Worker = getWorker(record.step3workerID.get)
        val step4Worker = getWorker(record.step4workerID.get)
        val step5Worker = getWorker(record.step5workerID.get)

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

