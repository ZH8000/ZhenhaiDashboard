package code.snippet

import code.json._
import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import scala.xml.NodeSeq
import com.mongodb.casbah.Imports._

/**
 *  用來處理 /rawData/yyyy-MM-dd 網址中的機台列表與 raw data 表格
 *
 */
class MachineDailyCount {

  /**
   *  顯示「無本日記錄」的訊息並隱藏網頁上其他元素
   */
  def showEmptyBox() = {
    S.error("目前無本日記錄")
    ".dataBlock" #> NodeSeq.Empty
  }

  /**
   *  用來顯示機台的 raw data，會將每筆 rawData 記錄轉化成 HTML 模板中表格的一行
   *
   *  機台編號的部份由網址取得，網址的格式為 /rawData/yyyy-MM-dd/XXX，其中 XXX 的
   *  部份即是機台編號。
   */
  def detail = {
    val Array(_, date, machineID) = S.uri.drop(1).split("/")
    val detailList = MongoDB.zhenhaiDaily(date).find(MongoDBObject("machineID" -> machineID))

    detailList.isEmpty match {
      case true   => showEmptyBox()
      case false  => 
        ".row" #> detailList.toList.map { record =>
          ".rawData *" #> record.get("rawData").toString
        }
    }

  }

  /**
   *  用來顯示機台列表及該日的累計良品數和事件數
   *
   *  日期的部份由網址取得，網址的格式為 /rawData/yyyy-MM-dd，其中 yyyy-MM-dd 的
   *  部份即是日期。
   */
  def render = {

    val Array(_, date) = S.uri.drop(1).split("/")
    val machineList = DailyMachineCount.findAll("insertDate", date).sortWith(_.machineID.get < _.machineID.get)

    machineList.isEmpty match {
      case true   => showEmptyBox()
      case false  =>
        ".row" #> machineList.map { record =>
          ".machineID *" #> record.machineID &
          ".countQty *" #> record.count_qty &
          ".eventQty *" #> record.event_qty &
          ".machineStatus *" #> record.status &
          ".rawDataList [href]" #> s"/rawData/$date/${record.machineID}"
        }
    }

  }

}

