package bootstrap.liftweb

import code.csv._
import net.liftweb.http.PlainTextResponse
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.Req
import net.liftweb.http.GetRequest

import net.liftweb.util.BasicTypesHelpers.AsInt

/**
 *  CSV REST API
 *
 *  這個物件用來定義 CSV 與相關的 REST API 網址，與其相對應的輸出。
 *
 *  用來實作當使用者點選網站上的「Export CSV」按鈕時，網站伺服器輸
 *  出 CSV 檔的部份。
 */
object CsvRestAPI extends RestHelper {

  /**
   *  將字串加上 Content-Type 的 HTTP Header，並以 HTTP Status Code 200 輸出
   *
   *  @param    csvString     要輸出的 CSV 內容
   *  @return                 CSV 檔的的 HTTP 輸出
   *
   */
  def toCSVResponse(csvString: String) = PlainTextResponse(csvString, List("Content-Type" -> "text/csv"), 200)

  // 定義 /api/csv/total/XXXXX 的網址，用在網頁上的「產量統計」->「依φ別」下的 Export CSV 按鈕。
  //
  // 每一個 case 敘述句後接的就是 XXXX 的網址部份，可以把 :: 看成 / 符號，會把該網址對應到相對
  // 應位置的變數名稱，若該變數名稱被 AsInt 包圍，則代表只有當該部份網址為整數時是合法的網址。
  //
  // Nil 代表網址的結束，其中的 Get 代表使用者必須發出 HTTP GET 的需求才會對應到這組規則，而使
  // 用者送出的詳細 HTTP Request 狀態物件則會被放在 req 變數中。
  //
  serve("api" / "csv" / "total" prefix {
    // 對應到 /api/csv/total.csv 此網址，且輸出為 TotalCSV.overview 此函式的輸出
    case Req(Nil, "csv", GetRequest) => toCSVResponse(TotalCSV.overview)    

    // 對應到 /api/csv/total/XXXX 此網址，且輸出為 TotalCSV(XXXX) 的輸出
    case Req(step :: Nil, "csv",  GetRequest) => toCSVResponse(TotalCSV(step))

    // 對應到 /api/csv/total/XXXX/YYYY 此網址，具輸出為 TotalCSV(XXXX, YYYY) 此函式呼叫後的輸出
    case Req(step :: productName :: Nil, "csv",  GetRequest) => toCSVResponse(TotalCSV(step, productName))

    // 對應到 /api/csv/total/XXXX/YYYY/AAAA/BBBB 此網址，具輸出為 TotalCSV(XXXX, YYYY, AAAA,BBBB) 此函式呼叫後的輸出，
    // 且 AAAA 和 BBBB 並需為整數，若不符合此條件則返回 404 NotFound 錯誤。
    case Req(step 
              :: productName 
              :: AsInt(year) 
              :: AsInt(month) 
              :: Nil, "csv", GetRequest)  => toCSVResponse(TotalCSV(step, productName, year, month))

    // 以下依此類推
    case Req(step :: productName 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: Nil, "csv", GetRequest) => toCSVResponse(TotalCSV(step, productName, year, month, week))
    case Req(step :: productName 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: AsInt(date) 
              :: Nil, "csv", GetRequest) => toCSVResponse(TotalCSV(step, productName, year, month, week, date))
    case Req(step :: productName 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: AsInt(date) 
              :: machineID 
              :: Nil, "csv", GetRequest) => toCSVResponse(TotalCSV(productName, year, month, week, date, machineID))
  })

  // 定義 /api/csv/capacity/XXXXX 的網址，用在網頁上的「產量統計」->「依容量」下的 Export CSV 按鈕。
  serve("api" / "csv" / "capacity" prefix {
    case Req(Nil, "csv", GetRequest) => toCSVResponse(CapacityCSV.overview)
    case Req(step :: Nil, "csv", GetRequest) => toCSVResponse(CapacityCSV(step))
    case Req(step :: capacity 
              :: Nil, "csv", GetRequest) => toCSVResponse(CapacityCSV(step, capacity))
    case Req(step :: capacity 
              :: AsInt(year) 
              :: AsInt(month) 
              :: Nil, "csv", GetRequest) => toCSVResponse(CapacityCSV(step, capacity, year, month))
    case Req(step :: capacity 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: Nil, "csv", GetRequest) => toCSVResponse(CapacityCSV(step, capacity, year, month, week))
    case Req(step :: capacity 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: AsInt(date) 
              :: Nil, "csv", GetRequest) => toCSVResponse(CapacityCSV(step, capacity, year, month, week, date))
    case Req(step :: capacity 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: AsInt(date) 
              :: machineID 
              :: Nil, "csv", GetRequest) => toCSVResponse(CapacityCSV(step, capacity, year, month, week, date, machineID))
  })

