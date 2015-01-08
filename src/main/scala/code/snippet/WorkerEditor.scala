package code.snippet

import code.model._

import net.liftweb.common.{Box, Full, Empty, Failure}

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.StatefulSnippet

import net.liftweb.util.Helpers._
import java.util.Date
import java.text.SimpleDateFormat
import scala.util.Try

class WorkerEdit(val worker: Worker) extends StatefulSnippet {

  
  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  private var workerNameBox: Box[String] = Full(worker.name.get)
  private var workerIDBox: Box[String] = Full(worker.workerID.get)
  private var onBoardDateBox: Box[Date] = Full(worker.onBoardDate.get)
  private var departmentBox: Box[String] = Full(worker.department.get)
  private var teamBox: Box[String] = Full(worker.team.get)
  private var workerTypeBox: Box[String] = Full(worker.workerType.get)

  def dispatch = {
    case "render" => render
  }

  def okMessage = s"已成功修改【${worker.name.get}】的資料"
  def checkWorkerID(workerID: String) = {
    val hasDuplicateID = !Worker.hasNoDuplicateID(workerID, Some(worker))
    val notSamePerson = !(workerID != worker.workerID.get)
    hasDuplicateID && notSamePerson
  }
  def checkWorkerIDMessage = "此工號與其他員工工號重覆"

  def checkDate(dateString: String): Box[Date] = tryo {
    dateFormatter.parse(dateString)
  } match {
    case Full(date) => Full(date)
    case _ => Empty
  }


  def process() {
    workerNameBox = S.param("name").filterNot(_.trim.isEmpty)
    workerTypeBox = S.param("workerType").filterNot(_.trim.isEmpty)
    workerIDBox = S.param("workerID").filterNot(_.trim.isEmpty)
    departmentBox = S.param("department").filterNot(_.trim.isEmpty)
    teamBox = S.param("team").filterNot(_.trim.isEmpty)
    onBoardDateBox = S.param("onBoardDate").flatMap(checkDate)

    val result = for {
      workerName      <- workerNameBox ?~ "姓名為必填欄位"
      workerID        <- workerIDBox ?~ "工號為必填欄位"
      noDuplicateID   <- workerIDBox.filterMsg(checkWorkerIDMessage)(checkWorkerID _)
      department      <- departmentBox ?~ "部門為必填欄位"
      team            <- teamBox ?~ "組別為必填欄位"
      onBoardDate     <- onBoardDateBox ?~ "入廠日期格式錯誤"
      workerType      <- workerTypeBox ?~ "員工類別"
      worker          <- worker.name(workerName)
                                .workerID(workerID)
                                .department(department)
                                .onBoardDate(onBoardDate)
                                .team(team)
                                .workerType(workerType)
                                .saveTheRecord()
    } yield worker

    result match {
      case Full(worker) => S.redirectTo("/management/workers/", () => S.notice(okMessage))
      case Failure(x, _, _) => S.error(x)
      case _ =>
    }
  }

  def render = {
    "@name [value]"       #> workerNameBox.getOrElse("") &
    "@workerID [value]"   #> workerIDBox.getOrElse("") &
    "@department [value]" #> departmentBox.getOrElse("") &
    "@team [value]"       #> teamBox.getOrElse("") &
    "@onBoardDate [value]" #> onBoardDateBox.map(dateFormatter.format).getOrElse("") &
    "#workerTypeNormal [checked]"   #> workerTypeBox.filter(_ == "normal").map(_ => "checked") &
    "#workerTypeMaintain [checked]" #> workerTypeBox.filter(_ == "maintain").map(_ => "checked") &
    "type=submit" #> SHtml.onSubmitUnit(process _)
  }
}

class WorkerAdd extends WorkerEdit(Worker.createRecord) {
  override def okMessage = s"已成功新增員工 ${worker.name.get}"
  override def checkWorkerID(workerID: String) = Worker.hasNoDuplicateID(workerID)
  override def checkWorkerIDMessage = "此工號已經存在"
}

