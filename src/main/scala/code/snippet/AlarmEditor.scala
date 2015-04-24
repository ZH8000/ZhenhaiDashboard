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

  private val Array(_, _, step, _) = S.uri.split("/").drop(1)
  private var machineIDBox: Box[String] = Full(alarm.machineID.toString)
  private var countdownQtyBox: Box[String] = Full(alarm.countdownQty.toString).filter(_ != "0")
  private var descriptionBox: Box[String] = Full(alarm.description.toString)

  private def process(): JsCmd = {
    def toDate(date: String) = try {
      Full(new SimpleDateFormat("yyyy-MM-dd").parse(date))
    } catch {
      case e: Exception => Failure(s"$date 不是合法的日期格式")
    }

    var machineIDBox = S.param("machineID")
    var countdownQtyBox = S.param("countdownQty")
    var descriptionBox = S.param("description")

    val alarmRecord = for {
      machineID        <- machineIDBox.filterNot(_.trim.isEmpty) ?~ "請輸入維修機台"
      countdownQtyStr <- countdownQtyBox.filterNot(_.trim.isEmpty) ?~ "請輸入目標良品數"
      countdownQty    <- asInt(countdownQtyStr)
      description      <- descriptionBox.filterNot(_.trim.isEmpty) ?~ "請輸入描述"
      _                <- descriptionBox.filterMsg("描述字數不能多於 60 字")(_.length <= 60)
    } yield {
      alarm.machineID(machineID)
           .step(step)
           .countdownQty(countdownQty)
           .description(description)
    }

    alarmRecord match {
      case Full(alarm) =>
        val machineID = alarm.machineID
        JsRaw(s"""showModalDialog('$machineID', ${alarm.countdownQty});""")
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("無法寫入資料庫")
    }
  }

  def onConfirm(value: String): JsCmd = {
    alarm.saveTheRecord() match {
      case Full(alarm) => S.redirectTo(s"/management/alarms/$step", () => S.notice("成功新增或修改維修行事曆"))
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("無法寫入資料庫")
    }
  }

  def render = {

    val machineList = MachineInfo.machineInfoList.filter("step" + _.machineType == step).map(_.machineID)

    "@countdownQty [value]"     #> countdownQtyBox &
    "@defaultMachineID [value]" #> machineIDBox &
    "@description *"            #> descriptionBox &
    ".machineItem" #> machineList.map { machineID =>
      ".machineItem [value]"  #> machineID &
      ".machineItem *"        #> machineID
    } &
    "type=submit" #> SHtml.ajaxOnSubmit(process) &
    "#modalOKButton [onclick]" #> SHtml.onEvent(onConfirm)
  }
}


