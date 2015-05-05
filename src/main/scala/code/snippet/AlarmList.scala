package code.snippet

import code.model._

import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.jquery.JqJsCmds.Hide

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import scala.xml.NodeSeq

class AlarmList {

  private def alarms = {
    S.uri.split("/").drop(1) match {
      case Array("dashboard") => Alarm.findAll.toList.sortWith(_.machineID.get < _.machineID.get)
      case Array(_, _, step)  => Alarm.findAll("step", step).toList.sortWith(_.machineID.get < _.machineID.get)
    }
  }
  private val (urgentAlarms, normalAlarms) = alarms.partition(_.isUrgentEvent)

  def deleteAlarm(alarm: Alarm)(value: String) = {
   
    alarm.delete_! match {
      case true => 
        S.notice(s"已刪除【${alarm.machineID} / ${alarm.description}】 此筆記錄")
        Hide(s"row-${alarm.id}")
      case false => 
        S.error(s"無法刪除【${alarm.machineID} / ${alarm.description}】 此筆記錄")
        Noop
    }
  }

  def markAsDoneInPostIt(alarm: Alarm, value: String): JsCmd = {

    val machineIDToCounter = MachineCounter.toHashMap
    val countQty = machineIDToCounter.get(alarm.machineID.get).getOrElse(0L)

    val newRecord = 
      alarm.isDone(true)
           .doneTime(new Date)
           .lastReplaceCount(countQty)
           .replacedCounter(alarm.replacedCounter.get + 1)
           .saveTheRecord()

    newRecord match {
      case Full(record) => JsRaw(s"""$$('#row-${alarm.id}').remove()""")
      case _ => S.error("無法存檔")
    }
  }


  def markAsDone(alarm: Alarm, value: String): JsCmd = {

    val machineIDToCounter = MachineCounter.toHashMap
    val countQty = machineIDToCounter.get(alarm.machineID.get).getOrElse(0L)
    val nextCount = countQty + alarm.countdownQty.get
    val newReplacedCounter = alarm.replacedCounter.get + 1

    val newRecord = 
      alarm.isDone(true)
           .doneTime(new Date)
           .lastReplaceCount(countQty)
           .replacedCounter(newReplacedCounter)
           .saveTheRecord()

    newRecord match {
      case Full(record) => JsRaw(s"""updateUI('${alarm.id}', $nextCount, $newReplacedCounter)""")
      case _ => S.error("無法存檔"); Noop
    }
  }

  def postIt = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val machineIDToCounter = MachineCounter.toHashMap

    def rowItem(alarm: Alarm) = {
      
      val countQty = machineIDToCounter.get(alarm.machineID.get).getOrElse(0L)

      ".item [id]" #> s"row-${alarm.id}" &
      ".machineID *" #> alarm.machineID &
      ".countQty *" #> countQty &
      ".countdownQty *" #> alarm.countdownQty &
      ".desc *" #> alarm.description &
      ".machineID [id]"    #> s"machineID-${alarm.id}" &
      ".desc [id]"  #> s"description-${alarm.id}" &
      ".machineID [class+]"    #> (if (alarm.isDone.get) "disabled" else "") &
      ".desc [class+]"  #> (if (alarm.isDone.get) "del" else "") &
      ".doneCheckbox [onclick]" #> SHtml.onEventIf(
        s"是否確【${alarm.machineID}】的【${alarm.description}】認標記成已完成", 
        markAsDoneInPostIt(alarm, _)
      )
    }

    val notDoneUrgent = urgentAlarms.filterNot(_.isDone.get)

    notDoneUrgent.isEmpty match {
      case true  => ".urgentAlarmBlock" #> NodeSeq.Empty
      case flase => 
        ".nonAlarm"         #> NodeSeq.Empty &
        ".urgentAlarmRow"   #> notDoneUrgent.map { alarm => rowItem(alarm) } 
    }
  }

  def breadcrumb = {
   
    val Array(_, _, step) = S.uri.split("/").drop(1)
    val machineTypeTitle = step match {
      case "step1" => "加締"
      case "step2" => "組立"
      case "step3" => "老化"
      case "step4" => "選別"
      case "step5" => "加工"
      case _ => "Unknown"
    }

    "#machineTypeTitle *" #> machineTypeTitle
  }

  def render = {

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val machineIDToCounter = MachineCounter.toHashMap

    val machineIDs = alarms.map(_.machineID.get).distinct.sortWith(_ < _)
    val idToAlarmMapping = alarms.map(alarm => (alarm.machineID.get -> alarm)).toMap

    ".machineRow" #> machineIDs.map { machineID =>
      val countQty = machineIDToCounter.get(machineID).getOrElse(0L)
      val wanQty = countQty / 10000.0

      ".countQty *" #> countQty &
      ".machineID *" #> machineID &
      ".countQtyWan *" #> s"%.1f".format(wanQty) &
      ".alarmRow" #> alarms.sortWith(_.countdownQty.get < _.countdownQty.get).map { alarm =>

        val nextCount = alarm.lastReplaceCount.get + alarm.countdownQty.get

        val doneCheckBox = alarm.isDone.get match {
          case true  =>
            ".doneCheckboxHolder *" #> <span>Ｖ</span>
          case false =>
            ".doneCheckbox [onclick]" #> SHtml.onEventIf(
              s"是否確【${alarm.machineID}】的【${alarm.description}】認標記成已完成", 
              markAsDone(alarm, _)
            )

        }

        ".alarmRow [id]" #> s"row-${alarm.id}" &
        ".description *" #> alarm.description &
        ".countdownQty *" #> alarm.countdownQty &
        ".nextCount *" #> nextCount &
        ".nextCount [id]" #> s"nextCount-${alarm.id}" &
        ".doneCheckboxHolder [id]" #> s"doneCheckboxHolder-${alarm.id}" &
        ".countQty *" #> countQty &
        ".editLink [href]" #> s"/management/alarms/edit/${alarm.id}" &
        ".replacedCounter *" #> alarm.replacedCounter &
        ".replacedCounter [id]" #> s"replacedCounter-${alarm.id}" &
        ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${alarm.machineID} / ${alarm.description}】嗎？", deleteAlarm(alarm)_) &
        doneCheckBox
      }

    }
  }

  def addLink = {
    val Array(_, _, step) = S.uri.drop(1).split("/")

    "#addLink [href]" #> s"/management/alarms/$step/add"
  }
}

