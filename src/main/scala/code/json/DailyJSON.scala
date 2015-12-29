package code.json

import code.lib._
import code.model._
import com.mongodb.casbah.Imports._
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

/**
 *  用來產生「產量統計」－＞「日報表」中的 JSON 格式的資料
 *
 *  JValue 是 Lift Web Framework 中用來表式 JSON 格式的資料結構的類別，
 *  當將這個類別的物件丟到 net.liftweb.http.JsonResponse 的時候，會自動
 *  轉換為 JSON 格式。
 *
 *  把 JValue 丟到 net.liftweb.http.JsonResponse 以及定義什麼樣的網址可
 *  以存取到什麼樣的 JSON 的程式碼位於 boot/JsonRestAPI.scala 中。
 *
 */
object DailyJSON extends JsonReport {

  /**
   *  輸出「產量統計」－＞「日報表」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year    年份
   *  @param    month   月份
   *  @return           此頁面的 JSON 格式的資料
   */
  def apply(year: Int, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = 
      MongoDB.zhenhaiDB("daily")
             .find("shiftDate" $gte startDate $lt endDate).toList

    // 取得每個製程對應到的累計良品數
    //
    // groupData(1) 為加締的良品數
    // groupData(2) 為組立的良品數，依此類推
    val dataByStep = 
      data.groupBy(entry => entry("machineType").toString.toInt)
          .mapValues(getSumQty)

    // 在 JSON 中我們需要依照加締(1) / 組立(2) / 老化(3) / 選別(4) / 加工切腳(5) 的順序排列
    val orderedKey = List(1, 2, 3, 4, 5)

    val dataSet = orderedKey.map { case step => 
      val countQty = dataByStep.getOrElse(step, 0L)
      val machineTypeName = MachineInfo.machineTypeName.get(step).getOrElse("Unknown")

      // 用 ~ 符號來將上述的三個欄位合併成一個 JSON 物件
      // 所以這邊實際上組出來的 JSON 檔大致上如下：
      // {
      //   "name": "加締",
      //   "value": 12345,
      //   "link": "/daily/2015/02"
      // }
      ("name"  -> machineTypeName) ~ 
      ("value" -> countQty) ~ 
      ("link"  -> s"/daily/$year/$month/$step")
    }

    // 最後的 JSON 檔是一個 JSON 物件，有一個 dataSet 欄位，該欄位
    // 為由上述的 dataSet 變數組成的 JSON 陣列，其組出來的 JSON 檔
    // 長得如下所示：
    //
    // {
    //   "dataSet": [
    //     {"name": "加締", "value": 1234, link: "/daily/2015/02"},
    //     {"name": "組立", "value": 234, link: "/daily/2015/02"},
    //     ....
    //   ]
    // }
    ("dataSet" -> dataSet)
  }

  /**
   *  輸出「產量統計」－＞「日報表」－＞「工序」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year    年份
   *  @param    month   月份
   *  @param    step    工序
   *  @return           此頁面的 JSON 格式的資料
   */
  def apply(year: Int, month: Int, step: String): JValue = {
    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB("daily")
                      .find("shiftDate" $gte startDate $lt endDate)
		      .filter(entry => entry("machineType") == step.toInt)

    val dataByDate = data.toList.groupBy(getDate).mapValues(getSumQty)
    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/daily/$year/$month/$step/$date")
    }

    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「日報表」－＞「工序」－＞「日期」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year    年份
   *  @param    month   月份
   *  @param    step    工序
   *  @param    date    日期
   *  @return           此頁面的 JSON 格式的資料
   */
  def apply(year: Int, month: Int, step: String, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"daily")
                      .find("shiftDate" $gte startDate $lt endDate)
		      .filter(entry => entry("machineType") == step.toInt)

    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)

    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/daily/$year/$month/$step/$date/$machineID")
    }

    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「日報表」－＞
   *  「工序」－＞「日期」－＞「機台編號」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year        年份
   *  @param    month       月份
   *  @param    step        工序
   *  @param    date        日期
   *  @param    machineID   機台編號
   *  @return               此頁面的 JSON 格式的資料
   */
  def apply(year: Int, month: Int, step: String, date: Int, machineID: String): JValue = {

    val cacheTableName = f"shift-$year-$month%02d-$date%02d"
    val data = 
      MongoDB.zhenhaiDB(cacheTableName).
              find(MongoDBObject("mach_id" -> machineID)).
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
