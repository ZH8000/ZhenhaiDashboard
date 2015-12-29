package code.snippet

import code.json._
import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._

import scala.xml.NodeSeq

/**
 *  用來顯示網頁上「維修記錄」的頁面的 Snippet
 */ 
class MachineMaintainLog {

  /**
   *  顯示「無維修紀錄」的訊息，並將 HTML 中 class="dataBlock" 以下的子節點隱藏
   */
  def showEmptyBox() = {
    S.error("目前無機台維修記錄")
    ".dataBlock" #> NodeSeq.Empty
  }

  /**
   *  設定麵包屑上的日期
   */
  def dateLink = {
    val Array(_, date) = S.uri.drop(1).split("/")

    ".date *" #> date
  }

  /**
   *  在日期列表上顯示有維修紀錄的日期列表
   */
  def dateList = {
    ".maintenanceDate" #> MachineMaintainLog.dateList.map { date =>
      "a [href]" #> s"/maintenanceLog/$date" &
      "a *"      #> date.toString
    }
  }

  /**
   *  顯示特定日期的維修紀錄
   *
   *  日期的部份由網址取出，網址的格式為 /maintenanceLog/yyyy-MM-dd
   */
  def render = {

    val Array(_, date) = S.uri.drop(1).split("/")
    val logs = MachineMaintainLogJSON.getLogs(date)

    logs.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        "#csvURL [href]" #> s"/api/csv/maintenanceLog/${date}" &
        ".row" #> logs.zipWithIndex.map { case (record, counter) =>

          val machineInfoHolder = MachineInfo.idTable.get(record.machineID)
          val machineType = machineInfoHolder.map(_.machineType).getOrElse(-1)
          val codeMapping = MaintenanceCode.mapping.get(machineType).getOrElse(Map.empty[Int, String])
          val codeDescriptions = record.maintenanceCode.map(code => codeMapping.get(code.toInt).getOrElse(code))

          ".counter *"  #> (counter + 1) &
          ".startWorkerName *" #> record.startWorkerName &
          ".machineID *"  #> record.machineID &
          ".item *"       #> codeDescriptions &
          ".startTime *"  #> record.startTime &
          ".endTime *"    #> record.endTime &
          ".endWorkerName *" #> record.endWorkerName &
          ".totalTime *" #> record.totalTime

        }
    }
  }
}
