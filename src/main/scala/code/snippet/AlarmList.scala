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

  private def alarms = Alarm.findAll.toList.sortWith(_.machineID.get < _.machineID.get)
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
    val newRecord = alarm.isDone(true).doneTime(new Date).saveTheRecord()
    newRecord match {
      case Full(record) => JsRaw(s"""$$('#row-${alarm.id}').remove()""")
      case _ => S.error("無法存檔")
    }
  }


  def markAsDone(alarm: Alarm)(status: Boolean): JsCmd = {

    val newRecord = alarm.isDone(status).doneTime(new Date).saveTheRecord()

    newRecord match {
      case Full(record) if status => JsRaw(s"""updateUI('${alarm.id}', true)""")
      case Full(record) if !status => JsRaw(s"""updateUI('${alarm.id}', false)""")
      case _ =>
        S.error("無法存檔")
        JsRaw(s"""updateUI('${alarm.id}', ${!status})""")
    }
  }

  def postIt = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

    def rowItem(alarm: Alarm) = {
      ".item [id]" #> s"row-${alarm.id}" &
      ".machineID *" #> alarm.machineID &
      ".workerName *" #> alarm.name &
      ".countQty *" #> alarm.countQty &
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

  def render = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    ".alarmRow" #> alarms.map { alarm =>

      ".alarmRow [id]" #> s"row-${alarm.id}" &
      ".machineID *" #> alarm.machineID &
      ".description *" #> alarm.description &
      ".countdownQty *" #> alarm.countdownQty &
      ".countQty *" #> alarm.countQty &
      ".workerID *" #> alarm.workerID &
      ".workerName *" #> alarm.name &
      ".editLink [href]" #> s"/management/alarms/edit/${alarm.id}" &
      ".doneCheckbox" #> SHtml.ajaxCheckbox(alarm.isDone.get, markAsDone(alarm)) &
      ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${alarm.machineID} / ${alarm.description}】嗎？", deleteAlarm(alarm)_)
    }
  }
}

