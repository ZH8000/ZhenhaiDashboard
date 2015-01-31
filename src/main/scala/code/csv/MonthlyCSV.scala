package code.csv

import code.lib._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object MonthlyCSV {

  def apply(year: Int) = {
    val JArray(jsonData) = MonthlyJSON(year) \\ "dataSet"
    CSVConverter(List("工序", "數量"), List("name", "value"), jsonData)
  }

  def apply(year: Int, step: String) = {
    val JArray(jsonData) = MonthlyJSON(year, step) \\ "dataSet"
    CSVConverter(List("月份", "數量"), List("name", "value"), jsonData)
  }

  def apply(year: Int, step: String, month: Int) = {
    val JArray(jsonData) = MonthlyJSON(year, step, month) \\ "dataSet"
    CSVConverter(List("週別", "數量"), List("name", "value"), jsonData)
  }

  def apply(year: Int, step: String, month: Int, week: Int) = {
    val JArray(jsonData) = MonthlyJSON(year, step, month, week) \\ "dataSet"
    CSVConverter(List("日期", "數量"), List("name", "value"), jsonData)
  }

  def apply(year: Int, step: String, month: Int, week: Int, date: Int) = {
    val JArray(jsonData) = MonthlyJSON(year, step, month, week, date) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

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


