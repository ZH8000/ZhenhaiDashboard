package bootstrap.liftweb

import code.util._
import code.json._
import code.csv._

import net.liftweb.http.LiftRules
import net.liftweb.http.Req

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.JsonResponse

import net.liftweb.common.{Box, Empty}
import net.liftweb.common.Box._

import com.mongodb.casbah.Imports._
import net.liftweb.http.PlainTextResponse

object ProductHelper extends RestHelper {

  import net.liftweb.util.BasicTypesHelpers.AsInt

  serve("api" / "json" / "total" prefix {
    case Nil Get req => 
      JsonResponse(TotalJSON.overview)
    case productName :: Nil Get req => 
      JsonResponse(TotalJSON(productName))
    case productName :: AsInt(year) :: AsInt(month) :: Nil Get req => 
      JsonResponse(TotalJSON(productName, year, month))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => 
      JsonResponse(TotalJSON(productName, year, month, week))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => 
      JsonResponse(TotalJSON(productName, year, month, week, date))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => 
      JsonResponse(TotalJSON(productName, year, month, week, date, machineID))
  })

  def toCSVResponse(csvString: String) = PlainTextResponse(csvString, List("Content-Type" -> "text/csv"), 200)

  serve("api" / "csv" / "total" prefix {
    case Nil Get req => toCSVResponse(TotalCSV.overview)
    case productName :: Nil Get req => toCSVResponse(TotalCSV(productName))
    case productName :: AsInt(year) :: AsInt(month) :: Nil Get req => toCSVResponse(TotalCSV(productName, year, month))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => toCSVResponse(TotalCSV(productName, year, month, week))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => toCSVResponse(TotalCSV(productName, year, month, week, date))
    case productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => toCSVResponse(TotalCSV(productName, year, month, week, date, machineID))
  })

  serve("api" / "csv" / "monthly" prefix {
    case AsInt(year) :: Nil Get req => toCSVResponse(MonthlyCSV(year))
    case AsInt(year) :: AsInt(month) :: Nil Get req => toCSVResponse(MonthlyCSV(year, month))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => toCSVResponse(MonthlyCSV(year, month, week))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => toCSVResponse(MonthlyCSV(year, month, week, date))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => toCSVResponse(MonthlyCSV(year, month, week, date, machineID))
  })

  serve("api" / "csv" / "daily" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => toCSVResponse(DailyCSV(year, month))
    case AsInt(year) :: AsInt(month) :: AsInt(date) :: Nil Get req => toCSVResponse(DailyCSV(year, month, date))
    case AsInt(year) :: AsInt(month) :: AsInt(date) :: machineID :: Nil Get req => toCSVResponse(DailyCSV(year, month, date, machineID))
  })


  serve("api" / "csv" / "machine" prefix {
    case Nil Get req => toCSVResponse(MachineCSV.overview)
    case machineType :: Nil Get req => toCSVResponse(MachineCSV(machineType))
    case machineType :: machineModel :: Nil Get req => toCSVResponse(MachineCSV(machineType, machineModel))
    case machineType :: machineModel :: machineID :: Nil Get req => toCSVResponse(MachineCSV(machineType, machineModel, machineID))
  })

  serve("api" / "json" / "monthly" prefix {
    case AsInt(year) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year))
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, month))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, month, week))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, month, week, date))
    case AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, month, week, date, machineID))
  })

  serve("api" / "json" / "daily" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      JsonResponse(DailyJSON(year, month))
    case AsInt(year) :: AsInt(month) :: AsInt(date) :: Nil Get req => 
      JsonResponse(DailyJSON(year, month, date))
    case AsInt(year) :: AsInt(month) :: AsInt(date) :: machineID :: Nil Get req => 
      JsonResponse(DailyJSON(year, month, date, machineID))
  })

  serve {
    case "api" :: "json" :: "alert" :: Nil Get req => JsonResponse(AlertJSON.overview)
  }

  serve("api" / "json" / "machine" prefix {
    case Nil Get req => JsonResponse(MachineJSON.overview)
    case machineType :: Nil Get req => JsonResponse(MachineJSON(machineType))
    case machineType :: machineModel :: Nil Get req => JsonResponse(MachineJSON(machineType, machineModel))
    case machineType :: machineModel :: machineID :: "pie" :: Nil Get req => JsonResponse(MachineJSON.detailPie(machineID))
    case machineType :: machineModel :: machineID :: "table" :: Nil Get req => JsonResponse(MachineJSON.detailTable(machineID))
  })


}

class Boot 
{

  import net.liftweb.sitemap._
  import net.liftweb.http.Templates
  import net.liftweb.sitemap.Loc.Template
  import scala.xml.NodeSeq

  private def getTemplate(path: String) = Template(() => Templates(path.split("/").toList) openOr NodeSeq.Empty)

  lazy val siteMap = SiteMap(
    Menu("Home") / "index",
    Menu("Dashboard") / "dashboard",
    Menu("Dashboard") / "alert",
    Menu("Total1") / "total" >> getTemplate("total/overview"),
    Menu("Total2") / "total" / * >> getTemplate("total/overview"),
    Menu("Total3") / "total" / * / * / * >> getTemplate("total/overview"),
    Menu("Total4") / "total" / * / * / * / * >> getTemplate("total/overview"),
    Menu("Total5") / "total" / * / * / * / * / * >> getTemplate("total/overview"),
    Menu("Total6") / "total" / * / * / * / * / * / * >> getTemplate("total/machine"),
    Menu("Monthly1") / "monthly" / * >> getTemplate("monthly/overview"),
    Menu("Monthly2") / "monthly" / * / * >> getTemplate("monthly/overview"),
    Menu("Monthly3") / "monthly" / * / * / * >> getTemplate("monthly/overview"),
    Menu("Monthly4") / "monthly" / * / * / * / * >> getTemplate("monthly/overview"),
    Menu("Monthly4") / "monthly" / * / * / * / * / * >> getTemplate("monthly/machine"),
    Menu("Daily1") / "daily" / * / * >> getTemplate("daily/overview"),
    Menu("Daily2") / "daily" / * / * / * >> getTemplate("daily/overview"),
    Menu("Daily2") / "daily" / * / * / * / * >> getTemplate("daily/machine"),
    Menu("Machine1") / "machine" >> getTemplate("machine/overview"),
    Menu("Machine1") / "machine" / * >> getTemplate("machine/overview"),
    Menu("Machine1") / "machine" / * / * >> getTemplate("machine/overview"),
    Menu("Machine1") / "machine" / * / * / * >> getTemplate("machine/detail")


  )

  val ensureSession: PartialFunction[Req, Unit] = {
    case req if true =>
  }

  def boot 
  {
    import net.liftweb.util.PartialFunctionWrapper
    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.addToPackages("code")
    LiftRules.setSiteMap(siteMap)
    LiftRules.dispatch.append(new PartialFunctionWrapper(ensureSession) guard ProductHelper)
  }
}
