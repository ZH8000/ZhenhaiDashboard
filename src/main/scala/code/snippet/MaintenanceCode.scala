package code.snippet

import code.model._
import code.lib._

import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmd
import net.liftweb.util.Helpers._
import net.liftweb.util._

class MaintenanceCodeEditor {

  def updateDescription(maintenanceCode: MaintenanceCode)(value: String): JsCmd = value.trim.isEmpty match {
    case true => Noop
    case false =>

      val newSavedRecord = maintenanceCode.description(value).saveTheRecord()

      newSavedRecord match {
        case Full(record) => S.notice(s"更新 ${maintenanceCode.code} 的描述為 $value。")
        case _ => S.error(s"無法更新 ${maintenanceCode.code} 的描述為 $value，請稍候再試")
      }

      Noop
  }

  def render = {
    ".maintenanceCodeRow" #> MachineInfo.maintenanceCodes.map { code =>

      val maintenanceCode = MaintenanceCode.find("code", code).openOr(MaintenanceCode.createRecord.code(code))
      val defaultDescription = maintenanceCode.description.get

      ".maintenanceCode *" #> code &
      "name=desc" #> SHtml.ajaxText(defaultDescription, false, updateDescription(maintenanceCode)_)
    }
  }

}
