package code.snippet

import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common._
import scala.xml.NodeSeq
import java.util.Date
import java.text.SimpleDateFormat

/**
 *  用來顯示「產量統計」－＞「月報表」的 Snippet
 */
class MonthlyReport {

  /**
   *  依照製程代碼顯示製程名稱
   *
   *  @param      step      製程代碼（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @return               製程名稱
   */
  def stepTitle(step: String) = MachineInfo.machineTypeName.get(step.toInt).getOrElse("Unknown")

  /**
   *  依照網址來產生網頁上顯示麵包屑要用的的 List[Step] 物件
   *
   *  @param      uri       瀏覽器上的網址用 / 分隔後的 List
   *  @return               代表麵包屑內容的 List[Step] 物件
   */
  def getSteps(uri: List[String]) = uri match {

    case "monthly" :: year :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step(s"月報表－$year 年", true, Some(s"/monthly/$year")),
        Step("工序"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "monthly" :: year :: step :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step(s"月報表－$year 年", true, Some(s"/monthly/$year")),
        Step(stepTitle(urlDecode(step)), true, Some(s"/monthly/$year/$step")),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )


    case "monthly" :: year :: step :: month :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step(s"月報表－$year 年", true, Some(s"/monthly/$year")),
        Step(stepTitle(urlDecode(step)), true, Some(s"/monthly/$year/$step")),
        Step(s"$month 月", true, Some(s"/monthly/$year/$step/$month")),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "monthly" :: year :: step :: month :: week :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step(s"月報表－$year 年", true, Some(s"/monthly/$year")),
        Step(stepTitle(urlDecode(step)), true, Some(s"/monthly/$year/$step")),
        Step(s"$month 月", true, Some(s"/monthly/$year/$step/$month")),
        Step(s"第 $week 週", true, Some(s"/monthly/$year/$step/$month/$week")),
        Step("日期"),
        Step("機器")
      )

    case "monthly" :: year :: step :: month :: week :: date :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step(s"月報表－$year 年", true, Some(s"/monthly/$year")),
        Step(stepTitle(urlDecode(step)), true, Some(s"/monthly/$year/$step")),
        Step(s"$month 月", true, Some(s"/monthly/$year/$step/$month")),
        Step(s"第 $week 週", true, Some(s"/monthly/$year/$step/$month/$week")),
        Step(s"$date 日", true, Some(s"/monthly/$year/$step/$month/$week/$date")),
        Step("機器")
      )

    case "monthly" :: year :: step :: month :: week :: date :: machineID :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step(s"月報表－$year 年", true, Some(s"/monthly/$year")),
        Step(stepTitle(urlDecode(step)), true, Some(s"/monthly/$year/$step")),
        Step(s"$month 月", true, Some(s"/monthly/$year/$step/$month")),
        Step(s"第 $week 週", true, Some(s"/monthly/$year/$step/$month/$week")),
        Step(s"$date 日", true, Some(s"/monthly/$year/$step/$month/$week/$date")),
        Step(machineID, true, Some(s"/monthly/$year/$step/$month/$week/$date/$machineID"))
      )

    case _ => Nil

  }

  /**
   *  用來顯示麵包屑
   */
  def showStepsSelector = {
    val steps = getSteps(S.uri.drop(1).split("/").toList)

    ".step" #> steps.map { step => 
      "a [href]" #> step.link &
      "a *" #> step.title &
      "a [class+]" #> (if (step.isActive) "active" else "")
    }

  }
  
  /**
   *  用來顯示最後一頁機台詳細統計紀錄
   */
  def machine = {

    val Array(_, year, step, month, week, date, machineID) = S.uri.drop(1).split("/")
    val machineLevelBox = MachineLevel.find("machineID", machineID)
    val levelA = machineLevelBox.map(_.levelA.get)
    val levelB = machineLevelBox.map(_.levelB.get)
    val levelC = machineLevelBox.map(_.levelC.get)

    "#step [value]" #> step &
    "#productMachine [value]" #> machineID &
    "#fullYear [value]" #> year &
    "#month [value]" #> month &
    "#week [value]" #> week &
    "#date [value]" #> date &
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    "#csvURL [href]" #> s"/api/csv${S.uri}.csv" &
    "#levelA [value]" #> levelA.map(_.toString).getOrElse("") &
    "#levelB [value]" #> levelB.map(_.toString).getOrElse("") &
    "#levelC [value]" #> levelC.map(_.toString).getOrElse("") &
    "#levelAExample" #> levelA.map { levelValue => ".levelAValue *" #> (levelValue / 12) } &
    "#levelBExample" #> levelB.map { levelValue => ".levelBValue *" #> (levelValue / 12) } &
    "#levelCExample" #> levelC.map { levelValue => ".levelCValue *" #> (levelValue / 12) } &
    showStepsSelector
  }

  /**
   *  用來顯示各頁的長條圖
   */
  def render = {
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    "#csvURL [href]" #> s"/api/csv${S.uri}.csv" &
    showStepsSelector
  }

  /**
   *  用來最後一層機台狀態頁面下方的事件統計表
   */
  def summary = {
    val Array(_, year, step, month, week, date, machineID) = S.uri.drop(1).split("/")
    EventSummaryTable(year.toInt, month.toInt, date.toInt, machineID)
  }

  /**
   *  用來最後一層機台狀態頁顯示今日工單列表
   */
  def lotNoList = {
    val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val Array(_, year, step, month, week, date, machineID) = S.uri.drop(1).split("/")
    val shiftDate = "%d-%02d-%02d".format(year.toInt, month.toInt, date.toInt)
    val lotNoList = DailyLotNoPartNo.findBy(machineID, shiftDate)

    ".dataRow" #> lotNoList.map { record =>
      val urlEncodedLotNo = urlEncode(record.lotNo.get)
      val productionCardURL = s"/productionCard/$urlEncodedLotNo"

      ".lotNo *" #> record.lotNo &
      ".lotNo [href]" #> productionCardURL &
      ".partNo *" #> record.partNo &
      ".lastStatus *" #> MachineStatusMapping.getDescription(record.lastStatus.get) &
      ".lastUpdated *" #> dateFormatter.format(new Date(record.lastUpdated.get * 1000))
    }
  }

}


