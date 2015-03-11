package code.csv

import code.lib._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object CapacityCSV {

  def overview = {
    val JArray(jsonData) = CapacityJSON.overview \\ "dataSet"
    CSVConverter(List("工序", "數量"), List("name", "value"), jsonData)
  }

  def apply(step: String) = {
    val JArray(jsonData) = CapacityJSON(step) \\ "dataSet"
    CSVConverter(List("容量", "數量"), List("name", "value"), jsonData)
  }

  def apply(step: String, capacity: String) = {
    val JArray(jsonData) = CapacityJSON(step, capacity) \\ "dataSet"
    CSVConverter(List("年月", "數量"), List("name", "value"), jsonData)
  }

  def apply(step: String, capacity: String, year: Int, month: Int) = {
    val JArray(jsonData) = CapacityJSON(step, capacity, year, month) \\ "dataSet"
    CSVConverter(List("週別", "數量"), List("name", "value"), jsonData)
  }

  def apply(step: String, capacity: String, year: Int, month: Int, week: Int) = {
    val JArray(jsonData) = CapacityJSON(step, capacity, year, month, week) \\ "dataSet"
    CSVConverter(List("日期", "數量"), List("name", "value"), jsonData)
  }

  def apply(step: String, capacity: String, year: Int, month: Int, week: Int, date: Int) = {
    val JArray(jsonData) = CapacityJSON(step, capacity, year, month, week, date) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

  def apply(step: String, capacity: String, year: Int, month: Int, week: Int, date: Int, machineID: String) = {
    val JArray(jsonData) = CapacityJSON(step, capacity, year, month, week, date, machineID) \\ "dataSet"

    CSVConverter(
      List("日期", "生產數量", "錯誤數量", "錯誤種類"), 
      List("timestamp", "count_qty", "event_qty", "defact_id"), 
      machineID,
      "說明", "defact_id",
      jsonData
    )
  }

}

