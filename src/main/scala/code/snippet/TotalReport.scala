package code.snippet

import code.lib._

import net.liftweb.util.Helpers._
import net.liftweb.http.S


class TotalReport {

  def getSteps(uri: List[String]) = uri match {
    case "total" :: Nil => 
      List(
        Step("總覽", true, Some("/total")), 
        Step("Φ 別"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: product :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(product, true, Some(s"/total/$product")),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: product :: year :: month :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(product, true, Some(s"/total/$product")),
        Step(s"$year-$month", true, Some(s"/total/$product/$year/$month")),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "total" :: product :: year :: month :: week :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(product, true, Some(s"/total/$product")),
        Step(s"$year-$month", true, Some(s"/total/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$product/$year/$month/$week")),
        Step("日期"),
        Step("機器")
      )

    case "total" :: product :: year :: month :: week :: date :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(product, true, Some(s"/total/$product")),
        Step(s"$year-$month", true, Some(s"/total/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$product/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/total/$product/$year/$month/$week/$date")),
        Step("機器")
      )

    case "total" :: product :: year :: month :: week :: date :: machineID :: Nil =>
      List(
        Step("總覽", true, Some("/total")), 
        Step(product, true, Some(s"/total/$product")),
        Step(s"$year-$month", true, Some(s"/total/$product/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/total/$product/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/total/$product/$year/$month/$week/$date")),
        Step(machineID, true, Some(s"/total/$product/$year/$month/$week/$date/$machineID"))
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

    val Array(_, productName, year, month, week, date, machineID) = S.uri.drop(1).split("/")

    "#productName [value]" #> productName &
    "#productMachine [value]" #> machineID &
    "#fullYear [value]" #> year &
    "#month [value]" #> month &
    "#week [value]" #> week &
    "#date [value]" #> date &
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    showStepsSelector
  }

  def render = {
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    showStepsSelector
  }

}


