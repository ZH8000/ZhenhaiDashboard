package code.snippet

import code.lib._
import code.model._
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.common._
import scala.xml.NodeSeq

/**
 *  用來顯示「產量統計」－＞「依容量」中的動態內容
 *
 */
class CapacityReport {

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

    case "capacity" :: Nil => 
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依容量", true, Some("/capacity")), 
        Step("工序"),
        Step("容量"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: Nil => 
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依容量", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step("容量"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: Nil => 
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依容量", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: year :: month :: Nil => 
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依容量", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step(s"$year-$month", true, Some(s"/capacity/$step/$capacity/$year/$month")),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: year :: month :: week :: Nil => 
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依容量", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step(s"$year-$month", true, Some(s"/capacity/$step/$capacity/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/capacity/$step/$capacity/$year/$month/$week")),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: year :: month :: week :: date :: Nil => 
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依容量", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step(s"$year-$month", true, Some(s"/capacity/$step/$capacity/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/capacity/$step/$capacity/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/capacity/$step/$capacity/$year/$month/$week/$date")),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: year :: month :: week :: date :: machineID :: Nil => 
      List(
        Step("產量統計", true, Some("/viewDetail")),
        Step("依容量", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step(s"$year-$month", true, Some(s"/capacity/$step/$capacity/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/capacity/$step/$capacity/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/capacity/$step/$capacity/$year/$month/$week/$date")),
        Step(s"$machineID", true, Some(s"/capacity/$step/$capacity/$year/$month/$week/$date/$machineID"))
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

    val Array(_, step, capacity, year, month, week, date, machineID) = S.uri.drop(1).split("/")
    val machineLevelBox = MachineLevel.find("machineID", machineID)
    val levelA = machineLevelBox.map(_.levelA.get)
    val levelB = machineLevelBox.map(_.levelB.get)
    val levelC = machineLevelBox.map(_.levelC.get)

    "#step [value]" #> step &
    "#capacity [value]" #> capacity &
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
    val Array(_, step, capacity, year, month, week, date, machineID) = S.uri.drop(1).split("/")
    EventSummaryTable(year.toInt, month.toInt, date.toInt, machineID)
  }


}


