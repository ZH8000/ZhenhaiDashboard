package bootstrap.liftweb

import code.csv._
import code.lib._

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
    case AsInt(year) :: step :: Nil Get req => toCSVResponse(MonthlyCSV(year, step))
    case AsInt(year) :: step :: AsInt(month) :: Nil Get req => toCSVResponse(MonthlyCSV(year, step, month))
    case AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: Nil Get req => toCSVResponse(MonthlyCSV(year, step, month, week))
    case AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => toCSVResponse(MonthlyCSV(year, step, month, week, date))
    case AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => toCSVResponse(MonthlyCSV(year, month, week, date, machineID))
  })

  serve("api" / "csv" / "daily" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => toCSVResponse(DailyCSV(year, month))
    case AsInt(year) :: AsInt(month) :: step :: Nil Get req => toCSVResponse(DailyCSV(year, month, step))
    case AsInt(year) :: AsInt(month) :: step :: AsInt(date) :: Nil Get req => toCSVResponse(DailyCSV(year, month, step, date))
    case AsInt(year) :: AsInt(month) :: step :: AsInt(date) :: machineID :: Nil Get req => toCSVResponse(DailyCSV(year, month, step, date, machineID))
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

  serve("api" / "csv" / "capacity" prefix {
    case Nil Get req => toCSVResponse(CapacityCSV.overview)
    case step :: Nil Get req => toCSVResponse(CapacityCSV(step))
    case step :: capacity :: Nil Get req => toCSVResponse(CapacityCSV(step, capacity))
    case step :: capacity :: AsInt(year) :: AsInt(month) :: Nil Get req => toCSVResponse(CapacityCSV(step, capacity, year, month))
    case step :: capacity :: AsInt(year) :: AsInt(month) :: AsInt(week) :: Nil Get req => toCSVResponse(CapacityCSV(step, capacity, year, month, week))
    case step :: capacity :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil Get req => toCSVResponse(CapacityCSV(step, capacity, year, month, week, date))
    case step :: capacity :: AsInt(year) :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil Get req => toCSVResponse(CapacityCSV(step, capacity, year, month, week, date, machineID))
  })

  serve("api" / "csv" prefix {
    case "todayOrder" :: Nil Get req => toCSVResponse(TodayOrderCSV())
  })

  serve("api" / "csv" / "productionCard" prefix {
    case lotNo :: Nil Get req => toCSVResponse(ProductionCard(lotNo))
  })

  serve("api" / "csv" / "maintenanceLog" prefix {
    case date :: Nil Get req => toCSVResponse(MachineMaintainLogCSV(date))
  })

  serve("api" / "csv" / "orderStatus" prefix {
    case date :: Nil Get req => toCSVResponse(OrderStatusCSV(date))
  })


}
