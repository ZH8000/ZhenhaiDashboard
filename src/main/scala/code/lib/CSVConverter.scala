package code.lib

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

object CSVConverter {

  def toCSVField(json: JValue, fieldName: String): String = {
    (json \\ fieldName) match {
      case JInt(value) => value.toString
      case value => s""""${value.values.toString}""""
    }
  }

  def apply(titles: List[String], fields: List[String], json: JValue) = {
    val JArray(dataSet) = json
    val csvTitle = titles.mkString(",")
    val csvRecords = dataSet.map { record =>
      val csvRecords = fields.map(field => toCSVField(record, field))
      csvRecords.mkString(",")
    }

    csvTitle + "\n" + csvRecords.mkString("\n")
  }

  def apply(titles: List[String], fields: List[String], machineID: String, descTitle: String, defactIDField: String, json: JValue) = {
    val JArray(dataSet) = json
    val csvTitle = (titles ++ List(descTitle)).mkString(",")
    val csvRecords = dataSet.map { record =>
      val csvRecords = fields.map(field => toCSVField(record, field))
      val defactDescription = (record \\ defactIDField).values.toString
      
      (csvRecords ++ List(s"$defactDescription")).mkString(",")
    }

    csvTitle + "\n" + csvRecords.mkString("\n")
  }

}

