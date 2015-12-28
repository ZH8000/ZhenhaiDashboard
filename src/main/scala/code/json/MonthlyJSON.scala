package code.json

import code.lib._
import code.model._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.mongodb.casbah.Imports._

/**
 *  用來產生「產量統計」－＞「月報表」中的 JSON 格式的資料
 *
 *  JValue 是 Lift Web Framework 中用來表式 JSON 格式的資料結構的類別，
 *  當將這個類別的物件丟到 net.liftweb.http.JsonResponse 的時候，會自動
 *  轉換為 JSON 格式。
 *
 *  把 JValue 丟到 net.liftweb.http.JsonResponse 以及定義什麼樣的網址可
 *  以存取到什麼樣的 JSON 的程式碼位於 boot/JsonRestAPI.scala 中。
 *
 */
object MonthlyJSON extends JsonReport {


  /**
   *  輸出「產量統計」－＞「月報表」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year    報表年份
   *  @return           此頁面的 JSON 格式的資料
   */
  def apply(year: Int): JValue = {
    val startDate = f"$year-"
    val endDate = f"${year+1}-"

    val data = MongoDB.zhenhaiDB("daily").find("shiftDate" $gte startDate $lt endDate).toList

    // 取得每個製程對應到的累計良品數
    //
    // dataByStep(1) 為加締的良品數
    // dataByStep(2) 為組立的良品數，依此類推
    val dataByStep = 
      data.groupBy(entry => entry("machineType").toString.toInt)
          .mapValues(getSumQty)

    // 在 JSON 中我們需要依照加締(1) / 組立(2) / 老化(3) / 選別(4) / 加工切腳(5) 的順序排列
    val orderedKey = List(1, 2, 3, 4, 5)

    val dataSet = orderedKey.map { case step => 

      val stepTitle = MachineInfo.machineTypeName.get(step).getOrElse("Unknown")

      // 用 ~ 符號來將上述的三個欄位合併成一個 JSON 物件
      // 所以這邊實際上組出來的 JSON 檔大致上如下：
      // {
      //   "name": "加締",
      //   "value": 12345,
      //   "link": "/monthly/2015/1"
      // }
      ("name" -> stepTitle) ~
      ("value" -> dataByStep.getOrElse(step, 0L)) ~
      ("link" -> s"/monthly/$year/$step")
    }

    // 最後的 JSON 檔是一個 JSON 物件，有一個 dataSet 欄位，該欄位
    // 為由上述的 dataSet 變數組成的 JSON 陣列，其組出來的 JSON 檔
    // 長得如下所示：
    //
    // {
    //   "dataSet": [
    //     {"name": "加締", "value": 1234, link: "/monthly/2015/1"},
    //     {"name": "組立", "value": 234, link: "/monthly/2015/2"},
    //     ....
    //   ]
    // }
    ("dataSet" -> dataSet)
  }

  /**
   *  輸出「產量統計」－＞「月報表」－＞「工序」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year    報表年份
   *  @param    step    工序
   *  @return           此頁面的 JSON 格式的資料
   */
  def apply(year: Int, step: String): JValue = {

    val startDate = f"$year-"
    val endDate = f"${year+1}-"

    val data = MongoDB.zhenhaiDB("daily").find("shiftDate" $gte startDate $lt endDate).filter(_("machineType") == step.toInt)
    val dataByMonth = data.toList.groupBy(getYearMonth).mapValues(getSumQty)

    val sortedData = dataByMonth.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (yearMonth, value) =>
      val Array(year, month) = yearMonth.split("-")
      ("name" -> s"$month 月") ~
      ("value" -> value) ~
      ("link" -> s"/monthly/$year/$step/$month")
    }

    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「月報表」－＞「工序」－＞「月份」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year    報表年份
   *  @param    step    工序
   *  @param    month   月份
   *  @return           此頁面的 JSON 格式的資料
   */
  def apply(year: Int, step: String, month: Int): JValue = {

    val startDate = f"$year-$month%02d"
    val endDate = f"$year-${month+1}%02d"

    val data = MongoDB.zhenhaiDB("daily").find("shiftDate" $gte startDate $lt endDate).filter(_("machineType") == step.toInt)
    val dataByWeek = data.toList.groupBy(getWeek).mapValues(getSumQty)
    val sortedData = dataByWeek.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (week, value) =>
      ("name" -> s"第 $week 週") ~
      ("value" -> value) ~
      ("link" -> s"/monthly/$year/$step/$month/$week")
    }

    ("steps" -> List(f"$month%02d 月")) ~
    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「月報表」－＞「工序」－＞
   *  「月份」－＞「第幾週」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year    報表年份
   *  @param    step    工序
   *  @param    month   月份
   *  @param    week    週
   *  @return           此頁面的 JSON 格式的資料
   */
  def apply(year: Int, step: String, month: Int, week: Int): JValue = {

    val startDate = f"$year-$month%02d-01"
    val endDate = f"$year-${month+1}%02d-01"

    val data = MongoDB.zhenhaiDB(s"daily").find("shiftDate" $gte startDate $lt endDate).filter(_("machineType") == step.toInt)
    val dataInWeek = data.filter { entry => 
      val Array(year, month, date) = entry("shiftDate").toString.split("-").map(_.toInt)
      DateUtils.getWeek(year, month, date) == week
    }

    val dataByDate = dataInWeek.toList.groupBy(getDate).mapValues(getSumQty)
    val sortedData = dataByDate.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (date, value) =>
      ("name" -> s"$date 日") ~
      ("value" -> value) ~
      ("link" -> s"/monthly/$year/$step/$month/$week/$date")
    }

    ("steps" -> List(f"$month%02d 月", f"第 $week 週")) ~
    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「月報表」－＞「工序」－＞
   *  「月份」－＞「第幾週」－＞「日期」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year    報表年份
   *  @param    step    工序
   *  @param    month   月份
   *  @param    week    週
   *  @param    date    日期
   *  @return           此頁面的 JSON 格式的資料
   */
  def apply(year: Int, step: String, month: Int, week: Int, date: Int): JValue = {

    val startDate = f"$year-$month%02d-${date}%02d"
    val endDate = f"$year-$month%02d-${date+1}%02d"

    val data = MongoDB.zhenhaiDB(s"daily").find("shiftDate" $gte startDate $lt endDate).filter(_("machineType") == step.toInt)
    val dataByMachine = data.toList.groupBy(getMachineID).mapValues(getSumQty)

    val sortedData = dataByMachine.toList.sortBy(_._1)
    val sortedJSON = sortedData.map{ case (machineID, value) =>
      ("name" -> s"$machineID") ~
      ("value" -> value) ~
      ("link" -> s"/monthly/$year/$step/$month/$week/$date/$machineID")
    }

    ("steps" -> List(f"$month%02d 月", f"第 $week 週", f"$date 日")) ~
    ("dataSet" -> sortedJSON)
  }

  /**
   *  輸出「產量統計」－＞「月報表」－＞「工序」－＞
   *  「月份」－＞「第幾週」－＞「日期」的總覽頁面的 JSON 格式的資料
   *
   *  @param    year        報表年份
   *  @param    step        工序
   *  @param    month       月份
   *  @param    week        週
   *  @param    date        日期
   *  @param    machineID   機台編號
   *  @return               此頁面的 JSON 格式的資料
   */
  def apply(year: Int, month: Int, week: Int, date: Int, machineID: String): JValue = {

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

    ("steps" -> List(f"$year-$month%02d", f"第 $week 週", f"$date 日", machineID)) ~
    ("dataSet" -> jsonData.toList.sortBy(x => Record(x)))
  }

}


