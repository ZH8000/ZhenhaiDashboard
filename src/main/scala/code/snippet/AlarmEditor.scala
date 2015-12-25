package code.snippet

import code.model._
import code.lib._

import net.liftweb.common.{Box, Full, Empty, Failure}
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmd
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers._
import java.text.SimpleDateFormat

/**
 *  新增「維修行事曆」中的項目
 *
 *  因為新增項目＝修改一個剛新增的空相目，所以此 Snippet  只要繼
 *  承修改「維修行事曆」項目的 AlarmEdit  類別，並傳入一個新的、
 *  空的 Alarm 的 Record 物件就好。
 *
 */
class AlarmAdd extends AlarmEdit(Alarm.createRecord)

/**
 *  修改「維修行事曆」中的項目
 *
 *  @param    alarm     要修改的維修行事曆項目
 *
 */
class AlarmEdit(alarm: Alarm) {

  /**
   *  從網址取得目前是修改哪一個製程的維修行事曆
   *
   *  S.uri = 瀏覽器上的網址，透過網址的格式，可以確認目前是修改
   *  或是新增，若為 {{{/management/alarms/edit/XXX}}}  的格式就
   *  是修改，若為 {{{/management/alarms/YYY/add}}} 的格式則為新
   *  增。
   *
   *  當是在修改的情況下，製程會由原本建構子中的的 Alarm  的物件
   *  的 step 成員變數取出，若是在新增的情況下，因為傳入的 Alarm
   *  物件還未有任何有用的資訊，因此是由上述網址格式中的 YYY  的
   *  部份來決定。
   *
   *  @return       要新增或的維修行事曆的製程（step1 = 加締 / step2 = 組立 / step3 = 老化 / step4 = 選別 / step5 = 加工切腳）
   *
   */
  private val step = {
    S.uri.split("/").drop(1).toList match {
      case "management" :: "alarms" :: "edit" :: alarmID :: Nil => alarm.step.get
      case "management" :: "alarms" :: step :: "add" :: Nil => step
      case _ => ""
    }
  }

  /**
   *  用來儲存 HTML 表單上的「機台編號」的欄位的內容
   */
  private var machineIDBox: Box[String] = Full(alarm.machineID.toString)

  /**
   *  用來儲存 HTML 表單上的「更換零件循環累計良品數」的欄位內容
   */
  private var countdownQtyBox: Box[String] = Full(alarm.countdownQty.toString).filter(_ != "0")

  /**
   *  用來儲存 HTML 表單上的「描述」的欄位內容
   */
  private var descriptionBox: Box[String] = Full(alarm.description.toString)

