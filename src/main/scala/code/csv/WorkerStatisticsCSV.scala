package code.csv

import code.model._
import code.json._

object WorkerStatisticsCSV {

  def overview = {

    val records = WorkerStatisticsJSON()
    val lines = records.map { record => s""""${record.workerID}","${record.name}",${record.countQty}""" }.mkString("\n")
    """"工號","姓名","產量"""" + "\n" + lines
  }

  def apply(workerMongoID: String) = {
    val records = WorkerStatisticsJSON(workerMongoID)
    val lines = records.map { record => s""""${record.title}",${record.countQty}""" }.mkString("\n")
    """"月份","產量"""" + "\n" + lines
  }

  def apply(workerMongoID: String, yearAndMonth: String) = {
    val records = WorkerStatisticsJSON(workerMongoID, yearAndMonth)
    val lines = records.map { record => s""""${record.title}",${record.countQty}""" }.mkString("\n")
    """"第幾週","產量"""" + "\n" + lines
  }

  def apply(workerMongoID: String, yearAndMonth: String, week: String) = {
    val records = WorkerStatisticsJSON(workerMongoID, yearAndMonth, week)
    val lines = records.map { record => s""""${record.title}",${record.countQty}""" }.mkString("\n")
    """"日期","產量"""" + "\n" + lines
  }

  def apply(workerMongoID: String, yearAndMonth: String, week: String, date: String) = {
    val records = WorkerStatisticsJSON(workerMongoID, yearAndMonth, week, date: String)
    val lines = records.map { record => s""""${record.title}",${record.countQty}""" }.mkString("\n")
    """"機台","產量"""" + "\n" + lines
  }


}
