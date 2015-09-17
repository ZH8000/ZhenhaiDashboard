package bootstrap.liftweb

import code.csv._
import code.lib._

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.PlainTextResponse
import net.liftweb.http.OutputStreamResponse
import net.liftweb.util.BasicTypesHelpers.AsInt
import java.io._

object ExcelRestAPI extends RestHelper {

  def gnerateMorningExcel(year: Int, month: Int)(outputStream: OutputStream) = {
    val excelGenerater = new DailyMorningExcel(year, month, outputStream)
    excelGenerater.outputExcel()
  }

  def gnerateMonthlyExcel(year: Int, month: Int, capacityRange: String)(outputStream: OutputStream) = {
    val excelGenerater = new MonthlySummaryExcel(year, month, capacityRange, outputStream)
    excelGenerater.outputExcel()
  }

  def gneratePerformanceExcel(year: Int, month: Int)(outputStream: OutputStream) = {
    val excelGenerater = new WorkerPerformanceExcel(year, month, outputStream)
    excelGenerater.outputExcel()
  }

  def gnerateKadouExcel(year: Int, month: Int)(outputStream: OutputStream) = {
    val excelGenerater = new KadouExcel(year, month, outputStream)
    excelGenerater.outputExcel()
  }

  def gnerateMachineDefactSummaryExcel(year: Int, month: Int, date: Int, shiftTag: String, sortTag: String)(outputStream: OutputStream) = {
    val excelGenerater = new MachineDefactSummaryExcel(year, month, date, shiftTag, sortTag, outputStream)
    excelGenerater.outputExcel()
  }


  serve("api" / "excel" / "workerPerformance" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      OutputStreamResponse(gneratePerformanceExcel(year, month)_, List("Content-Type" -> "application/vnd.ms-excel"))
  })

  serve("api" / "excel" / "monthly" prefix {
    case AsInt(year) :: AsInt(month) :: capacityRange :: Nil Get req => 
      OutputStreamResponse(gnerateMonthlyExcel(year, month, capacityRange)_, List("Content-Type" -> "application/vnd.ms-excel"))
  })

  serve("api" / "excel" / "morning" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      OutputStreamResponse(gnerateMorningExcel(year, month)_, List("Content-Type" -> "application/vnd.ms-excel"))
  })

  serve("api" / "excel" / "kadou" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      OutputStreamResponse(gnerateKadouExcel(year, month)_, List("Content-Type" -> "application/vnd.ms-excel"))
  })

  serve("api" / "excel" / "machineDefactSummary" prefix {
    case AsInt(year) :: AsInt(month) :: AsInt(date) :: shiftTag :: sortTag :: Nil Get req => 
      OutputStreamResponse(
        gnerateMachineDefactSummaryExcel(year, month, date, shiftTag, sortTag)_, 
        List("Content-Type" -> "application/vnd.ms-excel")
      )
  })

}

