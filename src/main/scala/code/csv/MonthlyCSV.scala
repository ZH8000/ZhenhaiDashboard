package code.csv

import code.lib._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/**
 *  此 Singleton 物件為用來產生網站上「產量統計」－＞「月報表」中的 CSV 檔，
 *  其運作原理為先取得相對應的 JSON 格式的資料，然後再透過位於 code/lib 中的
 *  CSVConverter 物件將其轉換成 CSV 格式。
 */
object MonthlyCSV {

  /**
   * 「產量統計」－＞「月報表」－＞「年份」該頁面的 CSV 檔
   *
   * @param     year    報表的年份
   * @return            該頁的 CSV 檔
   */
  def apply(year: Int) = {

    // 取得 JSON 檔，並取出該 JSON 檔中的 dataSet 欄位
    val JArray(jsonData) = MonthlyJSON(year) \\ "dataSet"

    CSVConverter(
      List("工序", "數量"),     // CSV 檔裡第一列的標頭
      List("name", "value"),    // CSV 每一欄對應到 JSON 中的哪一個欄位
      jsonData                  // JSON 的資料
    )
  }

  /**
   * 「產量統計」－＞「月報表」－＞「年份」－＞「工序」該頁面的 CSV 檔
   *
   * @param     year    報表的年份
   * @param     step    工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @return            該頁的 CSV 檔
   */
  def apply(year: Int, step: String) = {
    val JArray(jsonData) = MonthlyJSON(year, step) \\ "dataSet"
    CSVConverter(List("月份", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「月報表」－＞「年份」－＞
   * 「工序」－＞「月份」該頁面的 CSV 檔
   *
   * @param     year    報表的年份
   * @param     step    工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param     month   報表的月份
   * @return            該頁的 CSV 檔
   */
  def apply(year: Int, step: String, month: Int) = {
    val JArray(jsonData) = MonthlyJSON(year, step, month) \\ "dataSet"
    CSVConverter(List("週別", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「月報表」－＞「年份」－＞
   * 「工序」－＞「月份」－＞「週」該頁面的 CSV 檔
   *
   * @param     year    報表的年份
   * @param     step    工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param     month   報表的月份
   * @param     week    報表的週份
   * @return            該頁的 CSV 檔
   */
  def apply(year: Int, step: String, month: Int, week: Int) = {
    val JArray(jsonData) = MonthlyJSON(year, step, month, week) \\ "dataSet"
    CSVConverter(List("日期", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「月報表」－＞「年份」－＞
   * 「工序」－＞「月份」－＞「週」－＞「日期」該頁面的 CSV 檔
   *
   * @param     year    報表的年份
   * @param     step    工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param     month   報表的月份
   * @param     week    報表的週份
   * @param     date    報表的日期
   * @return            該頁的 CSV 檔
   */
  def apply(year: Int, step: String, month: Int, week: Int, date: Int) = {
    val JArray(jsonData) = MonthlyJSON(year, step, month, week, date) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

  /**
   * 「產量統計」－＞「月報表」－＞「年份」－＞
   * 「工序」－＞「月份」－＞「週」－＞「日期」－＞
   * 「編號」該頁面的 CSV 檔
   *
   * @param     year        報表的年份
   * @param     step        工序，1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳
   * @param     month       報表的月份
   * @param     week        報表的週份
   * @param     date        報表的日期
   * @param     machineID   機台編號
   * @return                該頁的 CSV 檔
   */
  def apply(year: Int, month: Int, week: Int, date: Int, machineID: String) = {
    val JArray(jsonData) = MonthlyJSON(year, month, week, date, machineID) \\ "dataSet"

    CSVConverter(
      List("日期", "生產數量", "錯誤數量", "錯誤種類"), 
      List("timestamp", "count_qty", "event_qty", "defact_id"), 
      machineID,
      "說明", "defact_id",
      jsonData
    )
  }
}
