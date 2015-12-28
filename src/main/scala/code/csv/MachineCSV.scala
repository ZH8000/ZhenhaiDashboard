package code.csv

import code.lib._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/**
 *  此 Singleton 物件為用來產生網站上「錯誤分析」中的 CSV 檔，
 *  其運作原理為先取得相對應的 JSON 格式的資料，然後再透過位於 code/lib 中的
 *  CSVConverter 物件將其轉換成 CSV 格式。
 */
object MachineCSV {

  /**
   * 「錯誤分析」的總覽頁面的 CSV 檔
   *
   * @return            該頁的 CSV 檔
   */
  def overview = {

    // 取得 JSON 檔，並取出該 JSON 檔中的 dataSet 欄位
    val JArray(jsonData) = MachineJSON.overview \\ "dataSet"

    CSVConverter(
      List("製程", "數量"),       // CSV 檔中第一列的標頭
      List("name", "value"),      // CSV 檔中每一欄對應到 JSON 中的哪個欄位
      jsonData                    // JSON 格式的資料
    )
  }

  /**
   * 「錯誤分析」－＞「製程」的頁面的 CSV 檔
   *
   * @param     machineType   製程名稱
   * @return                  該頁的 CSV 檔
   */
  def apply(machineType: String) = {
    val JArray(jsonData) = MachineJSON(machineType) \\ "dataSet"
    CSVConverter(List("機種", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「錯誤分析」－＞「製程」－＞「機型」的頁面的 CSV 檔
   *
   * @param     machineType   製程名稱
   * @param     machineModel  機型
   * @return                  該頁的 CSV 檔
   */
  def apply(machineType: String, machineModel: String) = {
    val JArray(jsonData) = MachineJSON(machineType, machineModel) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「錯誤分析」－＞「製程」－＞「機型」－＞「機台編號」的頁面的 CSV 檔
   *
   * @param     machineType   製程名稱
   * @param     machineModel  機型
   * @param     machineID     機台編號
   * @return                  該頁的 CSV 檔
   */
  def apply(machineType: String, machineModel: String, machineID: String) = {

    val JArray(jsonData) = MachineJSON.detailTable(machineID)

    CSVConverter(
      List("日期", "事件數量", "事件種類"), 
      List("time", "event_qty", "defact_id"), 
      machineID,
      "說明", "defact_id",
      jsonData
    )

  }

}
