package code.snippet

import code.lib._

import net.liftweb.util.Helpers._
import net.liftweb.http.S


class DailyReport {

  def getSteps(uri: List[String]) = uri match {

    case "daily" :: year :: month :: Nil =>
      List(
        Step(s"$year 年 $month 月", true, Some(s"/daily/$year/$month")),
        Step("日期"),
        Step("機器")
      )

    case "daily" :: year :: month :: date :: Nil =>
      List(
        Step(s"$year 年 $month 月", true, Some(s"/daily/$year/$month")),
        Step(s"$date 日", true, Some(s"/daily/$year/$month/$date")),
        Step("機器")
      )

    case "daily" :: year :: month :: date :: machineID :: Nil =>
      List(
        Step(s"$year 年 $month 月", true, Some(s"/daily/$year/$month")),
        Step(s"$date 日", true, Some(s"/daily/$year/$month/$date")),
        Step(s"$machineID", true, Some(s"/daily/$year/$month/$date/$machineID"))
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

    val Array(_, year, month, date, machineID) = S.uri.drop(1).split("/")

    "#productMachine [value]" #> machineID &
    "#fullYear [value]" #> year &
    "#month [value]" #> month &
    "#date [value]" #> date &
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    showStepsSelector
  }

  def render = {
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    showStepsSelector
  }

}


