package code.snippet

import code.json._
import code.model._

import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import scala.xml.NodeSeq

class MachineMaintainLog {

  val logs = MachineMaintainLogJSON.getLogs

  def showEmptyBox() = {
    S.error("目前無機台維修記錄")
    "table" #> NodeSeq.Empty
  }

  def render = {

    logs.isEmpty match {
      case true  => showEmptyBox()
      case false =>
        ".row" #> logs.map { record =>
          ".workerID *"   #> record.workerID &
          ".workerName *" #> record.workerName &
          ".machineID *"  #> record.machineID &
          ".item *"       #> record.item &
          ".startTime *"  #> record.startTime &
          ".endTime *"    #> record.endTime
        }
    }
  }
}