  /**
   *  當使用者按下表單下的「新增維修行事曆」按鈕時，會執行此函式來更改 Alarm 物件的
   *  內容，並顯示確認對話框。
   *
   */
  private def process(): JsCmd = {

    /**
     *  將 yyyy-MM-dd 格式的日期字串轉成 java.util.Date 物件
     *
     *  @param    date      yyyy-MM-dd 格式的日期字串
     *  @return             若可以正常轉換，則為 Full(相對應的 Date 物件)，否則為 Failure 物件
     */
    def toDate(date: String) = try {
      Full(new SimpleDateFormat("yyyy-MM-dd").parse(date))
    } catch {
      case e: Exception => Failure(s"$date 不是合法的日期格式")
    }

    var machineIDBox: Box[String]    = S.param("machineID")      // HTTP POST 中傳入的機台編號欄位
    var countdownQtyBox: Box[String] = S.param("countdownQty")   // HTTP POST 中傳入的更換零件循環累計良品數欄位
    var descriptionBox: Box[String]  = S.param("description")    // HTTP POST 中傳入的描述欄位

    //
    //  此 for-expression 會依序檢查以下事項：
    //
    //   1. machineIDBox 是否有值
    //   2. countQtyBox 是否有值
    //   3. countQtyBox 中的值是否能轉成整數
    //   4. descriptionBox 是否有值
    //   5. descriptionBox 中的值是否 <= 60 個字元
    //
    //   只有當以上條件均成立時，才會執行 yield 區塊中的程式碼，此情況下
    //   alarmRecord 變數會指到一個 Full[Alarm] 的物件，且其內容為更新過
    //   後的 Alarm  物件。
    //
    //   若以上的五個條件任何一項不成立，則 alarmRecord 會指到一個 Failure
    //   物件，該 Failure  物件的 message 欄位會被設為第一個不符合的條件
    //   的 ?~ 符號後的字串。
    //
    //
    val alarmRecord = for {
      machineID        <- machineIDBox.filterNot(_.trim.isEmpty) ?~ "請輸入維修機台"
      countdownQtyStr  <- countdownQtyBox.filterNot(_.trim.isEmpty) ?~ "請輸入目標良品數"
      countdownQty     <- asInt(countdownQtyStr)
      description      <- descriptionBox.filterNot(_.trim.isEmpty) ?~ "請輸入描述"
      _                <- descriptionBox.filterMsg("描述字數不能多於 60 字")(_.length <= 60)
    } yield {
      alarm.machineID(machineID)
           .step(step)
           .countdownQty(countdownQty)
           .description(description)
           .lastReplaceCount(MachineCounter.getCount(machineID))
    }

    // 檢查上述的 alarmRecord 物件
    alarmRecord match {
      // 如果成功就在 Client 端呼叫網頁上的 showModalDialog 的 JavaScript 函式
      // 並傳入機台編號和「更換零件循環累計良品數」來顯示確認對話框。
      case Full(alarm) =>
        val machineID = alarm.machineID
        JsRaw(s"""showModalDialog('$machineID', ${alarm.countdownQty});""")
      // 如果使用者輸入的資料有錯誤，則顯示相對應的錯誤訊息
      case Failure(msg, _, _) => S.error(msg)
      // 理論上不會到這行，因為在 for-expression 裡已經將 Empty 都用 ?~ 轉為
      // Failure 物件了，但為了讓編譯器不會發出警告，所以加入這個條件。
      case Empty => S.error("無法取得使用者輸入的資料")
    }
  }

  /**
   *  當使用者按下確認對話框的「確定」按鈕後，會執行此函式來將
   *  修過後的 Alarm  物件寫入資料庫中。
   *
   *  @param    value   HTML 中「確定」按鈕的 value 欄位，不會用到。
   *  @return           此函式完成後要在瀏覽器上執行的 JavaScript 程式
   */
  def onConfirm(value: String): JsCmd = {

    // 將修過後的 Alarm  物件寫入資料庫中，並確認其結果
    alarm.saveTheRecord() match {
      // 若成功則轉址到該製程的維修項目列表並透過 S.notice 在網頁上顯示通知
      case Full(alarm) => S.redirectTo(s"/management/alarms/${alarm.step}", () => S.notice("成功新增或修改維修行事曆"))
      // 若失敗則透過 S.error 輸出錯誤訊息
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("無法寫入資料庫")
    }
  }

  /**
   *  用來生成 HTML 模板中的動態資料，以及綁定 HTML 上的按鈕要
   *  執行哪個 Scala 函式。
   */
  def render = {

    // 取得此製程中所有的機台的機台編號
    val machineList = MachineInfo.machineInfoList.filter("step" + _.machineType == step).map(_.machineID)

    "@countdownQty [value]"     #> countdownQtyBox &  // 將 HTML 中 name="countQty" 元素的 value 值設成 countdownQtyBox 的值
    "@defaultMachineID [value]" #> machineIDBox &     // 將 HTML 中 name="defaultMachineID" 元素的 value 值設成 machineIDBox 的值
    "@description *"            #> descriptionBox &   // 將 HTML 中 name="description" 元素的 value 值設成 descriptionBox 的值
    ".machineItem" #> machineList.map { machineID =>  // 將 HTML 中 class="machineItem" 的元素，重覆 machineList.size 次，並
                                                      // 採用以下規則替代：
      ".machineItem [value]"  #> machineID &          // 將 class="machineItem" 元素的 value 值設為相對應的 machineID
      ".machineItem *"        #> machineID            // 將 class="machineItem" 元素的子元素（內容／文字）設為相對應的 machineID
    } &
    "type=submit" #> SHtml.ajaxOnSubmit(process) &    // 設定 HTML 中 type=submit 的元素在 submit 後要執行的程式碼
    "#modalOKButton [onclick]" #> SHtml.onEvent(onConfirm) // 設定 HTML 中 id="modalOKButton" 的元素按下去後要執行的程式碼
  }
}
