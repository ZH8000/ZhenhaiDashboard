package code.json

import code.lib._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.math.Ordering

import com.mongodb.casbah.Imports._


/**
 *  JSON 報表用工具
 *
 *  這個 Trait 用來存放其他產生 JSON 格式的程式碼需要用到的小函式
 *  和資料結構。
 *
 */
trait JsonReport {

  /**
   *  用來代表 JSON 格式物件當中一筆 Record，會從相對應的 JSON 物件中
   *  取得相對應的欄立。
   *
   *  舉例而言，當使用如下的程式碼之後：
   *
   *  {{{
   *    val jsonData: JValue = ....
   *    val record = Record(jsonData)
   *  }}}
   *
   *  即可透過 record.timestamp 取得 jsonData 中的 timestamp 欄位，
   *  用 record.countQty 取得 jsonData 中的 count_qty 欄位，
   *  以及用 reocrd.defactID 取得 jsonData 中的　
   */
  case class Record(json: JValue) {
    val JString(timestamp) = (json \\ "timestamp")
    val JInt(countQty) = (json \\ "count_qty")
    val JString(defactID) = (json \\ "defact_id")
  }
  
  /**
   *  用來排序 Record 的方法，當有一個 xs: List[Record] 時，可以
   *  使用 {{{xs.sortWith((a, b) => a < b)}}} 的方式來排序時，即會
   *  這個物件裡的 compare 函式來比較 a 和 b 兩個 Record，並決定其
   *  順序。
   */
  implicit object TableSorting extends Ordering[Record]{

    def compare(a: Record, b: Record): Int = {
  
      // 若 Timestamp 不同，那就依照 Timestamp 的順序
      if (a.timestamp != b.timestamp) {
        a.timestamp compare b.timestamp
      } else {
        // 不然的話，就再看良品數是否相同，若不同就
        // 用良品數來排。若連良品數都相同，就用 defactID
        // 來排序。
        if (a.countQty == b.countQty) {
          a.defactID compare a.defactID
        } else {
          b.countQty compare b.countQty
        }
      }
    }
  }

  /**
   *  取得一個 MongoDB 的 Record 的 List 中的 count_qty 欄位的總合
   *
   *  舉例而言，若 dataList 有三個元素，分別為 
   *  {{{
   *    {"machineID": "A01", "count_qty": 1},
   *    {"machineID": "A02", "count_qty": 3},
   *    {"machineID": "A03", "count_qty": 4}
   *  }}}
   *
   *  那將上述的 dataList 丟到這個函式後，會得到 (1 + 3 + 4) = 8
   *
   *  @param    dataList    一個 MongoDB 資料庫物件的 List
   *  @return               上述的 dataList 中的 count_qty 欄位的總合
   */
  def getSumQty(dataList: Iterable[DBObject]): Long = dataList.map(data => data("count_qty").toString.toLong).sum

  /**
   *  取得一個 MongoDB 的 Record 的 List 中的 event_qty 欄位的總合
   *
   *  舉例而言，若 dataList 有三個元素，分別為 
   *  {{{
   *    {"machineID": "A01", "count_qty": 1, "event_qty": 1},
   *    {"machineID": "A02", "count_qty": 2, "event_qty": 1},
   *    {"machineID": "A03", "count_qty": 3, "event_qty": 1}
   *  }}}
   *
   *  那將上述的 dataList 丟到這個函式後，會得到 (1 + 1 + 1) = 3
   *
   *  @param    dataList    一個 MongoDB 資料庫物件的 List
   *  @return               上述的 dataList 中的 event_qty 欄位的總合
   */
  def getSumEventQty(dataList: List[DBObject]) = dataList.map(data => data("event_qty").toString.toLong).sum


  /**
   *  取得 MongoDB 的 Record 中 mach_id 欄位
   *
   *  @param    entry       MongoDB 的 Record
   *  @return               entry.mach_id 的值
   */
  def getMachineID(entry: DBObject) = entry("mach_id").toString

  /**
   *  取得 MongoDB 的 Record 中 shiftDate 欄位（格式為 yyyy-MM-dd）中的日期（dd）的部份，
   *  並轉成 Long 型別。
   *
   *  @param    entry       MongoDB 的 Record
   *  @return               entry.shiftDate 中的日期
   */
  def getDate(entry: DBObject): Long = entry("shiftDate").toString.split("-")(2).toLong

  /**
   *  取得 MongoDB 的 Record 中 shiftDate 欄位（格式為 yyyy-MM-dd）中的年月（yyyy-MM）的部份，
   *  並轉成 String 型別。
   *
   *  @param    entry       MongoDB 的 Record
   *  @return               entry.shiftDate 中的年月份（yyyy-MM）
   */
  def getYearMonth(entry: DBObject) = entry("shiftDate").toString.substring(0, 7)

  /**
   *  取得 MongoDB 的 Record 中 shiftDate 欄位的日期對應到該月的第幾週
   *
   *  @param    entry       MongoDB 的 Record
   *  @return               entry.shiftDate 中的日期是該用的第幾週
   */
  def getWeek(entry: DBObject) = {
    val Array(year, month, date) = entry("shiftDate").toString.split("-")
    DateUtils.getWeek(year.toInt, month.toInt, date.toInt)
  }

  /**
   *  取得 MongoDB 的 Record 中 mach_type 欄位
   *
   *  @param    entry       MongoDB 的 Record
   *  @return               entry.mach_type 的值
   */
  def getMachineType(entry: DBObject) = entry("mach_type").toString

  /**
   *  取得 MongoDB 的 Record 中 mach_model 欄位
   *
   *  @param    entry       MongoDB 的 Record
   *  @return               entry.mach_model 的值
   */
  def getMachineModel(entry: DBObject) = entry("mach_model").toString

  /**
   *  取得 MongoDB 的 Record 中 defact_id 欄位
   *
   *  @param    entry       MongoDB 的 Record
   *  @return               entry.defact_id 的值
   */
  def getDefactID(entry: DBObject) = entry("defact_id").toString


}


