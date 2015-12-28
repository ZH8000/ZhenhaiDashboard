package code.lib

import net.liftweb.json.JsonDSL._
import net.liftweb.json._

/**
 *  將 JSON 格式轉成 CSV 檔案格式生成用的 Singleton 物件
 *
 */
object CSVConverter {

  /**
   *  取出 JSON 格式中的某一個欄位並轉換成 CSV 格式的資料
   *
   *  @param    json          JSON 資料
   *  @param    fieldName     要取出的欄位名稱
   *  @return                 該欄位的值加上雙引號，以做為 CSV 格式的欄位
   */
  def toCSVField(json: JValue, fieldName: String): String = {
    (json \\ fieldName) match {
      case JInt(value) => value.toString
      case value => s""""${value.values.toString}""""
    }
  }

  /**
   *  將 JSON 格式轉換成 CSV 格式的資料
   *
   *  這個函式會先在第一列輸出 titles 做為 CSV 格式的表格標頭，
   *  然後針對 json 這個變數中的資料，一筆資料對應到一 CSV 中的
   *  一行，並將其轉成 CSV 格式，來各欄位的順序即由 fields 這個
   *  變數指定。
   *
   *  @param    titles        CSV 裡第一列中的標頭
   *  @param    fields        CSV 裡每一欄對應到 JSON 的哪個欄位
   *  @param    json          JSON 檔案
   *  @return                 CSV 格式的輸出
   */
  def apply(titles: List[String], fields: List[String], json: JValue) = {
    val JArray(dataSet) = json
    val csvTitle = titles.mkString(",")
    val csvRecords = dataSet.map { record =>
      val csvRecords = fields.map(field => toCSVField(record, field))
      csvRecords.mkString(",")
    }

    csvTitle + "\n" + csvRecords.mkString("\n")
  }

  /**
   *  將 JSON 格式轉換成 CSV 格式的資料
   *
   *  這個函式會先在第一列輸出 titles 做為 CSV 格式的表格標頭，
   *  然後針對 json 這個變數中的資料，一筆資料對應到一 CSV 中的
   *  一行，並將其轉成 CSV 格式，來各欄位的順序即由 fields 這個
   *  變數指定。
   *
   *  除此之外，每一列的最後會加上一欄，其標頭為 descTitle，而
   *  其內容 json 資料中由 defactIDField 指定的欄位。
   *
   *  @param    titles        CSV 裡第一列中的標頭
   *  @param    fields        CSV 裡每一欄對應到 JSON 的哪個欄位
   *  @param    machineID     機台編號
   *  @param    descTitle     最後一欄的標頭
   *  @param    defactIDField 最後一欄是由 JSON 的哪個欄位取出
   *  @param    json          JSON 檔案
   *  @return                 CSV 格式的輸出
   */
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

