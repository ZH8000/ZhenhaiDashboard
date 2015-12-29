package code.json

import code.lib._
import code.model._
import com.mongodb.casbah.Imports._
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

/**
 *  用來產生「錯誤分析」中的 JSON 格式的資料
 *
 *  JValue 是 Lift Web Framework 中用來表式 JSON 格式的資料結構的類別，
 *  當將這個類別的物件丟到 net.liftweb.http.JsonResponse 的時候，會自動
 *  轉換為 JSON 格式。
 *
 *  把 JValue 丟到 net.liftweb.http.JsonResponse 以及定義什麼樣的網址可
 *  以存取到什麼樣的 JSON 的程式碼位於 boot/JsonRestAPI.scala 中。
 *
 */
object MachineJSON extends JsonReport {

  /**
   *  輸出「錯誤分析」的總覽頁面的 JSON 格式的資料
   *
   *  @return   此頁面的 JSON 格式的資料
   */
  def overview: JValue = {
    val data = MongoDB.zhenhaiDB("reasonByMachine").find("event_qty" $gt 0).toList
    val dataByMachineID = data.groupBy(getMachineType).mapValues(getSumEventQty)

    val dataJSON = dataByMachineID.map{ case (machineType, value) =>

      // 用 ~ 符號來將上述的三個欄位合併成一個 JSON 物件
      // 所以這邊實際上組出來的 JSON 檔大致上如下：
      // {
      //   "name": "加締卷取",
      //   "value": 12345,
      //   "link": "/machine/加締卷取"
      // }
      ("name" -> machineType) ~
      ("value" -> value) ~
      ("link" -> s"/machine/$machineType")
    }

    ("dataSet" -> dataJSON)
  }

  /**
   *  輸出「錯誤分析」－＞「工序」的總覽頁面的 JSON 格式的資料
   *
   *  @param    machineType   工序（加締卷取／組立／老化／選別／加工切腳）
   *  @return                 此頁面的 JSON 格式的資料
   */
  def apply(machineType: String): JValue = {

    val data = MongoDB.zhenhaiDB("reasonByMachine").find(MongoDBObject("mach_type" -> machineType) ++ ("event_qty" $gt 0))
    val dataByMachineModel = data.toList.groupBy(getMachineModel).mapValues(getSumEventQty)
    val dataJSON = dataByMachineModel.map{ case (machineModel, value) =>
      ("name" -> machineModel) ~
      ("value" -> value) ~
      ("link" -> s"/machine/$machineType/$machineModel")
    }

    ("dataSet" -> dataJSON)
  }

  /**
   *  輸出「錯誤分析」－＞「工序」－＞「機種」的總覽頁面的 JSON 格式的資料
   *
   *  @param    machineType   工序（加締卷取／組立／老化／選別／加工切腳）
   *  @param    machineModel  機台型號（TSW-100T / TSW-303....）
   *  @return                 此頁面的 JSON 格式的資料
   */
  def apply(machineType: String, machineModel: String): JValue = {

    val data = MongoDB.zhenhaiDB("reasonByMachine").find(
      MongoDBObject("mach_type" -> machineType) ++ 
      MongoDBObject("mach_model" -> machineModel) ++
      ("event_qty" $gt 0)
    )
    val dataByMachineID = data.toList.groupBy(getMachineID).mapValues(getSumEventQty)
    val dataJSON = dataByMachineID.map{ case (machineID, value) =>
      ("name" -> machineID) ~
      ("value" -> value) ~
      ("link" -> s"/machine/$machineType/$machineModel/$machineID")
    }

    ("dataSet" -> dataJSON)
  }

  /**
   *  輸出「錯誤分析」－＞「工序」－＞「機種」－＞「機台編號」的頁面的
   *  圓餅圖的 JSON 格式的資料
   *
   *  @param    machineID  機台編號
   *  @return              此頁面的 JSON 格式的資料
   */
  def detailPie(machineID: String): JValue = {

    val data = MongoDB.zhenhaiDB("dailyDefact").find(MongoDBObject("mach_id" -> machineID) ++ ("event_qty" $gt 0))
    val dataByDefactID = data.toList.groupBy(getDefactID).mapValues(getSumEventQty)
    val sortedData = dataByDefactID.toList.sortBy(_._1.toLong)
    val dataJSON = sortedData.map{ case (defactID, value) =>
      ("name" -> MachineInfo.getErrorDesc(machineID, defactID.toInt)) ~
      ("value" -> value)
    }

    ("dataSet" -> dataJSON)
  }

  /**
   *  輸出「錯誤分析」－＞「工序」－＞「機種」－＞「機台編號」的頁面的
   *  詳細表格的 JSON 格式的資料
   *
   *  @param    machineID  機台編號
   *  @return              此頁面的 JSON 格式的資料
   */
  def detailTable(machineID: String): JValue = {

    def byTimestamp(objA: DBObject, objB: DBObject) = objA("timestamp").toString < objB("timestamp").toString

    val data = MongoDB.zhenhaiDB("dailyDefact").find(MongoDBObject("mach_id" -> machineID) ++ ("event_qty" $gt 0))
    val dataJSON = data.toList.sortWith(byTimestamp).map { entry =>
      ("time" -> entry("timestamp").toString) ~
      ("defact_id" -> MachineInfo.getErrorDesc(machineID, entry("defact_id").toString.toInt)) ~
      ("event_qty" -> entry("event_qty").toString.toLong)
    }

    dataJSON
  }

}

