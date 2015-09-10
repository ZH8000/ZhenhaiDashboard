package code.snippet

import code.json._
import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import scala.xml.NodeSeq
import com.mongodb.casbah.Imports._

class MachineMaintainLog {


  def showEmptyBox() = {
    S.error("目前無機台維修記錄")
    ".dataBlock" #> NodeSeq.Empty
  }

  def dateList = {
    ".maintenanceDate" #> MachineMaintainLog.dateList.map { date =>
      "a [href]" #> s"/maintenanceLog/$date" &
      "a *"      #> date.toString
    }
  }

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
