package code.snippet

import code.lib._
import code.model._
import com.mongodb.casbah.Imports._

import net.liftweb.util.Helpers._
import net.liftweb.http.S

class CapacityReport {

  def stepTitle(step: String) = MachineInfo.machineTypeName.get(step.toInt).getOrElse("Unknown")

  def getSteps(uri: List[String]) = uri match {

    case "capacity" :: Nil => 
      List(
        Step("總覽", true, Some("/capacity")), 
        Step("工序"),
        Step("容量"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: Nil => 
      List(
        Step("總覽", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step("容量"),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: Nil => 
      List(
        Step("總覽", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step("月份"),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: year :: month :: Nil => 
      List(
        Step("總覽", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step(s"$year-$month", true, Some(s"/capacity/$step/$capacity/$year/$month")),
        Step("週"),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: year :: month :: week :: Nil => 
      List(
        Step("總覽", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step(s"$year-$month", true, Some(s"/capacity/$step/$capacity/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/capacity/$step/$capacity/$year/$month/$week")),
        Step("日期"),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: year :: month :: week :: date :: Nil => 
      List(
        Step("總覽", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step(s"$year-$month", true, Some(s"/capacity/$step/$capacity/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/capacity/$step/$capacity/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/capacity/$step/$capacity/$year/$month/$week/$date")),
        Step("機器")
      )

    case "capacity" :: step :: capacity :: year :: month :: week :: date :: machineID :: Nil => 
      List(
        Step("總覽", true, Some("/capacity")), 
        Step(stepTitle(urlDecode(step)), true, Some(s"/capacity/$step")),
        Step(s"${urlDecode(capacity)} Φ", true, Some(s"/capacity/$step/$capacity")),
        Step(s"$year-$month", true, Some(s"/capacity/$step/$capacity/$year/$month")),
        Step(s"第 $week 週", true, Some(s"/capacity/$step/$capacity/$year/$month/$week")),
        Step(s"$date 日", true, Some(s"/capacity/$step/$capacity/$year/$month/$week/$date")),
        Step(s"$machineID", true, Some(s"/capacity/$step/$capacity/$year/$month/$week/$date/$machineID"))
      )

    case _ => Nil
  }

  def machine = {

    val Array(_, step, capacity, year, month, week, date, machineID) = S.uri.drop(1).split("/")

    "#step [value]" #> step &
    "#capacity [value]" #> capacity &
    "#productMachine [value]" #> machineID &
    "#fullYear [value]" #> year &
    "#month [value]" #> month &
    "#week [value]" #> week &
    "#date [value]" #> date &
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    "#csvURL [href]" #> s"/api/csv${S.uri}" &
    showStepsSelector
  }



  def showStepsSelector = {
    val steps = getSteps(S.uri.drop(1).split("/").toList)

    ".step" #> steps.map { step => 
      "a [href]" #> step.link &
      "a *" #> step.title &
      "a [class+]" #> (if (step.isActive) "active" else "")
    }

  }

  def render = {
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    "#csvURL [href]" #> s"/api/csv${S.uri}" &
    showStepsSelector
  }

  def summary = {
    val Array(_, step, capacity, year, month, week, date, machineID) = S.uri.drop(1).split("/")
    EventSummaryTable(year.toInt, month.toInt, date.toInt, machineID)
  }


}


