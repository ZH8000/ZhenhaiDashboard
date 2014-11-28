package code.csv

import code.lib._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object TotalCSV {

  def overview = {
    val JArray(jsonData) = TotalJSON.overview \\ "dataSet"
    CSVConverter(List("Φ 別", "數量"), List("name", "value"), jsonData)
  }

  def apply(productName: String) = {
    val JArray(jsonData) = TotalJSON(productName) \\ "dataSet"
    CSVConverter(List("年月", "數量"), List("name", "value"), jsonData)
  }

  def apply(productName: String, year: Int, month: Int) = {
    val JArray(jsonData) = TotalJSON(productName, year, month) \\ "dataSet"
    CSVConverter(List("週別", "數量"), List("name", "value"), jsonData)
  }

  def apply(productName: String, year: Int, month: Int, week: Int) = {
    val JArray(jsonData) = TotalJSON(productName, year, month, week) \\ "dataSet"
    CSVConverter(List("日期", "數量"), List("name", "value"), jsonData)
  }

  def apply(productName: String, year: Int, month: Int, week: Int, date: Int) = {
    val JArray(jsonData) = TotalJSON(productName, year, month, week, date) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

  def apply(productName: String, year: Int, month: Int, week: Int, date: Int, machineID: String) = {
    val JArray(jsonData) = TotalJSON(productName, year, month, week, date, machineID) \\ "dataSet"

    CSVConverter(
      List("日期", "生產數量", "錯誤數量", "錯誤種類"), 
      List("timestamp", "count_qty", "bad_qty", "defact_id"), 
      machineID,
      "說明", "defact_id",
      jsonData
    )
  }

}


