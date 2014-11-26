package bootstrap.liftweb

import code.csv._

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.PlainTextResponse
import net.liftweb.util.BasicTypesHelpers.AsInt

object CsvRestAPI extends RestHelper {

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

}