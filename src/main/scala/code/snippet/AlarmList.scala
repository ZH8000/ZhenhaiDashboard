package code.snippet

import code.model._

import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.jquery.JqJsCmds.Hide

import java.text.SimpleDateFormat
import java.util.Date

class AlarmList {

  private def alarms = Alarm.findAll.toList.sortWith(_.dueDate.getTime < _.dueDate.getTime)
  private val (urgentAlarms, normalAlarms) = alarms.partition(_.isUrgentEvent)

  def deleteAlarm(alarm: Alarm)(value: String) = {
   
    alarm.delete_! match {
      case true => 
        S.notice(s"已刪除【${alarm.dueDateString} / ${alarm.machineID}】 此筆記錄")
        Hide(s"row-${alarm.id}")
      case false => 
        S.error(s"無法刪除【${alarm.dueDateString} / ${alarm.machineID}】 此筆記錄")
        Noop
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
      ".alarmRow [id]" #> s"row-${alarm.id}" &
      ".dueDate *" #> dateFormatter.format(alarm.dueDate) &
      ".machineID *" #> alarm.machineID &
      ".description *" #> alarm.description &
      ".dueDate [id]"      #> s"dueDate-${alarm.id}" &
      ".machineID [id]"    #> s"machineID-${alarm.id}" &
      ".description [id]"  #> s"description-${alarm.id}" &
      ".popup [data-title]" #> s"${alarm.name} / 2014-11-22" &
      ".popup [data-content]" #> alarm.description &
      ".dueDate [class+]"      #> (if (alarm.isDone.get) "disabled" else "") &
      ".machineID [class+]"    #> (if (alarm.isDone.get) "disabled" else "") &
      ".description [class+]"  #> (if (alarm.isDone.get) "disabled" else "") &
      "@doneLabel *"           #> (if (alarm.isDone.get) "已完成" else "末完成") &
      "@doneCheckbox [checked+]" #> (if (alarm.isDone.get) Some("checked") else None) &
      "@doneCheckbox" #> SHtml.ajaxCheckbox(alarm.isDone.get, markAsDone(alarm))
    }

    ".urgentAlarmRow" #> urgentAlarms.map { alarm => rowItem(alarm) } &
    ".alarmRow" #> normalAlarms.map { alarm => rowItem(alarm) }
  }

  def render = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    ".alarmRow" #> alarms.map { alarm =>

      println(alarm.isDone.get)
      ".alarmRow [id]" #> s"row-${alarm.id}" &
      ".dueDate *" #> dateFormatter.format(alarm.dueDate) &
      ".machineID *" #> alarm.machineID &
      ".description *" #> alarm.description &
      ".workerID *" #> alarm.workerID &
      ".workerName *" #> alarm.name &
      ".editLink [href]" #> s"/management/alarms/edit/${alarm.id}" &
      ".doneCheckbox" #> SHtml.ajaxCheckbox(alarm.isDone.get, markAsDone(alarm)) &
      ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${alarm.dueDateString} / ${alarm.machineID}】嗎？", deleteAlarm(alarm)_)
    }
  }
}

