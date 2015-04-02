package code.json

import code.model._

import java.text.SimpleDateFormat
import java.util.Date

object MachineMaintainLogJSON {
  
  case class Record(
    workerID: String, workerName: String, machineID: String, 
    maintenanceCode: List[String], 
    startTime: String, endTime: String
  )

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def getLogs: List[Record] = Nil

  def getLogs(date: String): List[Record] = {

    var allRecords: Map[Long, Record] = Map.empty

    MachineMaintainLog.findAll("insertDate", date).foreach { rawRecord =>

      val worker = Worker.find(rawRecord.workerMongoID.get)
      val workerID = worker.map(_.workerID.get).openOr("查無此人")
      val workerName = worker.map(_.name.get).openOr("查無此人")
      val startTime = new Date(rawRecord.startTimestamp.get * 1000)
      val endTime = new Date(rawRecord.timestamp.get * 1000 + 5234500)

      if (rawRecord.startTimestamp.get != 0L) {
        def createRecord = Record(
          workerID, workerName, rawRecord.machineID.get, Nil, 
          dateFormatter.format(startTime), dateFormatter.format(endTime)
        )

        val targetRecord = allRecords.get(rawRecord.startTimestamp.get).getOrElse(createRecord)
        var updatedRecord = targetRecord.copy(maintenanceCode = rawRecord.maintenanceCode.get :: targetRecord.maintenanceCode)

        if (rawRecord.status.get == "03") {
          updatedRecord = updatedRecord.copy(endTime = dateFormatter.format(endTime))
        }

        allRecords += (rawRecord.startTimestamp.get -> updatedRecord)

      }
    }

    allRecords.values.toList
  }

}