  // 定義 /api/csv/monthly/XXXXX 的網址，用在網頁上的「產量統計」->「月報表」下的 Export CSV 按鈕。
  serve("api" / "csv" / "monthly" prefix {
    case Req(AsInt(year) :: Nil, "csv", GetRequest) => toCSVResponse(MonthlyCSV(year))
    case Req(AsInt(year) :: step :: Nil, "csv", GetRequest) => toCSVResponse(MonthlyCSV(year, step))
    case Req(AsInt(year) :: step :: AsInt(month) :: Nil, "csv", GetRequest) => toCSVResponse(MonthlyCSV(year, step, month))
    case Req(AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: Nil, "csv", GetRequest) => toCSVResponse(MonthlyCSV(year, step, month, week))
    case Req(AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: AsInt(date) :: Nil, "csv", GetRequest) => toCSVResponse(MonthlyCSV(year, step, month, week, date))
    case Req(AsInt(year) :: step :: AsInt(month) :: AsInt(week) :: AsInt(date) :: machineID :: Nil, "csv", GetRequest) => toCSVResponse(MonthlyCSV(year, month, week, date, machineID))
  })

  // 定義 /api/csv/daily/XXXXX 的網址，用在網頁上的「產量統計」->「日報表」下的 Export CSV 按鈕。
  serve("api" / "csv" / "daily" prefix {
    case Req(AsInt(year) :: AsInt(month) :: Nil, "csv", GetRequest) => toCSVResponse(DailyCSV(year, month))
    case Req(AsInt(year) :: AsInt(month) 
                     :: step 
                     :: Nil, "csv", GetRequest) => toCSVResponse(DailyCSV(year, month, step))
    case Req(AsInt(year) :: AsInt(month) 
                     :: step 
                     :: AsInt(date) 
                     :: Nil, "csv", GetRequest) => toCSVResponse(DailyCSV(year, month, step, date))
    case Req(AsInt(year) :: AsInt(month) 
                     :: step 
                     :: AsInt(date) 
                     :: machineID 
                     :: Nil, "csv", GetRequest) => toCSVResponse(DailyCSV(year, month, step, date, machineID))
  })

  // 定義 /api/csv/workers/XXXXX 的網址，用在網頁上的「依人員」下的 Export CSV 按鈕。
  serve("api" / "csv" / "workers" prefix {
    case Req(Nil, "csv", GetRequest) => toCSVResponse(WorkerStatisticsCSV.overview)
    case Req(workerMongoID :: Nil, "csv", GetRequest) => toCSVResponse(WorkerStatisticsCSV(workerMongoID))
    case Req(workerMongoID :: yearAndMonth :: Nil, "csv", GetRequest) => toCSVResponse(WorkerStatisticsCSV(workerMongoID, yearAndMonth))
    case Req(workerMongoID :: yearAndMonth :: week :: Nil, "csv", GetRequest) => toCSVResponse(WorkerStatisticsCSV(workerMongoID, yearAndMonth, week))
    case Req(workerMongoID :: yearAndMonth :: week :: date :: Nil, "csv", GetRequest) => toCSVResponse(WorkerStatisticsCSV(workerMongoID, yearAndMonth, week, date))

  })

  // 定義 /api/csv/machine/XXXXX 的網址，用在網頁上的「錯誤分析」下的 Export CSV 按鈕。
  serve("api" / "csv" / "machine" prefix {
    case Req(Nil, "csv", GetRequest) => toCSVResponse(MachineCSV.overview)
    case Req(machineType :: Nil, "csv", GetRequest) => toCSVResponse(MachineCSV(machineType))
    case Req(machineType :: machineModel :: Nil, "csv", GetRequest) => toCSVResponse(MachineCSV(machineType, machineModel))
    case Req(machineType :: machineModel :: machineID :: Nil, "csv", GetRequest) => toCSVResponse(MachineCSV(machineType, machineModel, machineID))
  })

  // 定義 /api/csv/csv/todayOrder 的網址，用在網頁上的「今日工單」下的 Export CSV 按鈕。
  serve("api" / "csv" prefix {
    case Req("todayOrder" :: Nil, "csv", GetRequest) => toCSVResponse(TodayOrderCSV())
  })

  // 定義 /api/csv/csv/productionCard/XXXX 的網址，用在網頁上的「生產管理卡」下的 Export CSV 按鈕。
  serve("api" / "csv" / "productionCard" prefix {
    case Req(lotNo :: Nil, "csv", GetRequest) => toCSVResponse(ProductionCard(lotNo))
  })

  // 定義 /api/csv/csv/maintenanceLog/日期 的網址，用在網頁上的「維護記錄」下的 Export CSV 按鈕。
  serve("api" / "csv" / "maintenanceLog" prefix {
    case Req(date :: Nil, "csv", GetRequest) => toCSVResponse(MachineMaintainLogCSV(date))
  })

  // 定義 /api/csv/csv/orderStatus/日期 的網址，用在網頁上的「訂單狀態」下的 Export CSV 按鈕。
  serve("api" / "csv" / "orderStatus" prefix {
    case Req(date :: Nil, "csv", GetRequest) => toCSVResponse(OrderStatusCSV(date))
  })

  // 定義 /api/csv/csv/lossRate/ 的網址，用在網頁上的「訂單狀態」下的 Export CSV 按鈕。
  serve("api" / "csv" / "lossRate" prefix {
    case Req("result" :: startDate :: endDate :: machineType :: Nil, "csv", GetRequest) => toCSVResponse(LossRate(startDate, endDate, machineType))
    case Req("detail" :: startDate :: endDate :: machineType :: machineID :: Nil, "csv", GetRequest) => toCSVResponse(LossRate(startDate, endDate, machineType, machineID))
  })

}
