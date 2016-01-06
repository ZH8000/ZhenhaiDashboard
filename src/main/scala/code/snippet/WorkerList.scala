package code.snippet

import java.text.SimpleDateFormat

import code.model._
import net.liftweb.common.Full
import net.liftweb.http.{S, SHtml}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.util.Helpers._

/**
 *  用來顯示員工列表的 Snippet
 */
class WorkerList {

  /**
   *  從「部門」對應到「該部門的員工列表」的 Map 物件
   */
  private val departmentToWorkers = Worker.findAll.filterNot(_.isDeleted.get).groupBy(_.department.get)

  /**
   *  部門列表
   */
  private val departments = departmentToWorkers.keySet.toList.sortWith(_ < _)

  /**
   *  刪除資料庫中的員工紀錄
   *
   *  注意，此函式不會真的刪除該員工的 Record，而是將資料表的 isDeleted 欄位標記為 true，
   *  因為如果直接刪除的話，之前這位員工的生產紀錄的 Record 中會找不到相對應的員工。
   *
   *  @param    worker      要刪除的員工
   *  @param    value       從 HTML 傳入的資料，沒有用到
   */
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

  private var workers = Set.empty[String]

  def updateBarcodeList(workerID: String)(isChecked: Boolean) = {
    
    if (isChecked) {
      workers += workerID
    } else {
      workers -= workerID
    }

    println(workers)
  }

  /**
   *  用來顯示員工列表
   */
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
        ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${worker.name}】嗎？", deleteWorker(worker)_) &
        ".barcodeCheckbox [value]" #> worker.id.toString &
        ".barcodeLink [href]" #> s"/management/workers/barcode?workerID=${worker.id}"
      }
    } &
    ".departmentItem" #> departments.zipWithIndex.map { case (department, index) => 
      ".departmentItem *" #> department &
      ".departmentItem [data-tab]" #> department &
      ".departmentItem [class+]" #> (if (index == 0) "active" else "")
    }
  }
}

