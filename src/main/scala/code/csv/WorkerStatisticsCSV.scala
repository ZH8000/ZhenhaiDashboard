package code.csv

import code.model._
import code.json._

/**
 *  此 Singleton 物件為用來產生網站上「依人員」中的 CSV 檔，
 *  其運作原理為先取得相對應的 JSON 格式的資料，然後再透過位於 code/lib 中的
 *  CSVConverter 物件將其轉換成 CSV 格式。
 */
object WorkerStatisticsCSV {

  /**
   * 「依人員」該總覽頁面的 CSV 檔
   *
   * @return    該頁的 CSV 檔
   */
  def overview = {

    val records = WorkerStatistics()
    val lines = records.map { record => s""""${record.workerID}","${record.name}",${record.countQty}""" }.mkString("\n")
    """"工號","姓名","產量"""" + "\n" + lines
  }

  /**
   * 「依人員」－＞「員工」該總覽頁面的 CSV 檔
   *
   * @param   wokerMongoID    員工在 MongoDB 中的主鍵編號
   * @return                  該頁的 CSV 檔
   */
  def apply(workerMongoID: String) = {
    val records = WorkerStatistics(workerMongoID)
    val lines = records.map { record => s""""${record.title}",${record.countQty}""" }.mkString("\n")
    """"月份","產量"""" + "\n" + lines
  }

  /**
   * 「依人員」－＞「員工」－＞「年月」該總覽頁面的 CSV 檔
   *
   * @param   wokerMongoID    員工在 MongoDB 中的主鍵編號
   * @param   yearAndMonth    年月份
   * @return                  該頁的 CSV 檔
   */
  def apply(workerMongoID: String, yearAndMonth: String) = {
    val records = WorkerStatistics(workerMongoID, yearAndMonth)
    val lines = records.map { record => s""""${record.title}",${record.countQty}""" }.mkString("\n")
    """"第幾週","產量"""" + "\n" + lines
  }

  /**
   * 「依人員」－＞「員工」－＞「年月」－＞「週」該總覽頁面的 CSV 檔
   *
   * @param   wokerMongoID    員工在 MongoDB 中的主鍵編號
   * @param   yearAndMonth    年月份
   * @param   week            週
   * @return                  該頁的 CSV 檔
   */
  def apply(workerMongoID: String, yearAndMonth: String, week: String) = {
    val records = WorkerStatistics(workerMongoID, yearAndMonth, week)
    val lines = records.map { record => s""""${record.title}",${record.countQty}""" }.mkString("\n")
    """"日期","產量"""" + "\n" + lines
  }

  /**
   * 「依人員」－＞「員工」－＞「年月」－＞「週」－＞「日期」該總覽頁面的 CSV 檔
   *
   * @param   wokerMongoID    員工在 MongoDB 中的主鍵編號
   * @param   yearAndMonth    年月份
   * @param   week            週
   * @param   date            日期
   * @return                  該頁的 CSV 檔
   */
  def apply(workerMongoID: String, yearAndMonth: String, week: String, date: String) = {
    val records = WorkerStatistics(workerMongoID, yearAndMonth, week, date: String)
    val lines = records.map { record => s""""${record.title}",${record.countQty}""" }.mkString("\n")
    """"機台","產量"""" + "\n" + lines
  }
}
