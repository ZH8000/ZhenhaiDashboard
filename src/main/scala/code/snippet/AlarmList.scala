package code.snippet

import code.model._

import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.jquery.JqJsCmds.Hide

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import scala.xml.NodeSeq

/**
 *  此類別用來顯示網頁上「維修行事曆」的資料
 *
 */
class AlarmList {

  /**
   *  依照網址取得相對應的維修行事曆在資料庫中的記錄，並以 List[Alarm] 的資料結構傳回。
   *
   *  若是在 /dashboard 頁面，就是所有的維修行事曆項目，若是在「網站管理」－＞「維修行
   *  事曆」的頁面中，則是只會列出相對應的製程的 Alarm 物件。
   *
   *  @return     依照網址決定的 Alarm 物件的 List
   *
   */
  private def alarms = {
    S.uri.split("/").drop(1) match {
      case Array("dashboard") => Alarm.findAll.toList.sortWith(_.machineID.get < _.machineID.get)
      case Array(_, _, step)  => Alarm.findAll("step", step).toList.sortWith(_.machineID.get < _.machineID.get)
    }
  }

  /**
   *  (List[已經要更換的 Alarm], List[尚未達到更換數的 Alarm])
   *
   *  urgentAlarms 會是上述 alarms 變數中已經累計良品數達到需要更換的 Alarm 物件的 List，
   *  normalAlarms 則是 alarms 變數中尚累計良品數未達到需要更換的 Alarm 物件的 List。
   *
   */
  private val (urgentAlarms, normalAlarms) = alarms.partition(_.isUrgentEvent)

  /**
   *  從資料庫中刪除 Alarm 物件
   *
   *  @param      alarm       要刪除的 Alarm 物件
   *  @param      value       在 HTML 上的元素的 valeue 屬性，只是為了符合 Framework 的 API 而加已，不會用到
   */
  def deleteAlarm(alarm: Alarm)(value: String) = {
   
    alarm.delete_! match {
      case true => 
        S.notice(s"已刪除【${alarm.machineID} / ${alarm.description}】 此筆記錄")
        Hide(s"row-${alarm.id}")
      case false => 
        S.error(s"無法刪除【${alarm.machineID} / ${alarm.description}】 此筆記錄")
        Noop
    }
  }

  /**
   *  在 Dashboard 主頁右邊的「更換提示」中，標記完成的對話框按下去後要執行的動作
   *
   *  在資料庫中將此 Alarm 物件設為已更換。
   *
   *  @param    alarm     要標記為已更換的 Alarm 物件
   *  @param    value     在 HTML 上的元素的 valeue 屬性，只是為了符合 Framework 的 API 而加已，不會用到
   *  @return             此函式執行完畢後要在 Client 端執行的 JavaScript 程式碼
   */
  def markAsDoneInPostIt(alarm: Alarm, value: String): JsCmd = {

    val machineIDToCounter = MachineCounter.toHashMap
    val countQty = machineIDToCounter.get(alarm.machineID.get).getOrElse(0L)

    val newRecord = 
      alarm.isDone(true)
           .doneTime(new Date)
           .lastReplaceCount(countQty)
           .replacedCounter(alarm.replacedCounter.get + 1)
           .saveTheRecord()

    newRecord match {
      case Full(record) => JsRaw(s"""$$('#row-${alarm.id}').remove()""")
      case _ => S.error("無法存檔")
    }
  }

  /**
   *  在「網頁管理」－＞「維修行事曆」中，標記完成的對話框按下去後要執行的動作
   *
   *  在資料庫中將此 Alarm 物件設為已更換。
   *
   *  @param    alarm     要標記為已更換的 Alarm 物件
   *  @param    value     在 HTML 上的元素的 valeue 屬性，只是為了符合 Framework 的 API 而加已，不會用到
   *  @return             此函式執行完畢後要在 Client 端執行的 JavaScript 程式碼
   */
  def markAsDone(alarm: Alarm, value: String): JsCmd = {

    val machineIDToCounter = MachineCounter.toHashMap
    val countQty = machineIDToCounter.get(alarm.machineID.get).getOrElse(0L)
    val nextCount = countQty + alarm.countdownQty.get
    val newReplacedCounter = alarm.replacedCounter.get + 1

    val newRecord = 
      alarm.isDone(true)
           .doneTime(new Date)
           .lastReplaceCount(countQty)
           .replacedCounter(newReplacedCounter)
           .doneUser(User.CurrentUser.map(_.username.get).getOrElse("Unknown"))
           .saveTheRecord()

    newRecord match {
      case Full(record) => JsRaw(s"""updateUI('${alarm.id}', $nextCount, $newReplacedCounter)""")
      case _ => S.error("無法存檔"); Noop
    }
  }

