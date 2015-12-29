package bootstrap.liftweb

import code.json._
import net.liftweb.http.JsonResponse
import net.liftweb.http.rest.RestHelper
import net.liftweb.util.BasicTypesHelpers.AsInt

/**
 *  JSON 輸出的 REST API 設定
 *
 *  此 Singleton 物件用來設定網站上輸出 JSON 格式的 REST API，主要用
 *  在「產量統計」中，讓瀏覽器透過 JSON / D3D 畫圖表的部份。
 *
 */
object JsonRestAPI extends RestHelper {

  // 定義 /api/csv/json/total/XXXX 的網址，用在網頁上的「產量統計」->「依φ別」中用來讓瀏覽器透
  // 過 D3D JavaScript 函式庫畫出圖表。
  //
  // 每一個 case 敘述句後接的就是 XXXX 的網址部份，可以把 :: 看成 / 符號，會把該網址對應到相對
  // 應位置的變數名稱，若該變數名稱被 AsInt 包圍，則代表只有當該部份網址為整數時是合法的網址。
  //
  // Nil 代表網址的結束，其中的 Get 代表使用者必須發出 HTTP GET 的需求才會對應到這組規則，而使
  // 用者送出的詳細 HTTP Request 狀態物件則會被放在 req 變數中。
  //
  serve("api" / "json" / "total" prefix {
    case Nil Get req => JsonResponse(TotalJSON.overview)
    case step :: Nil Get req => JsonResponse(TotalJSON(step))
    case step :: product 
              :: Nil Get req => JsonResponse(TotalJSON(step, product))
    case step :: productName 
              :: AsInt(year) 
              :: AsInt(month) 
              :: Nil Get req => JsonResponse(TotalJSON(step, productName, year, month))
    case step :: productName 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: Nil Get req => JsonResponse(TotalJSON(step, productName, year, month, week))
    case step :: productName 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: AsInt(date) 
              :: Nil Get req => JsonResponse(TotalJSON(step, productName, year, month, week, date))
    case step :: productName 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: AsInt(date) 
              :: machineID 
              :: Nil Get req => JsonResponse(TotalJSON(productName, year, month, week, date, machineID))
  })

  // 定義 /api/csv/json/capacity/XXXX 的網址，用在網頁上的「產量統計」->「依容量」中用來讓瀏覽器透
  // 過 D3D JavaScript 函式庫畫出圖表。
  //
  serve("api" / "json" / "capacity" prefix {
    case Nil Get req => JsonResponse(CapacityJSON.overview)
    case step :: Nil Get req => JsonResponse(CapacityJSON(step))
    case step :: capacity 
              :: Nil Get req => JsonResponse(CapacityJSON(step, capacity))
    case step :: capacity 
              :: AsInt(year) 
              :: AsInt(month) 
              :: Nil Get req => JsonResponse(CapacityJSON(step, capacity, year, month))
    case step :: capacity 
              :: AsInt(year) 
              :: AsInt(month)
              :: AsInt(week)
              :: Nil Get req => JsonResponse(CapacityJSON(step, capacity, year, month, week))
    case step :: capacity 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: AsInt(date) 
              :: Nil Get req => JsonResponse(CapacityJSON(step, capacity, year, month, week, date))
    case step :: capacity 
              :: AsInt(year) 
              :: AsInt(month) 
              :: AsInt(week) 
              :: AsInt(date) 
              :: machineID 
              :: Nil Get req => JsonResponse(CapacityJSON(step, capacity, year, month, week, date, machineID))
  })

  // 定義 /api/csv/json/monthly/XXXX 的網址，用在網頁上的「產量統計」->「月報表」中用來讓瀏覽器透
  // 過 D3D JavaScript 函式庫畫出圖表。
  //
  serve("api" / "json" / "monthly" prefix {
    case AsInt(year) :: Nil Get req => JsonResponse(MonthlyJSON(year))
    case AsInt(year) :: step 
                     :: Nil Get req => JsonResponse(MonthlyJSON(year, step))
    case AsInt(year) :: step 
                     :: AsInt(month) 
                     :: Nil Get req => JsonResponse(MonthlyJSON(year, step, month))
    case AsInt(year) :: step 
                     :: AsInt(month) 
                     :: AsInt(week) 
                     :: Nil Get req => JsonResponse(MonthlyJSON(year, step, month, week))
    case AsInt(year) :: step 
                     :: AsInt(month) 
                     :: AsInt(week)
                     :: AsInt(date) 
                     :: Nil Get req => JsonResponse(MonthlyJSON(year, step, month, week, date))
    case AsInt(year) :: step 
                     :: AsInt(month) 
                     :: AsInt(week) 
                     :: AsInt(date) 
                     :: machineID 
                     :: Nil Get req => JsonResponse(MonthlyJSON(year, month, week, date, machineID))
  })

  // 定義 /api/csv/json/daily/XXXX 的網址，用在網頁上的「產量統計」->「日報表」中用來讓瀏覽器透
  // 過 D3D JavaScript 函式庫畫出圖表。
  //
  serve("api" / "json" / "daily" prefix {
    case AsInt(year) :: AsInt(month) 
                     :: Nil Get req => JsonResponse(DailyJSON(year, month))
    case AsInt(year) :: AsInt(month) 
                     :: step 
                     :: Nil Get req => JsonResponse(DailyJSON(year, month, step))
    case AsInt(year) :: AsInt(month) 
                     :: step 
                     :: AsInt(date) 
                     :: Nil Get req => JsonResponse(DailyJSON(year, month, step, date))
    case AsInt(year) :: AsInt(month) 
                     :: step 
                     :: AsInt(date) 
                     :: machineID 
                     :: Nil Get req => JsonResponse(DailyJSON(year, month, step, date, machineID))
  })

  // 定義 /api/csv/json/machine/XXXX 的網址，用在網頁上的「錯誤分析」中用來讓瀏覽器透
  // 過 D3D JavaScript 函式庫畫出圖表。
  //
  serve("api" / "json" / "machine" prefix {
    case Nil Get req => JsonResponse(MachineJSON.overview)
    case machineType :: Nil Get req => JsonResponse(MachineJSON(machineType))
    case machineType :: machineModel :: Nil Get req => JsonResponse(MachineJSON(machineType, machineModel))
    case machineType :: machineModel :: machineID :: "pie" :: Nil Get req => JsonResponse(MachineJSON.detailPie(machineID))
    case machineType :: machineModel :: machineID :: "table" :: Nil Get req => JsonResponse(MachineJSON.detailTable(machineID))
  })
}

