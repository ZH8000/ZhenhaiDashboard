package code.json

import code.model._

import java.text.SimpleDateFormat
import java.util.Date

/**
 *  用來產生「維修紀錄」中的 JSON 格式的資料
 *
 *  JValue 是 Lift Web Framework 中用來表式 JSON 格式的資料結構的類別，
 *  當將這個類別的物件丟到 net.liftweb.http.JsonResponse 的時候，會自動
 *  轉換為 JSON 格式。
 *
 *  把 JValue 丟到 net.liftweb.http.JsonResponse 以及定義什麼樣的網址可
 *  以存取到什麼樣的 JSON 的程式碼位於 boot/JsonRestAPI.scala 中。
 */
object MachineMaintainLogJSON {
  
  /**
   *  代表一組維修紀錄（刷條碼一進／一出）的 Record
   *
   *  @param    startWorkerID       進入維修模式的員工的 ID
   *  @param    startWorkerName     進入維修模式的員工的姓名
   *  @param    endWorkerId         出維修模式的員工的 ID
   *  @param    endWorkerName       出維修模式的員工的姓名
   *  @param    machineID           被維修的機台編號
   *  @param    maintenanceCode     本次維修項目的維修編號的 List
   *  @param    startTime           維修開始的時間
   *  @param    endTime             出維修的時間
   *  @param    totalTime           總維修時間
   */
  case class Record(
    startWorkerID: String, 
    startWorkerName: String, 
    endWorkerID: String, 
    endWorkerName: String,
    machineID: String, 
    maintenanceCode: List[String], 
    startTime: String, 
    endTime: String,
    totalTime: String
  )

  /**
   *  取得特定日期的維修紀錄列表
   *
   *  @param    date    要取得的維修紀錄的日期
   *  @return           該日期的維修紀錄的 List
   */
  def getLogs(date: String): List[Record] = {

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    // 取得從 RaspberryPi 收到的維修紀錄的原始資料
    val rawDataLogs = MachineMaintainLog.findAll("insertDate", date)

    // 因為原始的維修紀錄中，開始維修／維修結束以及維修項目的代碼，都是
    // 一筆獨立的 Record，但在網頁上顯示的時候，從進維修到出維修應要是
    // 顯示為整合性的一筆資料。
    //
    // 為了能夠達成上述的目地，從 RaspberryPi 回傳的每一筆維修資料，都會
    // 帶有維修開始的時間，因此我們可以利用維修開始的時間將資料 group 起來，
    // 讓擁有同一個開始時間的維修紀錄視為同一筆資料。
    //
    // 此 allRecords 變數的 HashMap 就是用來達成這樣的目的，其中用來做為 Key 值
    // 的 Long 型別的資料，就是維修開始時間的 Timestamp，而其值就是整合過
    // 後的維修紀錄。
    //
    var allRecords: Map[Long, Record] = Map.empty

    // 針對原始的每一筆維修紀錄，進行大括號內的 block 的動作
    rawDataLogs.foreach { rawRecord =>

      val worker = Worker.find(rawRecord.workerMongoID.get)
      val workerID = worker.map(_.workerID.get).openOr("查無此人")
      val workerName = worker.map(_.name.get).openOr("查無此人")
      val startTime = new Date(rawRecord.startTimestamp.get * 1000)
      val endTime = new Date(rawRecord.timestamp.get * 1000)

      // 只有在維修開始時間非為 0 的時候才需要處理
      if (rawRecord.startTimestamp.get != 0L) {

        def createRecord = Record(
          workerID, workerName, 
          workerID, workerName,
          rawRecord.machineID.get, Nil, 
          dateFormatter.format(startTime), 
          dateFormatter.format(endTime),
          "AAAA"
        )

        // 取得 allRecords 中相對應到此維修紀錄開始時間的整合性紀錄，在 allRecords 中沒有，
        // 則建立一筆新的
        val targetRecord = allRecords.get(rawRecord.startTimestamp.get).getOrElse(createRecord)

        // 將目前的維修紀錄的維修項目編號也加入整合性維修紀錄日
        var updatedRecord = 
          targetRecord.copy(
            maintenanceCode = rawRecord.maintenanceCode.get :: targetRecord.maintenanceCode
          )

        // 如果此筆維修紀錄是出維修的紀錄（機器狀態為 03），則記錄此筆維修紀錄
        // 的結束維修人員／出維修時間／維修總時間
        if (rawRecord.status.get == "03") {
          val difference = (rawRecord.timestamp.get - rawRecord.startTimestamp.get) / 60
          val hours = difference / 60
          val minutes = difference % 60
          updatedRecord = updatedRecord.copy(
            endWorkerID = workerID,
            endWorkerName = workerName,
            endTime = dateFormatter.format(endTime),
            totalTime = "%02d:%02d".format(hours, minutes)
          )
        }

        // 更新 allRecords 中對應到此筆維修紀錄的開始時間的 Record 成為
        // updatedRecord 這個物件。
        allRecords += (rawRecord.startTimestamp.get -> updatedRecord)

      }
    }

    allRecords.values.toList
  }

}

