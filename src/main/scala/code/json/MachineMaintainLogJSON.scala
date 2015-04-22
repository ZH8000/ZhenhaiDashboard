package code.json

import code.model._

import java.text.SimpleDateFormat
import java.util.Date

object MachineMaintainLogJSON {
  
  case class Record(
    startWorkerID: String, startWorkerName: String, 
    endWorkerID: String, endWorkerName: String,
    machineID: String, 
    maintenanceCode: List[String], 
    startTime: String, endTime: String,
    totalTime: String
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
      val endTime = new Date(rawRecord.timestamp.get * 1000)

      if (rawRecord.startTimestamp.get != 0L) {
        def createRecord = Record(
          workerID, workerName, 
          workerID, workerName,
          rawRecord.machineID.get, Nil, 
          dateFormatter.format(startTime), 
          dateFormatter.format(endTime),
          "AAAA"
        )

        val targetRecord = allRecords.get(rawRecord.startTimestamp.get).getOrElse(createRecord)
        var updatedRecord = targetRecord.copy(maintenanceCode = rawRecord.maintenanceCode.get :: targetRecord.maintenanceCode)

        if (rawRecord.status.get == "03") {
          val difference = (rawRecord.timestamp.get - rawRecord.startTimestamp.get) / 60
          val hours = difference / 60
          val minutes = difference % 60
          updatedRecord = updatedRecord.copy(
            endWorkerID = workerID,
            endWorkerName = workerName,
            endTime = dateFormatter.format(endTime),
            totalTime = "%02d:%02d".format(hours, minutes)
          )
        }

        allRecords += (rawRecord.startTimestamp.get -> updatedRecord)

      }
    }

    allRecords.values.toList
  }

}

