package code.snippet

import code.json._
import code.model._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import scala.xml.NodeSeq

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

    val maintenanceCodeDescription = 
      MaintenanceCode.findAll.map(record => (record.code.get -> record.description.get)).toMap

    logs.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        "#csvURL [href]" #> s"/api/csv/maintenanceLog/${date}" &
        ".row" #> logs.map { record =>

          val codeDescriptions = record.maintenanceCode.map(code => maintenanceCodeDescription.get(code).getOrElse(code))

          ".workerID *"   #> record.workerID &
          ".workerName *" #> record.workerName &
          ".machineID *"  #> record.machineID &
          ".item *"       #> record.maintenanceCode &
          ".startTime *"  #> record.startTime &
          ".endTime *"    #> record.endTime &
          ".desc *"       #> codeDescriptions.mkString("、")
        }
    }
  }
}
