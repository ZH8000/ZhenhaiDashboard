package code.csv

import code.json._

object MachineMaintainLogCSV {
  
  def apply() = {

    val lines = MachineMaintainLogJSON.getLogs.map { record =>
      s""""${record.workerID}","${record.workerName}","${record.machineID}","${record.maintenanceCode}"""" +
      s""""${record.startTime}","${record.endTime}""""
    }

    s""""工號","姓名","機台","維修項目","開始時間","結束時間"""" + "\n" +
    lines.mkString("\n")
  }

}

