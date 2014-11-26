package code.csv

import code.util._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object DailyCSV {

  def apply(year: Int, month: Int) = {
    val JArray(jsonData) = DailyJSON(year, month) \\ "dataSet"
    CSVConverter(List("週別", "數量"), List("name", "value"), jsonData)
  }

  def apply(year: Int, month: Int, date: Int) = {
    val JArray(jsonData) = DailyJSON(year, month, date) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

  def apply(year: Int, month: Int, date: Int, machineID: String) = {
    val JArray(jsonData) = DailyJSON(year, month, date, machineID) \\ "dataSet"

    CSVConverter(
      List("日期", "生產數量", "錯誤數量", "錯誤種類"), 
      List("timestamp", "count_qty", "bad_qty", "defact_id"), 
      machineID,
      "說明", "defact_id",
      jsonData
    )
  }

}


