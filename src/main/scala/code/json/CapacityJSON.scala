package code.json

import code.model._
import code.lib._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.util.Helpers._

import scala.collection.mutable.HashMap

import com.mongodb.casbah.Imports._

/**
 *  用來產生「產量統計」－＞「依容量」中的 JSON 格式的資料
 *
 *  JValue 是 Lift Web Framework 中用來表式 JSON 格式的資料結構的類別，
 *  當將這個類別的物件丟到 net.liftweb.http.JsonResponse 的時候，會自動
 *  轉換為 JSON 格式。
 *
 *  把 JValue 丟到 net.liftweb.http.JsonResponse 以及定義什麼樣的網址可
 *  以存取到什麼樣的 JSON 的程式碼位於 boot/JsonRestAPI.scala 中。
 *
 */
object CapacityJSON extends JsonReport {

  /**
   *  輸出「產量統計」－＞「依容量」的總覽頁面的 JSON 格式的資料
   *
   *  @return   此頁面的 JSON 格式的資料
   */
  def overview: JValue = {

    // 取得每個製程對應到的累計良品數
    //
    // groupData(1) 為加締的良品數
    // groupData(2) 為組立的良品數，依此類推
    val groupedData: Map[Int, Long] = 
      MongoDB.zhenhaiDB("product")                              // 取得 MongoDB 中 product 資料表中的所有資料
             .groupBy(x => x.get("machineType").toString.toInt) // 並將資料中 machineType 相同的 group 成一個 List，並成為一個 HashMap
             .mapValues(getSumQty)                              // 最後將 HashMap 中每一個 Element 中 Key 值的 List 做加總，讓
                                                                // 該 Key 對應到該製程的總良品數

    // 在 JSON 中我們需要依照加締(1) / 組立(2) / 老化(3) / 選別(4) / 加工切腳(5) 的順序排列
    val orderedKey = List(1, 2, 3, 4, 5)

    val dataSet: List[JValue] = orderedKey.map { case key => 
      val countQty = groupedData.getOrElse(key, 0L)
      val machineTypeTitle = MachineInfo.machineTypeName.get(key).getOrElse("Unknown")

      // 用 ~ 符號來將上述的三個欄位合併成一個 JSON 物件
      // 所以這邊實際上組出來的 JSON 檔大致上如下：
      // {
      //   "name": "加締",
      //   "value": 12345,
      //   "link": "/capacity/1"
      // }
      ("name" -> machineTypeTitle) ~    // 第一個欄位的名稱為 name，內容為 machineTypeTitle 字串
      ("value" -> countQty) ~           // 第二個欄位的名稱為 value，內容為 countQty 整數
      ("link" -> s"/capacity/$key")     // 第三個欄位為 link，內容為對應到下一層網頁的 URL 的字串
    }

    // 最後的 JSON 檔是一個 JSON 物件，有一個 dataSet 欄位，該欄位
    // 為由上述的 dataSet 變數組成的 JSON 陣列，其組出來的 JSON 檔
    // 長得如下所示：
    //
    // {
    //   "dataSet": [
    //     {"name": "加締", "value": 1234, link: "/capacity/1"},
    //     {"name": "組立", "value": 234, link: "/capacity/2"},
    //     ....
    //   ]
    // }
    ("dataSet" -> dataSet)
  }

