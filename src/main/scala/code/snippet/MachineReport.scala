package code.snippet

import code.lib._

import net.liftweb.util.Helpers._
import net.liftweb.http.S


class MachineReport {

  def getSteps(uri: List[String]) = uri match {

    case "machine" :: Nil =>
      List(
        Step("總覽", true, Some(s"/machine")),
        Step("製程"),
        Step("機種"),
        Step("機器")
      )

    case "machine" :: machineType :: Nil =>
      List(
        Step("總覽", true, Some(s"/machine")),
        Step(s"製程：${urlDecode(machineType)}", true, Some(s"/machine/$machineType")),
        Step("機種"),
        Step("機器")
      )

    case "machine" :: machineType :: machineModel :: Nil =>
      List(
        Step("總覽", true, Some(s"/machine")),
        Step(s"製程：${urlDecode(machineType)}", true, Some(s"/machine/$machineType")),
        Step(s"機種：${machineModel}", true, Some(s"/machine/$machineType/$machineModel")),
        Step("機器")
      )

    case "machine" :: machineType :: machineModel :: machineID :: Nil =>
      List(
        Step("總覽", true, Some(s"/machine")),
        Step(s"製程：${urlDecode(machineType)}", true, Some(s"/machine/$machineType")),
        Step(s"機種：${machineModel}", true, Some(s"/machine/$machineType/$machineModel")),
        Step(machineID, true, Some(s"/machine/$machineType/$machineModel/$machineID"))
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

    val Array(_, _, _, machineID) = S.uri.drop(1).split("/")

    "#machineID [value]" #> machineID &
    "#tableDataURL [value]" #> s"/api/json${S.uri}/table" &
    "#pieChartDataURL [value]" #> s"/api/json${S.uri}/pie" &
    showStepsSelector
  }

  def render = {
    "#dataURL [value]" #> s"/api/json${S.uri}" &
    showStepsSelector
  }

}


