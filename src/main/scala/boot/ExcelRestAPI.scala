package bootstrap.liftweb

import code.csv._
import code.lib._

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.PlainTextResponse
import net.liftweb.http.OutputStreamResponse
import net.liftweb.util.BasicTypesHelpers.AsInt
import java.io._

object ExcelRestAPI extends RestHelper {

  def gnerateMonthlyExcel(year: Int, month: Int, capacityRange: String)(outputStream: OutputStream) = {
    val excelGenerater = new MonthlySummaryExcel(year, month, capacityRange, outputStream)
    excelGenerater.outputExcel()
  }

  serve("api" / "excel" / "monthly" prefix {
    case AsInt(year) :: AsInt(month) :: capacityRange :: Nil Get req => 
      OutputStreamResponse(gnerateMonthlyExcel(year, month, capacityRange)_, List("Content-Type" -> "application/vnd.ms-excel"))
  })

}

