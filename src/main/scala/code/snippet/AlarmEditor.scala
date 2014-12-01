package code.snippet

import code.model._
import code.lib._

import net.liftweb.common.{Box, Full, Empty, Failure}

import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._

import net.liftweb.http.js.JsCmd

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.StatefulSnippet
import net.liftweb.http.LiftRules

import net.liftweb.util.Helpers._
import java.util.Date
import java.text.SimpleDateFormat

class AlarmAdd extends AlarmEdit(Alarm.createRecord)

class AlarmEdit(alarm: Alarm) {

  private var workerBox: Box[Worker] = Worker.find(alarm.workerMongoID.toString)
  private var startDateBox: Box[String] = Full(new SimpleDateFormat("yyyy-MM-dd").format(alarm.startDate.get))
  private var machineIDBox: Box[String] = Full(alarm.machineID.toString)
  private var countdownDaysBox: Box[String] = Full(alarm.countdownDays.toString).filter(_ != "0")
  private var descriptionBox: Box[String] = Full(alarm.description.toString)
  private def currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date)

  private def setWorker(workerID: String): JsCmd = {
    this.workerBox = Worker.findByWorkerID(workerID)
    this.workerBox match {
      case Full(worker) => 
        SetValById("name", worker.name.get) &
        SetHtml(LiftRules.noticesContainerId, <span/>)
      case _ => S.error("請輸入正確的工號")
    }
  }

  private def process(): JsCmd = {
    def toDate(date: String) = try {
      Full(new SimpleDateFormat("yyyy-MM-dd").parse(date))
    } catch {
      case e: Exception => Failure(s"$date 不是合法的日期格式")
    }

    var startDateBox = S.param("startDate")
    var machineIDBox = S.param("machineID")
    var countdownDaysBox = S.param("countdownDays")
    var descriptionBox = S.param("description")

    val alarmRecord = for {
      worker           <- workerBox ?~ "請輸入正確的工號"
      startDateStr     <- startDateBox.filterNot(_.trim.isEmpty) ?~ "請輸入起算日期"
      startDate        <- toDate(startDateStr)
      machineID        <- machineIDBox.filterNot(_.trim.isEmpty) ?~ "請輸入維修機台"
      countdownDaysStr <- countdownDaysBox.filterNot(_.trim.isEmpty) ?~ "請輸入倒數天數"
      countdownDays    <- asInt(countdownDaysStr)
      description      <- descriptionBox.filterNot(_.trim.isEmpty) ?~ "請輸入描述"
      _                <- descriptionBox.filterMsg("描述字數不能多於 60 字")(_.length <= 60)
    } yield {
      alarm.name(worker.name.get)
           .workerMongoID(worker.id.toString)
           .startDate(startDate)
           .machineID(machineID)
           .workerID(worker.workerID.get)
           .countdownDays(countdownDays)
           .description(description)
    }

    alarmRecord match {
      case Full(alarm) =>
        val startDate = startDateBox.getOrElse("")
        val machineID = alarm.machineID
        JsRaw(s"""showModalDialog('$machineID', '$startDate', ${alarm.countdownDays}, '${alarm.description}');""")
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("無法寫入資料庫")
    }
  }

  def onConfirm(value: String): JsCmd = {
    alarm.saveTheRecord() match {
      case Full(alarm) => S.redirectTo("/management/alarms/", () => S.notice("成功新增或修改維修行事曆"))
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("無法寫入資料庫")
    }
  }

  def render = {
    val defaultWorkerID = workerBox.map(_.workerID.get).getOrElse("")
    val defaultWorkerName = workerBox.map(_.name.get)
    val defaultStartDate = startDateBox.getOrElse(currentDate)
    "@workerID"                 #> SHtml.ajaxText(defaultWorkerID, false, setWorker _, "name" -> "workerID") &
    "@name [value]"             #> defaultWorkerName &
    "@startDate [value]"        #> defaultStartDate &
    "@countdownDays [value]"    #> countdownDaysBox &
    "@defaultMachineID [value]" #> machineIDBox &
    "@description *"            #> descriptionBox &
    ".machineItem" #> MachineInfo.machineList.map { machineID =>
      ".machineItem [value]"  #> machineID &
      ".machineItem *"        #> machineID
    } &
    "type=submit" #> SHtml.ajaxOnSubmit(process) &
    "#modalOKButton [onclick]" #> SHtml.onEvent(onConfirm)
  }
}


