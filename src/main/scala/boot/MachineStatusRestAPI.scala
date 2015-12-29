package bootstrap.liftweb

import code.model._
import com.mongodb.casbah.Imports._
import net.liftweb.http.{PlainTextResponse, _}
import net.liftweb.http.rest.RestHelper

/**
 *  取得機台斷電前的機台狀態
 *
 *  這個 REST API 用來提供給 RaspberryPi 存取，讓 RaspberryPi 知道
 *  上一次斷電時機台的狀態。
 *
 */
object MachineStatusRestAPI extends RestHelper {

  /**
   *  取得機台上一次斷電前的狀態
   *
   *  每一個工單的資料為一行，格式為：
   *
   *  工單號 料號 目標量 目前為止此工單累計的良品數
   *
   *  @param    machineID     機台編號
   *  @return                 機台狀態  
   */
  def getMachineStatus(machineID: String) = {

    val totalCountOfOrders = MongoDB.zhenhaiDB("totalCountOfOrders")

    // 因為 RaspberryPi 黑盒子開機時不會知道自己上一次是不是不正常斷電，
    // 所以一定都會到這個 REST API 網址來抓資料。我們必須確認只有當此機
    // 台上一次是不正常斷電時，才反回資料。
    //
    // 所以當 RaspberryPi 存取資料時，我們只能回傳該機台最後一個狀態為
    // 不正常斷電（status 為 11）的資料。
    //
    val query = MongoDBObject("machineID" -> machineID, "status" -> "11")

    totalCountOfOrders.find(query).toList match {
      case Nil => NotFoundResponse("NotFound")
      case records => 
        val lines = records.map { record => 
          val lotNo = record.get("lotNo")
          val partNo = record.get("partNo")
          val workQty = record.get("workQty")
          val totalCount = record.get("totalCount")
          s"$lotNo $partNo $workQty $totalCount" 
        }
      
        PlainTextResponse(lines.mkString("\n"))
    }
  }

  // 設定 REST API 的網址
  //
  //  - 網址為 api/machineStatus/機台編號
  //  - 此網址的輸出為 getMachineStatus 函式的返回值
  //
  serve("api" / "machineStatus" prefix {
    case machineID :: Nil Get req => getMachineStatus(machineID)
  })

}
