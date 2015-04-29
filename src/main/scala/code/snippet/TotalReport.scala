package code.snippet

import code.lib._

import net.liftweb.util.Helpers._
import net.liftweb.http.S

class TotalReport {

  def stepTitle(step: String) = MachineInfo.machineTypeName.get(step.toInt).getOrElse("Unknown")

  def getSteps(uri: List[String]) = uri match {
    case "total" :: Nil => 
      List(
        Step("總覽", true, Some("/total")), 
        Step("工序"),
        Step("Φ 別"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step("Φ 別"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: product :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: product :: year :: month :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step(s"$year-$month", true, Some(s"/total/$step/$product/$year/$month")),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: product :: year :: month :: week :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step(s"$year-$month", true, Some(s"/total/$step/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$step/$product/$year/$month/$week")),
        Step("日期"),
        Step("機器")
      )

    case "total" :: step :: product :: year :: month :: week :: date :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step(s"$year-$month", true, Some(s"/total/$step/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$step/$product/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/total/$step/$product/$year/$month/$week/$date")),
        Step("機器")
      )

    case "total" :: step :: product :: year :: month :: week :: date :: machineID :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/total/$step")),
        Step(product, true, Some(s"/total/$step/$product")),
        Step(s"$year-$month", true, Some(s"/total/$step/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$step/$product/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/total/$step/$product/$year/$month/$week/$date")),
        Step(machineID, true, Some(s"/total/$step/$product/$year/$month/$week/$date/$machineID"))
      )

    case _ => Nil

  }

  def showStepsSelector = {
    val steps = getSteps(S.uri.drop(1).split("/").toList)

    ".step" #> steps.map { step => 
      "a [href]" #> step.link &
      "a *" #> step.title &
      "a [class+]" #> (if (step.isActive) "active" else "")
    }

  }
  
  def machine = {

    val Array(_, step, productName, year, month, week, date, machineID) = S.uri.drop(1).split("/")

    "#step [value]" #> step &
    "#productName [value]" #> productName &
    "#productMachine [value]" #> machineID &
    "#fullYear [value]" #> year &
    "#month [value]" #> month &
    "#week [value]" #> week &
    "#date [value]" #> date &
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    "#csvURL [href]" #> s"/api/csv${S.uri}" &
    showStepsSelector
  }

  def render = {
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    "#csvURL [href]" #> s"/api/csv${S.uri}" &
    showStepsSelector
  }

  def summary = {
    val Array(_, step, productName, year, month, week, date, machineID) = S.uri.drop(1).split("/")
    EventSummaryTable(year.toInt, month.toInt, date.toInt, machineID)
  }


}