  /**
   *  用來顯示 Dashboard 主頁右側的「零件定期更換通知」的列表
   */
  def postIt = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val machineIDToCounter = MachineCounter.toHashMap

    // 針對每一個 row，用此函式中中的規則以及傳入的 Alarm 物件來轉換
    def rowItem(alarm: Alarm) = {
      
      val countQty = machineIDToCounter.get(alarm.machineID.get).getOrElse(0L)

      ".item [id]" #> s"row-${alarm.id}" &
      ".machineID *" #> alarm.machineID &
      ".countQty *" #> countQty &
      ".countdownQty *" #> alarm.countdownQty &
      ".desc *" #> alarm.description &
      ".machineID [id]"    #> s"machineID-${alarm.id}" &
      ".desc [id]"  #> s"description-${alarm.id}" &
      ".machineID [class+]"    #> (if (alarm.isDone.get) "disabled" else "") &
      ".desc [class+]"  #> (if (alarm.isDone.get) "del" else "") &
      ".doneCheckbox [onclick]" #> SHtml.onEventIf(
        s"是否確【${alarm.machineID}】的【${alarm.description}】認標記成已完成", 
        markAsDoneInPostIt(alarm, _)
      )
    }

    // 只取出已經需要更換，而且尚未更換的 Alarm 物件
    val notDoneUrgent = urgentAlarms.filterNot(_.isDone.get)

    notDoneUrgent.isEmpty match {
      case true  => ".urgentAlarmBlock" #> NodeSeq.Empty
      case flase => 
        ".nonAlarm"         #> NodeSeq.Empty &
        ".urgentAlarmRow"   #> notDoneUrgent.map { alarm => rowItem(alarm) } 
    }
  }

  /**
   *  用來顯示網頁「網站管理」－＞「維修行事曆」上的列表中的「製程」部份的標頭
   */
  def breadcrumb = {
   
    val Array(_, _, step) = S.uri.split("/").drop(1)

    val machineTypeTitle = step match {
      case "step1" => "加締"
      case "step2" => "組立"
      case "step3" => "老化"
      case "step4" => "選別"
      case "step5" => "加工"
      case _ => "Unknown"
    }

    "#machineTypeTitle *" #> machineTypeTitle
  }

  /**
   *  用來顯示網頁「網站管理」－＞「維修行事曆」上的列表
   */
  def render = {

    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")
    val machineIDToCounter = MachineCounter.toHashMap

    val machineIDs = alarms.map(_.machineID.get).distinct.sortWith(_ < _)

    ".machineRow" #> machineIDs.map { machineID =>
      val countQty = machineIDToCounter.get(machineID).getOrElse(0L)
      val wanQty = countQty / 10000.0

      ".countQty *" #> countQty &
      ".machineID *" #> machineID &
      ".countQtyWan *" #> s"%.1f".format(wanQty) &
      ".alarmRow" #> alarms.view
                           .filter(_.machineID.get == machineID)
                           .sortWith(_.countdownQty.get < _.countdownQty.get)
                           .map { alarm =>

        val nextCount = alarm.lastReplaceCount.get match {
          case 0     => countQty + alarm.countdownQty.get
          case count => count + alarm.countdownQty.get
        }

        val doneCheckBox = alarm.isDone.get match {
          case true  =>
            ".doneCheckboxHolder *" #> <span>Ｖ</span>
          case false =>
            ".doneCheckbox [onclick]" #> SHtml.onEventIf(
              s"是否確【${alarm.machineID}】的【${alarm.description}】認標記成已完成", 
              markAsDone(alarm, _)
            )

        }

        ".alarmRow [id]" #> s"row-${alarm.id}" &
        ".description *" #> alarm.description &
        ".countdownQty *" #> alarm.countdownQty &
        ".nextCount *" #> nextCount &
        ".nextCount [id]" #> s"nextCount-${alarm.id}" &
        ".doneCheckboxHolder [id]" #> s"doneCheckboxHolder-${alarm.id}" &
        ".countQty *" #> countQty &
        ".editLink [href]" #> s"/management/alarms/edit/${alarm.id}" &
        ".replacedCounter *" #> alarm.replacedCounter &
        ".replacedCounter [id]" #> s"replacedCounter-${alarm.id}" &
        ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${alarm.machineID} / ${alarm.description}】嗎？", deleteAlarm(alarm)_) &
        doneCheckBox
      }

    }
  }

  /**
   *  用來建立「網頁管理」－＞「維修行事曆」－＞「製程」中的「新增」按鈕點下去
   *  時要連到哪個網址。
   */
  def addLink = {
    val Array(_, _, step) = S.uri.drop(1).split("/")

    "#addLink [href]" #> s"/management/alarms/$step/add"
  }
}

