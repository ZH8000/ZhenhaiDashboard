package code.json

import code.model._

import java.text.SimpleDateFormat
import java.util.Date

object MachineMaintainLogJSON {
  
  case class Record(workerID: String, workerName: String, machineID: String, item: String, startTime: String, endTime: String)

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def getLogs = {
    MachineMaintainLog.findAll.sortWith(_.timestamp.get > _.timestamp.get).map { record =>

      val worker = Worker.find(record.workerMongoID.get)
      val workerID = worker.map(_.workerID.get).openOr("查無此人")
      val workerName = worker.map(_.name.get).openOr("查無此人")
      val startTime = new Date(record.timestamp.get * 1000)
      val endTime = new Date(record.timestamp.get * 1000 + 5234500)

      Record(workerID, workerName, record.machineID.get, record.item.get, dateFormatter.format(startTime), dateFormatter.format(endTime))
    }
  }

}

