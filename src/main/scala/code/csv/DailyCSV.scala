package code.csv

import code.lib._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/**
 *  此 Singleton 物件為用來產生網站上「產量統計」－＞「日報表」中的 CSV 檔，
 *  其運作原理為先取得相對應的 JSON 格式的資料，然後再透過位於 code/lib 中的
 *  CSVConverter 物件將其轉換成 CSV 格式。
 */
object DailyCSV {

  /**
   * 「產量統計」－＞「日報表」－＞「年月份」該頁面的 CSV 檔
   *
   * @param     year    報表的年份
   * @param     month   報表的月份
   * @return            該頁的 CSV 檔
   */
  def apply(year: Int, month: Int) = {

    // 取得 JSON 檔，並取出該 JSON 檔中的 dataSet 欄位
    val JArray(jsonData) = DailyJSON(year, month) \\ "dataSet"

    CSVConverter(
      List("工序", "數量"),     // CSV 裡第一列的標頭
      List("name", "value"),    // CSV 每一欄對應到 jsonData 中的哪些欄位
      jsonData                  // JSON 格式的資料
    )
  }

  /**
   * 「產量統計」－＞「日報表」－＞「年月份」－＞「工序」該頁面的 CSV 檔
   *
   * @param     year    報表的年份
   * @param     month   報表的月份
   * @param     step    工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @return            該頁的 CSV 檔
   */
  def apply(year: Int, month: Int, step: String) = {
    val JArray(jsonData) = DailyJSON(year, month, step) \\ "dataSet"
    CSVConverter(List("日期", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「日報表」－＞「年月份」－＞「工序」－＞「日期」該頁面的 CSV 檔
   *
   * @param     year    報表的年份
   * @param     month   報表的月份
   * @param     step    工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param     date    日期
   * @return            該頁的 CSV 檔
   */
  def apply(year: Int, month: Int, step: String, date: Int) = {
    val JArray(jsonData) = DailyJSON(year, month, step, date) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「日報表」－＞「年月份」－＞
   * 「工序」－＞「日期」－＞「機台編號」該頁面的 CSV 檔
   *
   * @param     year        報表的年份
   * @param     month       報表的月份
   * @param     step        工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param     date        日期
   * @param     machineID   機台編號
   * @return                該頁的 CSV 檔
   */
  def apply(year: Int, month: Int, step: String, date: Int, machineID: String) = {
    val JArray(jsonData) = DailyJSON(year, month, step, date, machineID) \\ "dataSet"

    CSVConverter(
      List("日期", "生產數量", "錯誤數量", "錯誤種類"), 
      List("timestamp", "count_qty", "event_qty", "defact_id"), 
      machineID,
      "說明", "defact_id",
      jsonData
    )
  }
}
