package code.csv

import code.json._
import code.model._

object MachineMaintainLogCSV {
  
  def apply(date: String) = {

    val maintenanceCodeDescription = 
            MaintenanceCode.findAll.map(record => (record.code.get -> record.description.get)).toMap

    val lines = MachineMaintainLogJSON.getLogs(date).map { record =>

      val codeDescriptions = 
        record.maintenanceCode.map(code => maintenanceCodeDescription.get(code).getOrElse(code))
              .mkString("、")

      s""""${record.workerID}","${record.workerName}","${record.machineID}","${codeDescriptions}",""" +
      s""""${record.startTime}","${record.endTime}""""
    }

    s""""工號","姓名","機台","維修項目","開始時間","結束時間"""" + "\n" +
    lines.mkString("\n")
  }

}