  /**
   *  輸出「產量統計」－＞「依容量」－＞「工序」的總覽頁面的 JSON 格式的資料
   *
   *  @param    step      工序（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @return             此頁面的 JSON 格式的資料
   */
  def apply(step: String): JValue = {

    val data = MongoDB.zhenhaiDB(s"product").find(MongoDBObject("machineType" -> step.toInt)).toList
    val dataByCapacity = data.groupBy(record => record.get("capacityRange").toString).mapValues(getSumQty)

    // JSON 檔中的 φ 別順序為 5 - 8 / 10 - 12.5 / 16 - 18 / Unknown，
    // 且只列出 dataByCapacity 中有的部份
    val sortedData = List("5 - 8", "10 - 12.5", "16 - 18", "Unknown").filter(dataByCapacity contains _)

    val sortedJSON = sortedData.map{ capacity =>
      ("name" -> s"$capacity Φ") ~
      ("value" -> dataByCapacity.getOrElse(capacity, 0L)) ~
      ("link" -> s"/capacity/$step/${urlEncode(capacity)}")
    }

    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「依容量」－＞「工序」－＞「容量範圍」的總覽頁面的 JSON 格式的資料
   *
   *  @param    step      工序（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @param    capacity  容量範圍
   *  @return             此頁面的 JSON 格式的資料
   */
  def apply(step: String, capacity: String): JValue = {

    val data = MongoDB.zhenhaiDB(s"daily").find(MongoDBObject("machineType" -> step.toInt, "capacityRange" -> capacity)).toList
    val dataByProduct = data.groupBy(getYearMonth).mapValues(getSumQty)
    val sortedData = dataByProduct.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (yearAndMonth, value) =>
      val Array(year, month) = yearAndMonth.split("-").map(_.toInt)
      ("name" -> yearAndMonth) ~
      ("value" -> value) ~
      ("link" -> s"/capacity/$step/${urlEncode(capacity)}/$year/$month")
    }

    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「依容量」－＞「工序」－＞
   *  「容量範圍」－＞「年月」的總覽頁面的 JSON 格式的資料
   *
   *  @param    step      工序（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @param    capacity  容量範圍
   *  @param    year      報表年份
   *  @param    month     報表月份
   *  @return             此頁面的 JSON 格式的資料
   */
  def apply(step: String, capacity: String, year: Int, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB(s"daily").find("shiftDate" $gte startDate $lt endDate)
                      .filter(record => record("machineType") == step.toInt && record("capacityRange") == capacity)

    val dataByWeek = data.toList.groupBy(getWeek).mapValues(getSumQty)
    val sortedData = dataByWeek.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (week, value) =>
      ("name" -> s"第 $week 週") ~
      ("value" -> value) ~
      ("link" -> s"/capacity/$step/$capacity/$year/$month/$week")
    }

    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「依容量」－＞「工序」－＞
   *  「容量範圍」－＞「年月」－＞「週」的總覽頁面的 JSON 格式的資料
   *
   *  @param    step      工序（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @param    capacity  容量範圍
   *  @param    year      報表年份
   *  @param    month     報表月份
   *  @param    week      報表的週
   *  @return             此頁面的 JSON 格式的資料
   */
  def apply(step: String, capacity: String, year: Int, month: Int, week: Int): JValue = {

    val startDate = f"$year-$month%02d-01"
    val endDate = f"$year-${month+1}%02d-01"

    val data = 
      MongoDB.zhenhaiDB(s"daily")
             .find("shiftDate" $gte startDate $lt endDate)
             .filter(record => record("machineType") == step.toInt && record("capacityRange") == capacity)

    val dataInWeek = data.filter { entry => 
      val Array(year, month, date) = entry("shiftDate").toString.split("-").map(_.toInt)
      DateUtils.getWeek(year, month, date) == week
    }

    val dataByDate = dataInWeek.toList.groupBy(getDate).mapValues(getSumQty)
    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/capacity/$step/$capacity/$year/$month/$week/$date")
    }

    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「依容量」－＞「工序」－＞
   *  「容量範圍」－＞「年月」－＞「週」－＞「日期」的總覽頁面的 JSON 格式的資料
   *
   *  @param    step      工序（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @param    capacity  容量範圍
   *  @param    year      報表年份
   *  @param    month     報表月份
   *  @param    week      報表的週
   *  @param    date      日期
   *  @return             此頁面的 JSON 格式的資料
   */
  def apply(step: String, capacity: String, year: Int, month: Int, week: Int, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = 
      MongoDB.zhenhaiDB(s"daily")
             .find("shiftDate" $gte startDate $lt endDate)
             .filter(record => record("machineType") == step.toInt && record("capacityRange") == capacity)

    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)
    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/capacity/$step/$capacity/$year/$month/$week/$date/$machineID")
    }

    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「依容量」－＞「工序」－＞
   *  「容量範圍」－＞「年月」－＞「週」－＞
   *  「日期」－＞「機台編號」的總覽頁面的 JSON 格式的資料
   *
   *  @param    step      工序（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @param    capacity  容量範圍
   *  @param    year      報表年份
   *  @param    month     報表月份
   *  @param    week      報表的週
   *  @param    date      日期
   *  @param    machineID 機台編號
   *  @return             此頁面的 JSON 格式的資料
   */
  def apply(step: String, capacity: String, year: Int, month: Int, week: Int, date: Int, machineID: String): JValue = {

    val cacheTableName = f"shift-$year-$month%02d-$date%02d"

    val data = 
      MongoDB.zhenhaiDB(cacheTableName).
              find(MongoDBObject("machineType" -> step.toInt, "mach_id" -> machineID, "capacityRange" -> capacity)).
              sort(MongoDBObject("timestamp" -> 1))

    val jsonData = data.map { entry => 

      val countQty = entry("count_qty").toString.toLong
      val defactDescription = (countQty > 0) match {
        case true  => "良品數"
	case false => MachineInfo.getErrorDesc(machineID, entry("defact_id").toString.toInt)
      }

      ("timestamp" -> entry("timestamp").toString) ~
      ("defact_id" -> defactDescription) ~
      ("count_qty" -> entry("count_qty").toString.toLong) ~
      ("event_qty" -> entry("event_qty").toString.toLong)

    }

    ("dataSet" -> jsonData.toList.sortBy(x => Record(x)))
  }

}

