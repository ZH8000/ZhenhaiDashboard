package bootstrap.liftweb

import code.csv._
import code.lib._

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.PlainTextResponse
import net.liftweb.http.OutputStreamResponse
import net.liftweb.http.InMemoryResponse
import net.liftweb.http.NotFoundResponse
import net.liftweb.http.RedirectResponse
import net.liftweb.util.BasicTypesHelpers.AsInt
import java.io._
import java.nio.file.{Files, Paths}

/**
 *  Excel 的 REST API 設定
 *
 *  此 Singleton 物件用來設定網站上輸出 Excel 檔案的 REST API。
 *
 */
object ExcelRestAPI extends RestHelper {

  /**
   *  輸出「產量統計」－＞「晨間檢討」的 Excel 檔的內容到 outputStream 中
   *
   *  @param    year            該 Excel 檔的年
   *  @param    month           該 Excel 檔的月
   *  @param    outputStream    要輸出到哪個 OutputStream 中
   */
  def gnerateMorningExcel(year: Int, month: Int)(outputStream: OutputStream) = {
    new DailyMorningExcel(year, month, outputStream).outputExcel()
  }

  /**
   *  輸出「產量統計」－＞「重點統計」的 Excel 檔的內容到 outputStream 中
   *
   *  @param    year            該 Excel 檔的年
   *  @param    month           該 Excel 檔的月
   *  @param    capacityRange   哪個範圍的電容
   *  @param    outputStream    要輸出到哪個 OutputStream 中
   */
  def gnerateMonthlyExcel(year: Int, month: Int, capacityRange: String)(outputStream: OutputStream) = {
    new MonthlySummaryExcel(year, month, capacityRange, outputStream).outputExcel()
  }

  /**
   *  輸出「產量統計」－＞「稼動率」的 Excel 檔的內容到 outputStream 中
   *
   *  @param    year            該 Excel 檔的年
   *  @param    month           該 Excel 檔的月
   *  @param    outputStream    要輸出到哪個 OutputStream 中
   */
  def gnerateKadouExcel(year: Int, month: Int)(outputStream: OutputStream) = {
    new KadouExcel(year, month, outputStream).outputExcel()
  }

  /**
   *  輸出「產量統計」－＞「生產狀態」的 Excel 檔的內容到 outputStream 中
   *
   *  @param    year            該 Excel 檔的年
   *  @param    month           該 Excel 檔的月
   *  @param    date            該 Excel 檔的日期
   *  @param    shitTag         若為 ”M" 是早班，若為 "N" 是晚班
   *  @param    sortTag         資料的排序方法（"model" 為依機種排序，"size" 為依尺寸排序，"area" 為依區域排序）
   *  @param    outputStream    要輸出到哪個 OutputStream 中
   */
  def gnerateMachineDefactSummaryExcel(year: Int, month: Int, date: Int, 
                                       shiftTag: String, sortTag: String)(outputStream: OutputStream) = {
    new MachineDefactSummaryExcel(year, month, date, shiftTag, sortTag, outputStream).outputExcel()
  }

  /**
   *  輸出「產量統計」－＞「人員效率」的 Excel 檔的內容到 outputStream 中
   *
   *  因人員效率的 Excel 檔案需要較大的計算資源，因此不使用即時產生的方式，
   *  而是在 crontab 中固定每十五分鐘產生一次快取檔，當使用者點選連結時，則
   *  直接提供這個快取檔給使用者。
   *
   *  快取檔位於網伺服器上的 /opt/Jetty-9.2.5/workerPerformance/ 此資料夾中
   *
   *  @param    year            該 Excel 檔的年
   *  @param    month           該 Excel 檔的月
   *  @param    outputStream    要輸出到哪個 OutputStream 中
   */
  def genearteWorkerPerformance(year: Int, month: Int) = {
    try {
      val byteArray = Files.readAllBytes(Paths.get(f"/opt/Jetty-9.2.5/workerPerformance/$year-$month%02d.xls"))
      InMemoryResponse(byteArray, List("Content-Type" -> "application/vnd.ms-excel"), Nil, 200)
    } catch {
      case e: Exception => RedirectResponse("/notFound")
    }
  }


  // 定義 /api/csv/excel/workerPerformance/XXXX/YY 的網址，用在網頁上的「產量統計」->「人員效率」
  // 下的「開啟完整 Excel 報表」按鈕。
  //
  // 每一個 case 敘述句後接的就是 XXXX 的網址部份，可以把 :: 看成 / 符號，會把該網址對應到相對
  // 應位置的變數名稱，若該變數名稱被 AsInt 包圍，則代表只有當該部份網址為整數時是合法的網址。
  //
  // Nil 代表網址的結束，其中的 Get 代表使用者必須發出 HTTP GET 的需求才會對應到這組規則，而使
  // 用者送出的詳細 HTTP Request 狀態物件則會被放在 req 變數中。
  //
  serve("api" / "excel" / "workerPerformance" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => genearteWorkerPerformance(year, month)
  })

  // 定義 /api/csv/excel/monthly/XXXX/YY/ZZ 的網址，用在網頁上的「產量統計」-> 「重點統計」
  // 下的「開啟完整 Excel 報表」按鈕。
  //
  serve("api" / "excel" / "monthly" prefix {
    case AsInt(year) :: AsInt(month) :: capacityRange :: Nil Get req => 
      OutputStreamResponse(
        gnerateMonthlyExcel(year, month, capacityRange)_,     // 要填什麼資料到 OutputStream 中
        List("Content-Type" -> "application/vnd.ms-excel")    // HTTP Header
      )
  })

  // 定義 /api/csv/excel/morning/XXXX/YY 的網址，用在網頁上的「產量統計」-> 「晨間檢討」
  // 下的「開啟完整 Excel 報表」按鈕。
  //
  serve("api" / "excel" / "morning" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      OutputStreamResponse(
        gnerateMorningExcel(year, month)_, 
        List("Content-Type" -> "application/vnd.ms-excel")
      )
  })

  // 定義 /api/csv/excel/kadou/XXXX/YY 的網址，用在網頁上的「產量統計」-> 「稼動率」
  // 下的「開啟完整 Excel 報表」按鈕。
  //
  serve("api" / "excel" / "kadou" prefix {
    case AsInt(year) :: AsInt(month) :: Nil Get req => 
      OutputStreamResponse(gnerateKadouExcel(year, month)_, List("Content-Type" -> "application/vnd.ms-excel"))
  })

  // 定義 /api/csv/excel/machineDefactSummary/XXXX/YY 的網址，用在網頁上的「產量統計」-> 「生產狀況」
  // 下的「開啟 Excel 報表」按鈕。
  //
  serve("api" / "excel" / "machineDefactSummary" prefix {
    case AsInt(year) :: AsInt(month) :: AsInt(date) :: shiftTag :: sortTag :: Nil Get req => 
      OutputStreamResponse(
        gnerateMachineDefactSummaryExcel(year, month, date, shiftTag, sortTag)_, 
        List("Content-Type" -> "application/vnd.ms-excel")
      )
  })
}

