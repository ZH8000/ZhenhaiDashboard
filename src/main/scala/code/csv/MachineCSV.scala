package code.csv

import code.util._
import code.json._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

object MachineCSV {

  def overview = {
    val JArray(jsonData) = MachineJSON.overview \\ "dataSet"
    CSVConverter(List("製程", "數量"), List("name", "value"), jsonData)
  }

  def apply(machineType: String) = {
    val JArray(jsonData) = MachineJSON(machineType) \\ "dataSet"
    CSVConverter(List("機種", "數量"), List("name", "value"), jsonData)
  }

  def apply(machineType: String, machineModel: String) = {
    val JArray(jsonData) = MachineJSON(machineType, machineModel) \\ "dataSet"
    CSVConverter(List("機台", "數量"), List("name", "value"), jsonData)
  }

  def apply(machineType: String, machineModel: String, machineID: String) = {

    val JArray(jsonData) = MachineJSON.detailTable(machineID)

    CSVConverter(
      List("日期", "事件數量", "事件種類"), 
      List("time", "bad_qty", "defact_id"), 
      machineID,
      "說明", "defact_id",
      jsonData
    )

  }

}
