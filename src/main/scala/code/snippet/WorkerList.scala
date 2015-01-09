package code.snippet

import code.model._

import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds._
import java.text.SimpleDateFormat

class WorkerList {

  private val departmentToWorkers = Worker.findAll.filterNot(_.isDeleted.get).groupBy(_.department.get)
  private val departments = departmentToWorkers.keySet.toList.sortWith(_ < _)

  def deleteWorker(worker: Worker)(value: String) = {
    worker.isDeleted(true).saveTheRecord() match {
      case Full(_) => 
        S.notice(s"已刪除【${worker.name}】")
        Hide(s"row-${worker.id}")
      case _ => 
        S.error(s"無法刪除【${worker.name}】")
        Noop
    }
  }

  def render = {

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

    ".workerTable" #> departments.zipWithIndex.map { case(department, index) =>
      ".workerTable [data-tab]" #> department &
      ".workerTable [class+]" #> (if (index == 0) "active" else "") &
      ".workerRow" #> departmentToWorkers(department).map { worker =>
        ".workerRow [id]" #> s"row-${worker.id}" &
        ".workerName *" #> worker.name.get &
        ".workerID *" #> worker.workerID.get &
        ".workerTeam *" #> worker.team &
        ".workerOnBoardDate *" #> dateFormatter.format(worker.onBoardDate.get) &
        ".workingYears *" #> f"${worker.workingYears}" &
        ".workerType *" #> worker.workerTypeTitle &
        ".editLink [href]" #> s"/management/workers/edit/${worker.id}" &
        ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${worker.name}】嗎？", deleteWorker(worker)_)
      }
    } &
    ".departmentItem" #> departments.zipWithIndex.map { case (department, index) => 
      ".departmentItem *" #> department &
      ".departmentItem [data-tab]" #> department &
      ".departmentItem [class+]" #> (if (index == 0) "active" else "")
    }
  }
}

