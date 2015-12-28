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

/**
 *  處理「網站管理」－＞「員工列表」中新增員工的部份
 *
 *  因為新增員工＝修改一個剛新增的空的員工資料，所以此 Snippet  只要繼
 *  承修改「維修員工資料」項目的 WorkerEdit  類別，並傳入一個新的、
 *  空的 Worker 的 Record 物件就好。
 */
class WorkerAdd extends WorkerEdit(Worker.createRecord) {
  override def okMessage = s"已成功新增員工 ${worker.name.get}"
  override def checkWorkerID(workerID: String) = Worker.hasNoDuplicateID(workerID)
  override def checkWorkerIDMessage = "此工號已經存在"
}

/**
 *  處理「網站管理」－＞「員工列表」－＞「編輯」的部份
 */
class WorkerEdit(val worker: Worker) extends StatefulSnippet {
  
  private val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
  private var workerNameBox: Box[String] = Full(worker.name.get)          // 用來儲存 HTML 表單中輸入的員工姓名
  private var workerIDBox: Box[String] = Full(worker.workerID.get)        // 用來儲存 HTML 表單中輸入的工號
  private var onBoardDateBox: Box[Date] = Full(worker.onBoardDate.get)    // 用來儲存 HTML 表單中輸入的到職日
  private var departmentBox: Box[String] = Full(worker.department.get)    // 用來儲存 HTML 表單中輸入的部門
  private var teamBox: Box[String] = Full(worker.team.get)                // 用來儲存 HTML 表單中輸入的組別
  private var workerTypeBox: Box[String] = Full(worker.workerType.get)    // 用來儲存 HTML 表單中輸入的員工類型（生產／維修）

  /**
   *  成功修改後要顯示的訊息
   */
  def okMessage = s"已成功修改【${worker.name.get}】的資料"

  /**
   *  員工工號重覆時要顯示的錯誤訊息
   */
  def checkWorkerIDMessage = "此工號與其他員工工號重覆"

  /**
   *  檢查員工編號是否與資料庫內其他員工重覆
   *
   *  @param    workerID      台容的員工編號
   *  @return                 如果有重覆則為 true，否則為 false
   */
  def checkWorkerID(workerID: String) = {
    val hasDuplicateID = !Worker.hasNoDuplicateID(workerID, Some(worker))
    val notSamePerson = !(workerID != worker.workerID.get)
    hasDuplicateID && notSamePerson
  }

  /**
   *  檢查日期格式是否合法並轉換為 Box[java.util.Date] 物件
   *
   *  @param    dateString  要轉換的日期（格式應為 yyyy-MM-dd）
   *  @return               放了轉換過後的日期物件的 Box
   */
  def checkDate(dateString: String): Box[Date] = tryo {
    dateFormatter.parse(dateString)
  } match {
    case Full(date) => Full(date)
    case _ => Empty
  }

  /**
   *  當使用者送出表單後要做的事情
   *
   *  此函式會檢查使用者輸入的資料並更新資料庫中相對應的 Worker 物件。
   *
   */
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

  /**
   *  設定 HTML 表單及其行為
   */
  def render = {
    "@name [value]"         #> workerNameBox.getOrElse("") &
    "@workerID [value]"     #> workerIDBox.getOrElse("") &
    "@department [value]"   #> departmentBox.getOrElse("") &
    "@team [value]"         #> teamBox.getOrElse("") &
    "@onBoardDate [value]"  #> onBoardDateBox.map(dateFormatter.format).getOrElse("") &
    "#workerTypeNormal [checked]"   #> workerTypeBox.filter(_ == "normal").map(_ => "checked") &
    "#workerTypeMaintain [checked]" #> workerTypeBox.filter(_ == "maintain").map(_ => "checked") &
    "type=submit" #> SHtml.onSubmitUnit(process _)
  }

  /**
   *  指定 HTML 中呼叫的 Snippet 名稱要對應到哪些函式
   *
   *  為了讓使用者在輸入表單後，若有錯誤而無法進行時，原先輸入的值還會留在表單上，需
   *  要使用 StatefulSnippet 的機制，也就是讓此類別繼承自 StatefulSnippet 這個 trait。
   *
   *  但如果是 StatefulSnippet，會需要自行指定 HTML 模板中， data-lift="ChangePassword.render" 裡
   *  面的 "render" 對應到哪個函式。
   */
  def dispatch = {
    case "render" => render
  }

}


