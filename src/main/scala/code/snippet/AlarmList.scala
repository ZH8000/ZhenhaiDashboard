package code.snippet

import code.model._

import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds._
import java.text.SimpleDateFormat

class AlarmList {

  private def alarms = Alarm.findAll.toList.sortWith(_.dueDate.getTime < _.dueDate.getTime)

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

  def render = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    ".alarmRow" #> alarms.map { alarm =>
      ".alarmRow [id]" #> s"row-${alarm.id}" &
      ".dueDate *" #> dateFormatter.format(alarm.dueDate) &
      ".machineID *" #> alarm.machineID &
      ".description *" #> alarm.description &
      ".workerID *" #> alarm.workerID &
      ".workerName *" #> alarm.name &
      ".editLink [href]" #> s"/management/alarms/edit/${alarm.id}" &
      ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${alarm.dueDateString} / ${alarm.machineID}】嗎？", deleteAlarm(alarm)_)
    }
  }
}

