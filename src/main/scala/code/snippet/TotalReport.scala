package code.snippet

import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common._
import scala.xml.NodeSeq

/**
 *  用來顯示網頁上「產量統計」－＞「依φ別」的部份的 Snippet
 */
class TotalReport {

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
    case "total" :: Nil => 
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依φ別", true, Some("/total")), 
        Step("工序"),
        Step("Φ 別"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依φ別", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step("Φ 別"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: product :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依φ別", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: product :: year :: month :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依φ別", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step(s"$year-$month", true, Some(s"/total/$step/$product/$year/$month")),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: product :: year :: month :: week :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依φ別", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step(s"$year-$month", true, Some(s"/total/$step/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$step/$product/$year/$month/$week")),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: product :: year :: month :: week :: date :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依φ別", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step(s"$year-$month", true, Some(s"/total/$step/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$step/$product/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/total/$step/$product/$year/$month/$week/$date")),
        Step("機器")
      )

    case "total" :: step :: product :: year :: month :: week :: date :: machineID :: Nil =>
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依φ別", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step(s"$year-$month", true, Some(s"/total/$step/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$step/$product/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/total/$step/$product/$year/$month/$week/$date")),
        Step(machineID, true, Some(s"/total/$step/$product/$year/$month/$week/$date/$machineID"))
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

    val Array(_, step, productName, year, month, week, date, machineID) = S.uri.drop(1).split("/")
    val machineLevelBox = MachineLevel.find("machineID", machineID)
    val levelA = machineLevelBox.map(_.levelA.get)
    val levelB = machineLevelBox.map(_.levelB.get)
    val levelC = machineLevelBox.map(_.levelC.get)

    "#step [value]" #> step &
    "#productName [value]" #> productName &
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
    val Array(_, step, productName, year, month, week, date, machineID) = S.uri.drop(1).split("/")
    EventSummaryTable(year.toInt, month.toInt, date.toInt, machineID)
  }


}


