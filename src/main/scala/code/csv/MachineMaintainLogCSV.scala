package code.csv

import code.json._
import code.model._
import code.lib._

object MachineMaintainLogCSV {
  
  def apply(date: String) = {

    val lines = MachineMaintainLogJSON.getLogs(date).map { record =>

      val machineInfoHolder = MachineInfo.idTable.get(record.machineID)
      val machineType = machineInfoHolder.map(_.machineType).getOrElse(-1)
      val codeMapping = MaintenanceCode.mapping.get(machineType).getOrElse(Map.empty[Int, String])
      val codeDescriptions = record.maintenanceCode.map(code => codeMapping.get(code.toInt).getOrElse(code)).mkString("、")

      s""""${record.machineID}","${codeDescriptions}",""" +
      s""""${record.startTime}","${record.startWorkerName}",""" +
      s""""${record.endTime}", ${record.endWorkerName}, ${record.totalTime}"""
    }

    s""""機台","維修項目","開始時間","員工","結束時間","員工","總計時間"""" + "\n" +
    lines.mkString("\n")
  }

}

