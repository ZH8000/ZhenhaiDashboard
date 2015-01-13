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

class AlertList {

  import code.lib._

  val Array(_, _, date) = S.uri.drop(1).split("/")
  val alertList = code.model.Alert.findAll("date", date).toList.sortWith(_.timestamp.get < _.timestamp.get)

  def showEmptyBox() = {
     S.error("查無機台異常")
     ".dataBlock" #> NodeSeq.Empty
  }

  def render = {

    alertList.isEmpty match {
      case true => showEmptyBox()
      case false =>
        ".row" #> alertList.map { item =>

          val errorDesc = MachineInfo.getErrorDesc(item.mach_id.get, item.defact_id.get)

          ".timestamp *" #> item.timestamp &
          ".machineID *" #> item.mach_id &
          ".defactID *" #> errorDesc
        }
    }
  }
}

class StrangeQty {

  import code.lib._
  val strangeQtyList = StrangeQty.findAll.toList.sortWith(_.emb_date.get > _.emb_date.get)
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def render = {
    ".row" #> strangeQtyList.map { item =>

      val errorDesc = if (item.count_qty.get > 0) {
        ""
      } else {
        MachineInfo.getErrorDesc(item.mach_id.get, item.defact_id.get)
      }

      ".timestamp *" #> dateFormatter.format(new Date(item.emb_date.get * 1000)) &
      ".machineID *" #> item.mach_id &
      ".countQty *"  #> item.count_qty &
      ".badQty *" #> item.bad_qty &
      ".defactID *" #> errorDesc
    }
  }
}

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
      ".dueDate *" #> dateFormatter.format(alarm.dueDate) &
      ".machineID *" #> alarm.machineID &
      ".workerName *" #> alarm.name &
      ".desc *" #> alarm.description &
      ".dueDate [id]"      #> s"dueDate-${alarm.id}" &
      ".machineID [id]"    #> s"machineID-${alarm.id}" &
      ".desc [id]"  #> s"description-${alarm.id}" &
      ".dueDate [class+]"      #> (if (alarm.isDone.get) "disabled" else "") &
      ".machineID [class+]"    #> (if (alarm.isDone.get) "disabled" else "") &
      ".desc [class+]"  #> (if (alarm.isDone.get) "del" else "") &
      ".doneCheckbox [onclick]" #> SHtml.onEventIf(
        s"是否確【${alarm.machineID}】的【${alarm.description}】認標記成已完成", 
        markAsDoneInPostIt(alarm, _)
      )
    }

    val notDoneUrgent = urgentAlarms.filter(x => x.dueDateCalendar.compareTo(today) >= 0 && !x.isDone.get)
    val notDoneNormal = normalAlarms.filter(x => x.dueDateCalendar.compareTo(today) >= 0 && !x.isDone.get)

    val urgentListBinding = notDoneUrgent.isEmpty match {
      case true  => ".urgentAlarmBlock" #> NodeSeq.Empty
      case flase => ".urgentAlarmRow"   #> notDoneUrgent.map { alarm => rowItem(alarm) }
    }
    val alarmListBinding = notDoneNormal.isEmpty match {
      case true  => ".alarmBlock" #> NodeSeq.Empty
      case false => ".alarmRow" #> notDoneNormal.map { alarm => rowItem(alarm) }
    }

    if (notDoneUrgent.isEmpty && notDoneNormal.isEmpty) {
      ".urgentAlarmBlock" #> NodeSeq.Empty &
      ".alarmBlock" #> NodeSeq.Empty
    } else {
      urgentListBinding &
      alarmListBinding &
      ".nonAlarm" #> NodeSeq.Empty
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
      ".doneCheckbox" #> SHtml.ajaxCheckbox(alarm.isDone.get, markAsDone(alarm)) &
      ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${alarm.dueDateString} / ${alarm.machineID}】嗎？", deleteAlarm(alarm)_)
    }
  }
}

