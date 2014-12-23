package bootstrap.liftweb

import code.json._
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.JsonResponse
import net.liftweb.util.BasicTypesHelpers.AsInt

object JsonRestAPI extends RestHelper {

  serve("api" / "json" / "total" prefix {
    case Nil Get req => 
      JsonResponse(TotalJSON.overview)
    case step :: Nil Get req => 
      JsonResponse(TotalJSON(step))
    case step :: product :: Nil Get req => 
      JsonResponse(TotalJSON(step, product))
    case step :: productName :: AsInt(year) :: AsInt(month) :: Nil Get req => 
      JsonResponse(TotalJSON(step, productName, year, month))
    case step :: productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => 
      JsonResponse(TotalJSON(step, productName, year, month, week))
    case step :: productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => 
      JsonResponse(TotalJSON(step, productName, year, month, week, date))
    case step :: productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => 
      JsonResponse(TotalJSON(productName, year, month, week, date, machineID))
  })

  serve("api" / "json" / "monthly" prefix {
    case AsInt(year) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year))
    case AsInt(year) :: step :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, step))
    case AsInt(year) :: step :: AsInt(month) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, step, month))
    case AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, step, month, week))
    case AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => 
      JsonResponse(MonthlyJSON(year, step, month, week, date))
    case AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => 
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

