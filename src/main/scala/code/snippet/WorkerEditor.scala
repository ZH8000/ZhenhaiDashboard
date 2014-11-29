package code.snippet

import code.model._

import net.liftweb.common.{Box, Full, Empty, Failure}

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.StatefulSnippet

import net.liftweb.util.Helpers._

class WorkerEdit(worker: Worker) extends StatefulSnippet {

  private var workerNameBox: Box[String] = Full(worker.name.get)
  private var workerIDBox: Box[String] = Full(worker.workerID.get)
  private var departmentBox: Box[String] = Full(worker.department.get)
  private var teamBox: Box[String] = Full(worker.team.get)
  private var workerTypeBox: Box[String] = Full(worker.workerType.get)

  def dispatch = {
    case "render" => render
  }

  def checkWorkerID(workerID: String) = Worker.hasNoDuplicateID(workerID, Some(worker))
  def checkWorkerName(name: String) = Worker.hasNoDuplicateName(name, Some(worker))

  def process() {
    workerNameBox = S.param("name").filterNot(_.trim.isEmpty)
    workerTypeBox = S.param("workerType").filterNot(_.trim.isEmpty)
    workerIDBox = S.param("workerID").filterNot(_.trim.isEmpty)
    departmentBox = S.param("department").filterNot(_.trim.isEmpty)
    teamBox = S.param("team").filterNot(_.trim.isEmpty)

    val result = for {
      workerName      <- workerNameBox ?~ "姓名為必填欄位"
      workerID        <- workerIDBox ?~ "工號為必填欄位"
      noDuplicateName <- workerNameBox.filterMsg("系統內已有相同員工姓名")(checkWorkerName _)
      noDuplicateID   <- workerIDBox.filterMsg("系統內已有相同工號")(checkWorkerID _)
      department      <- departmentBox ?~ "部門為必填欄位"
      team            <- teamBox ?~ "組別為必填欄位"
      workerType      <- workerTypeBox ?~ "員工類別"
      worker          <- worker.name(workerName).workerID(workerID).department(department).team(team).
                                workerType(workerType).saveTheRecord()
    } yield worker

    result match {
      case Full(worker) => 
        S.redirectTo(
          "/management/workers/", 
          () => S.notice(s"已成功新增員工 ${worker.name.get}")
        )
      case Failure(x, _, _) => S.error(x)
      case _ =>
    }
  }

  def render = {
    "@name [value]"       #> workerNameBox.getOrElse("") &
    "@workerID [value]"   #> workerIDBox.getOrElse("") &
    "@department [value]" #> departmentBox.getOrElse("") &
    "@team [value]"       #> teamBox.getOrElse("") &
    "#workerTypeNormal [checked]"   #> workerTypeBox.filter(_ == "normal").map(_ => "checked") &
    "#workerTypeMaintain [checked]" #> workerTypeBox.filter(_ == "maintain").map(_ => "checked") &
    "type=submit" #> SHtml.onSubmitUnit(process _)
  }
}

class WorkerAdd extends WorkerEdit(Worker.createRecord) {
  override def checkWorkerID(workerID: String) = Worker.hasNoDuplicateID(workerID)
}

