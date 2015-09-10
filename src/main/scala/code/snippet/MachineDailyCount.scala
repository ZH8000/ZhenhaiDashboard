package code.snippet

import code.json._
import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import scala.xml.NodeSeq
import com.mongodb.casbah.Imports._


class MachineDailyCount {
  def showEmptyBox() = {
    S.error("目前無本日記錄")
    ".dataBlock" #> NodeSeq.Empty
  }


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

