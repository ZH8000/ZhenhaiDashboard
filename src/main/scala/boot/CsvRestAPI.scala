package bootstrap.liftweb

import code.csv._

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.PlainTextResponse
import net.liftweb.util.BasicTypesHelpers.AsInt

object CsvRestAPI extends RestHelper {

  def toCSVResponse(csvString: String) = PlainTextResponse(csvString, List("Content-Type" -> "text/csv"), 200)

  serve("api" / "csv" / "total" prefix {
    case Nil Get req => toCSVResponse(TotalCSV.overview)
    case step :: Nil Get req => toCSVResponse(TotalCSV(step))
    case step :: productName :: Nil Get req => toCSVResponse(TotalCSV(step, productName))
    case step :: productName :: AsInt(year) :: AsInt(month) :: Nil Get req => toCSVResponse(TotalCSV(step, productName, year, month))
    case step :: productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => toCSVResponse(TotalCSV(step, productName, year, month, week))
    case step :: productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => toCSVResponse(TotalCSV(step, productName, year, month, week, date))
    case step :: productName :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => toCSVResponse(TotalCSV(productName, year, month, week, date, machineID))
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


  serve("api" / "csv" / "workers" prefix {
    case Nil Get req => toCSVResponse(WorkerStatisticsCSV.overview)
    case workerMongoID :: Nil Get req => toCSVResponse(WorkerStatisticsCSV(workerMongoID))
    case workerMongoID :: yearAndMonth :: Nil Get req => toCSVResponse(WorkerStatisticsCSV(workerMongoID, yearAndMonth))
    case workerMongoID :: yearAndMonth :: week :: Nil Get req => toCSVResponse(WorkerStatisticsCSV(workerMongoID, yearAndMonth, week))
    case workerMongoID :: yearAndMonth :: week :: date :: Nil Get req => toCSVResponse(WorkerStatisticsCSV(workerMongoID, yearAndMonth, week, date))

  })

  serve("api" / "csv" prefix {
    case "orderStatus" :: Nil Get req => toCSVResponse(OrderStatusCSV())
    case "todayOrder" :: Nil Get req => toCSVResponse(TodayOrderCSV())
    case "machineMaintainLog" :: Nil Get req => toCSVResponse(MachineMaintainLogCSV())
  })

}
