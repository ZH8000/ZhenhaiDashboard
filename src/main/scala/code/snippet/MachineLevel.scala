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

class MachineLevelEditor {

  def updateLevel(machineLevel: MachineLevel, field: String)(value: String): JsCmd = value.trim.isEmpty match {
    case true => Noop
    case false =>

      def updateRecord(field: String, newLevel: Long) = {
        field match {
          case "A" => machineLevel.levelA(newLevel)
          case "B" => machineLevel.levelB(newLevel)
          case "C" => machineLevel.levelC(newLevel)
        }

        machineLevel
      }

      val newSavedRecord = for {
        newValue <- asLong(value)
        newRecord = updateRecord(field, newValue)
        savedRecord <- newRecord.saveTheRecord
      } yield savedRecord

      newSavedRecord match {
        case Full(record) => S.notice(s"更新 ${machineLevel.machineID} 的 $field 至 $value。")
        case _ => S.error(s"無法更新 ${machineLevel.machineID} 的 $field 至 $value，請確認新的值無誤")
      }

      Noop
  }

  def render = {

    val sortedMachineList = MachineInfo.machineList.sortWith(_ < _)

    

    ".machineLevelRow" #> sortedMachineList.map { machineID =>

      val machineLevel = MachineLevel.find("machineID", machineID).openOr(MachineLevel.createRecord.machineID(machineID))

      val defaultLevelA = if (machineLevel.levelA.get > 0) machineLevel.levelA.toString else ""
      val defaultLevelB = if (machineLevel.levelB.get > 0) machineLevel.levelB.toString else ""
      val defaultLevelC = if (machineLevel.levelC.get > 0) machineLevel.levelC.toString else ""

      ".machineID *" #> machineID &
      "name=levelA" #> SHtml.ajaxText(defaultLevelA, false, updateLevel(machineLevel, "A")_) &
      "name=levelB" #> SHtml.ajaxText(defaultLevelB, false, updateLevel(machineLevel, "B")_) &
      "name=levelC" #> SHtml.ajaxText(defaultLevelC, false, updateLevel(machineLevel, "C")_)
    }
  }

}
