package code.csv

import code.lib._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/**
 *  此 Singleton 物件為用來產生網站上「產量統計」－＞「依 φ 別」中的 CSV 檔，
 *  其運作原理為先取得相對應的 JSON 格式的資料，然後再透過位於 code/lib 中的
 *  CSVConverter 物件將其轉換成 CSV 格式。
 */
object TotalCSV {

  /**
   * 「產量統計」－＞「依 φ 別」該總覽頁面的 CSV 檔
   *
   * @return    該頁的 CSV 檔
   */
  def overview = {

    // 取得 JSON 檔，並取出該 JSON 檔中的 dataSet 欄位
    val JArray(jsonData) = TotalJSON.overview \\ "dataSet"

    CSVConverter(
      List("工序", "數量"),       // CSV 檔中的標頭
      List("name", "value"),      // CSV 中每一欄對應到哪一個 JSON 的欄位
      jsonData                    // JSON 的資料
    )
  }

  /**
   * 「產量統計」－＞「依 φ 別」－＞「工序」頁面的 CSV 檔
   *
   * @param   step    工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @return          該頁面的 CSV 檔
   */
  def apply(step: String) = {
    val JArray(jsonData) = TotalJSON(step) \\ "dataSet"
    CSVConverter(List("Φ 別", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「依 φ 別」－＞「工序」－＞「φ別」頁面的 CSV 檔
   *
   * @param   step          工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param   productName   φ 別
   * @return                該頁面的 CSV 檔
   */
  def apply(step: String, productName: String) = {
    val JArray(jsonData) = TotalJSON(step, productName) \\ "dataSet"
    CSVConverter(List("年月", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「依 φ 別」－＞「工序」－＞「φ別」－＞「年月」頁面的 CSV 檔
   *
   * @param   step          工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param   productName   φ 別
   * @param   year          年份
   * @param   month         月份
   * @return                該頁面的 CSV 檔
   */
  def apply(step: String, productName: String, year: Int, month: Int) = {
    val JArray(jsonData) = TotalJSON(step, productName, year, month) \\ "dataSet"
    CSVConverter(List("週別", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「依 φ 別」－＞「工序」－＞
   * 「φ別」－＞「年月」－＞「週」頁面的 CSV 檔
   *
   * @param   step          工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param   productName   φ 別
   * @param   year          年份
   * @param   month         月份
   * @param   week          週
   * @return                該頁面的 CSV 檔
   */
  def apply(step: String, productName: String, year: Int, month: Int, week: Int) = {
    val JArray(jsonData) = TotalJSON(step, productName, year, month, week) \\ "dataSet"
    CSVConverter(List("日期", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「依 φ 別」－＞「工序」－＞
   * 「φ別」－＞「年月」－＞「週」－＞「日期」頁面的 CSV 檔
   *
   * @param   step          工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param   productName   φ 別
   * @param   year          年份
   * @param   month         月份
   * @param   week          週
   * @param   date          日期
   * @return                該頁面的 CSV 檔
   */
  def apply(step: String, productName: String, year: Int, month: Int, week: Int, date: Int) = {
    val JArray(jsonData) = TotalJSON(step, productName, year, month, week, date) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「依 φ 別」－＞「工序」－＞
   * 「φ別」－＞「年月」－＞「週」－＞「日期」－＞「機台編號」頁面的 CSV 檔
   *
   * @param   step          工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param   productName   φ 別
   * @param   year          年份
   * @param   month         月份
   * @param   week          週
   * @param   date          日期
   * @param   machineID     機台編號
   * @return                該頁面的 CSV 檔
   */
  def apply(productName: String, year: Int, month: Int, week: Int, date: Int, machineID: String) = {
    val JArray(jsonData) = TotalJSON(productName, year, month, week, date, machineID) \\ "dataSet"

    CSVConverter(
      List("日期", "生產數量", "錯誤數量", "錯誤種類"), 
      List("timestamp", "count_qty", "event_qty", "defact_id"), 
      machineID,
      "說明", "defact_id",
      jsonData
    )
  }

}


