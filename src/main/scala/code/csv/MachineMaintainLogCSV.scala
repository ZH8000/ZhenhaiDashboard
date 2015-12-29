package code.csv

import code.json._
import code.lib._
import code.model._

/**
 *  此 Singleton 物件為用來產生網站上「維修紀錄」中特定日期的記錄 CSV 檔
 */
object MachineMaintainLogCSV {
  
  /**
   *  特定日期的維修紀錄的 CSV 檔
   *
   *  @param      date      維修紀錄的日期
   *  @return               該日期的維修紀錄的 CSV 檔
   */
  def apply(date: String) = {

    // 取得維修紀錄，每一筆記錄為 List 中的一個 Element
    val logs: List[MachineMaintainLogJSON.Record] = MachineMaintainLogJSON.getLogs(date)

    // 將每個 Element 轉換成一行 CSV 字串
    val lines = logs.map { record =>

      val machineInfoHolder = MachineInfo.idTable.get(record.machineID)
      val machineType = machineInfoHolder.map(_.machineType).getOrElse(-1)
      val codeMapping = MaintenanceCode.mapping.get(machineType).getOrElse(Map.empty[Int, String])
      val codeDescriptions = record.maintenanceCode.map(code => codeMapping.get(code.toInt).getOrElse(code)).mkString("、")

      s""""${record.machineID}","${codeDescriptions}",""" +
      s""""${record.startTime}","${record.startWorkerName}",""" +
      s""""${record.endTime}", ${record.endWorkerName}, ${record.totalTime}"""
    }

    // CSV 的標頭，加上將上述 lines 中的每一個 Element 的字串用 \n 連接起來，
    // 然後合成一個完整的 CSV 檔案的字串
    val header = s""""機台","維修項目","開始時間","員工","結束時間","員工","總計時間"""" + "\n"
    header + lines.mkString("\n")
  }

}

