package code.snippet

import code.lib._

import net.liftweb.util.Helpers._
import net.liftweb.http.S


class MonthlyReport {

  def getSteps(uri: List[String]) = uri match {
    case "monthly" :: year :: Nil =>
      List(
        Step(s"$year 年", true, Some(s"/monthly/$year")),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "monthly" :: year :: month :: Nil =>
      List(
        Step(s"$year 年", true, Some(s"/monthly/$year")),
        Step(s"$month 月", true, Some(s"/monthly/$year/$month")),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "monthly" :: year :: month :: week :: Nil =>
      List(
        Step(s"$year 年", true, Some(s"/monthly/$year")),
        Step(s"$month 月", true, Some(s"/monthly/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/monthly/$year/$month/$week")),
        Step("日期"),
        Step("機器")
      )

    case "monthly" :: year :: month :: week :: date :: Nil =>
      List(
        Step(s"$year 年", true, Some(s"/monthly/$year")),
        Step(s"$month 月", true, Some(s"/monthly/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/monthly/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/monthly/$year/$month/$week/$date")),
        Step("機器")
      )

    case "monthly" :: year :: month :: week :: date :: machineID :: Nil =>
      List(
        Step(s"$year 年", true, Some(s"/monthly/$year")),
        Step(s"$month 月", true, Some(s"/monthly/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/monthly/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/monthly/$year/$month/$week/$date")),
        Step(machineID, true, Some(s"/monthly/$year/$month/$week/$date/$machineID"))
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

    val Array(_, year, month, week, date, machineID) = S.uri.drop(1).split("/")

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

}


