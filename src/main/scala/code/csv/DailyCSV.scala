package code.csv

import code.lib._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object DailyCSV {

  def apply(year: Int, month: Int) = {
    val JArray(jsonData) = DailyJSON(year, month) \\ "dataSet"
    CSVConverter(List("工序", "數量"), List("name", "value"), jsonData)
  }

  def apply(year: Int, month: Int, step: String) = {
    val JArray(jsonData) = DailyJSON(year, month, step) \\ "dataSet"
    CSVConverter(List("日期", "數量"), List("name", "value"), jsonData)
  }

  def apply(year: Int, month: Int, step: String, date: Int) = {
    val JArray(jsonData) = DailyJSON(year, month, step, date) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

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


