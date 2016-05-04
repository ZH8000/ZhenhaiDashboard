package bootstrap.liftweb

import code.lib._
import code.model._
import net.liftweb.common.{Empty, Full}
import net.liftweb.http.{S, Templates, _}
import net.liftweb.sitemap.Loc.{EarlyResponse, If, Template}
import net.liftweb.sitemap._

import scala.xml.NodeSeq

/**
 *  此 Singleton 物件用來設定整個網站中，哪個網址對應到哪個 HTML 模板，以及
 *  是否需要使用者登入、登入的使用者需要具有哪些權限等。
 *
 *  網址的設定位於此 Singleton 的 siteMap 變數中，會在 Boot.scala 中被引用
 *  做為網站的網址設定。
 */
object SiteMapDefine {

  /**
   *  取得 HTML 模板，若無法取得則傳回空的 HTML 頁面
   *  
   *  此函式用來提供某個網址特定的 HTML 模板，若無法找到該模板，則
   *  會使用空白頁面。
   *
   *  模板全部份於 webapp/ 資料夾下，path 為相對應於此資料夾的模板
   *  檔案路徑。
   *
   *  @param    path    HTML 模板路徑
   *  @return           HTML 模板
   *
   */
  private def getTemplate(path: String) = Template(() => Templates(path.split("/").toList) openOr NodeSeq.Empty)

  /**
   *  檢查使用者是否有登入，若尚未登入則導至登入畫面
   *
   *  此函式用來設定某個網址是否需要先登入才能存取，若 SiteMap 中
   *  指定了某個網址使用此函式檢查，則使用者若未登入，會被導至登
   *  入頁面。
   *
   *  @return     具有上述行為的 SiteMap 規則
   */
  private def needLogin = If(
    () => User.isLoggedIn,                              // 一定要符合的條件
    () => S.redirectTo("/", () => S.error("請先登入"))  // 若未符合上述條件時的動作
  )

  /**
   *  檢查使用者的權限是否足夠
   *
   *  此函式用來讓 SiteMap 指定某個網址是否需要檢查目前使用者是否具有特定的權限，
   *  若指定使用此函式檢查，則當使用者不具有指定的權限時，會被導至首頁，並且出現
   *  「權限不足」的警告字樣。
   *
   *  @param    permission        要檢查的權限
   *  @return                     具有上述行為的 SiteMap 規則
   */
  private def hasPermission(permission: PermissionContent.Value) = If(
    () => { User.CurrentUser.get.map(_.hasPermission(permission)).openOr(false) }, 
    () => S.redirectTo("/", () => S.error("權限不足"))
  )

  /**
   *  若使用者已登入則
   *
   *  用在登入頁面的條件檢查，因為只有當使用者是在未登入的狀態時，才能進入
   *  登入頁面。所以當這個函式檢查到使用者倘若不是在未登入的狀態（已登入），
   *  就會將使用者導到 dashboard 主頁。
   *
   *  @return     上述行為的 SiteMap 規則
   */
  private def redirectToDashboardIfLoggedIn = If(
    () => !User.isLoggedIn,             // 使用者一定要在未登入狀態才能看到登入畫面
    () => S.redirectTo("/dashboard")    // 不然的話就導到 dashboard 主頁
  )

  /**
   *  登出選單的 URL 的 SiteMap 行為
   *
   *  若使用者本來就尚未登入，則登出的 URL 應該為 404 NotFound 狀態，
   *  若使用者是在已登入的狀態，則清除目前使用者的 Session 變數，並
   *  導到登入畫面。
   *
   *  @return     上述行為的 SiteMap 規則
   */
  private def logout = EarlyResponse{ () =>
    User.isLoggedIn match {
      case false => Full(NotFoundResponse("NotFound"))
      case true => 
        User.CurrentUser(Empty)
        S.redirectTo("/", () => S.notice("已登出"))
    }
  }

  /**
   *  編輯員工的網頁的 URL 的 SiteMap 的 Menu 設定
   */
  val editWorkerMenu = Menu.param[Worker](
    "EditWorker",                           // 此 Menu 的名稱
    "EditWorker",                           // 此 Menu 的標題
    id => Worker.find(id),                  // 如何從網址上的字串轉換到 Worker 物件
    worker => worker.id.get.toString        // 如何從 Worker 物件轉換到網址上的字串
  )

  /**
   *  編輯網站使用者的網頁的 URL 的 SiteMap 的 Menu 設定
   */
  val editUserMenu = Menu.param[User]("EditUser", "EditUser", id => User.find(id), user => user.id.get.toString)
 
  /**
   *  編輯「維修行事曆」的網頁的 URL 的 SiteMap 的 Menu 設定
   */
  val editAlarmMenu = Menu.param[Alarm]("EditAlarm", "EditAlarm", id => Alarm.find(id), alarm => alarm.id.get.toString)

  /**
   *  此處為設定整個網站內所有的網址對應到的 HTML 模板，或其行為
   *
   *  語法為 Menu("標題") / 第一層網址 / 第二層網址 ＞＞ 規則設定一 ＞＞ 規則設定二，
   *  依此類推。
   *
   *  若未指定 getTemplate 規則，則會依照相對應的路徑去尋找 webapp/ 下的檔案，
   *  例如  Menu("ChangePassword") / "user" / "changePassword" 的 HTML 模板就
   *  會是 webapp/user/changePassword.html
   *
   *  若網址的部份有 * 號，則代表可以匹配任何字串，此時需使用 getTemplate 指定
   *  要使用的 HTML 模板（同樣位於 webapp 資料夾中）。
   */
  val siteMap = SiteMap(
    Menu("Home") / "index" 
      >> redirectToDashboardIfLoggedIn,

    Menu("Logout") / "user" / "logout" 
      >> logout,

    Menu("ChangePassword") / "user" / "changePassword" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAccount),

    Menu("Dashboard") / "dashboard" 
      >> needLogin,

    Menu("ViewDetail") / "viewDetail" 
      >> needLogin,

    Menu("ViewDetail") / "excel" / "monthly" / "index" 
      >> needLogin,

    Menu("ExcelMonthlyRange") / "excel" / "monthly" / * / * 
      >> getTemplate("excel/monthly/capacityRange") 
      >> needLogin,

    Menu("ExcelMonthlyDetail") / "excel" / "monthly" / * / * / * 
      >> getTemplate("excel/monthly/detail") 
      >> needLogin,

    Menu("DailyMorning") / "excel" / "morning" / * / * 
      >> getTemplate("excel/morning/detail") 
      >> needLogin,

    Menu("WorkerPerformance") / "excel" / "workerPerformance" / * / * 
      >> getTemplate("excel/workerPerformance/detail") 
      >> needLogin,

    Menu("Kadou") / "excel" / "kadou" / * / * 
      >> getTemplate("excel/kadou/detail") 
      >> needLogin,

    Menu("RawDataMachineList") / "rawData" / * 
      >> getTemplate("rawData/machineList") 
      >> needLogin,
    Menu("RawDataDetaiList") / "rawData" / * / * 
      >> getTemplate("rawData/detailList") 
      >> needLogin,

    Menu("DefactSummaryIndex") / "machineDefactSummary" / * / * / * 
      >> getTemplate("machineDefactSummary/index") 
      >> needLogin,
    Menu("DefactSummarySort") / "machineDefactSummary" / * / * / * / * 
      >> getTemplate("machineDefactSummary/sort") 
      >> needLogin,
    Menu("DefactSummaryDetail") / "machineDefactSummary" / * / * / * / * / * 
      >> getTemplate("machineDefactSummary/detail") 
      >> needLogin,

    Menu("AlertIndex") / "alert" / "index" >> needLogin,
    Menu("AlertStrangeQty") / "alert" / "strangeQty" >> needLogin,
    Menu("AlertDate") / "alert" / "alertDate" >> needLogin,
    Menu("AlertAlert") / "alert" / "alert" / * >> getTemplate("alert/alert") >> needLogin,
    Menu("Capacity1") / "capacity" 
      >> getTemplate("capacity/overview") 
      >> needLogin 
      >> hasPermission(PermissionContent.ReportCapacity),
    Menu("Capacity2") / "capacity" / * 
      >> getTemplate("capacity/overview") 
      >> needLogin 
      >> hasPermission(PermissionContent.ReportCapacity),
    Menu("Capacity3") / "capacity" / * / * 
      >> getTemplate("capacity/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportCapacity),
    Menu("Capacity4") / "capacity" / * / * / * / * 
      >> getTemplate("capacity/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportCapacity),
    Menu("Capacity5") / "capacity" / * / * / * / * / * 
      >> getTemplate("capacity/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportCapacity),
    Menu("Capacity6") / "capacity" / * / * / * / * / * / * 
      >> getTemplate("capacity/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportCapacity),
    Menu("Capacity7") / "capacity" / * / * / * / * / * / * / * 
      >> getTemplate("capacity/machine") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportCapacity),
 
    Menu("Total1") / "total" 
      >> getTemplate("total/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportPhi),
    Menu("Total2") / "total" / * 
      >> getTemplate("total/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportPhi),
    Menu("Total3") / "total" / * / * 
      >> getTemplate("total/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportPhi),
    Menu("Total3") / "total" / * / * / * 
      >> getTemplate("total/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportPhi),
    Menu("Total4") / "total" / * / * / * / * 
      >> getTemplate("total/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportPhi),
    Menu("Total5") / "total" / * / * / * / * / * 
      >> getTemplate("total/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportPhi),
    Menu("Total6") / "total" / * / * / * / * / * / * 
      >> getTemplate("total/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportPhi),
    Menu("Total7") / "total" / * / * / * / * / * / * / * 
      >> getTemplate("total/machine") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportPhi),
 
    Menu("Monthly1") / "monthly" / *
      >> getTemplate("monthly/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportMonthly),
    Menu("Monthly2") / "monthly" / * / * 
      >> getTemplate("monthly/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportMonthly),
    Menu("Monthly3") / "monthly" / * / * / * 
      >> getTemplate("monthly/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportMonthly),
    Menu("Monthly4") / "monthly" / * / * / * / * 
      >> getTemplate("monthly/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportMonthly),
    Menu("Monthly5") / "monthly" / * / * / * / * / * 
      >> getTemplate("monthly/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportMonthly),
    Menu("Monthly6") / "monthly" / * / * / * / * / * / * 
      >> getTemplate("monthly/machine") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportMonthly),

    Menu("Daily1") / "daily" / * / * 
      >> getTemplate("daily/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportDaily),
    Menu("Daily2") / "daily" / * / * / * 
      >> getTemplate("daily/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportDaily),
    Menu("Daily3") / "daily" / * / * / * / * 
      >> getTemplate("daily/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportDaily),
    Menu("Daily4") / "daily" / * / * / * / * / * 
      >> getTemplate("daily/machine") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportDaily),
 
    Menu("Machine1") / "machine" 
      >> getTemplate("machine/index") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportBug),
    Menu("Machine2") / "machine" / * 
      >> getTemplate("machine/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportBug),
    Menu("Machine3") / "machine" / * / * 
      >> getTemplate("machine/overview") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportBug),
    Menu("Machine4") / "machine" / * / * / * 
      >> getTemplate("machine/detail") 
      >> needLogin
      >> hasPermission(PermissionContent.ReportBug),
 
    Menu("Managemen1") / "management" / "index" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),
    Menu("Managemen2") / "management" / "workers" / "add" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),
    Menu("Managemen3") / "management" / "workers" / "index" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),
    Menu("Managemen4") / "management" / "workers" / "barcode" 
      >> needLogin
      >> Worker.barcodePDF
      >> hasPermission(PermissionContent.ManagementWorker),
    editWorkerMenu / "management" / "workers" / "edit" / * 
      >> getTemplate("management/workers/edit") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),

    Menu("Managemen6") / "management" / "alarms" / "index" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAlarm),
    Menu("Managemen6") / "management" / "alarms" / * 
      >> getTemplate("management/alarms/list") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAlarm),
    Menu("Managemen5") / "management" / "alarms" / * / "add" 
      >> getTemplate("management/alarms/add") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAlarm),

    editAlarmMenu / "management" / "alarms" / "edit" / * 
      >> getTemplate("management/alarms/edit") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAlarm),
 
    Menu("Managemen7") / "management" / "account" / "index" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAccount),
    Menu("Managemen8") / "management" / "account" / "add" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAccount),
    Menu("Managemen8") / "management" / "account" / "addPermission" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAccount),
    editUserMenu / "management" / "account" / "edit" / * 
      >> getTemplate("management/account/edit") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAccount),
    Menu("Managemen9") / "management" / "productCost" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAccount),

    Menu("Workers") / "workers" / "index" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),
    Menu("Workers1") / "workers" / * 
      >> getTemplate("workers/worker") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),
    Menu("Workers2") / "workers" / * / * 
      >> getTemplate("workers/weekly") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),
    Menu("Workers3") / "workers" / * / * / * 
      >> getTemplate("workers/daily") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),
    Menu("Workers4") / "workers" / * / * / * / * 
      >> getTemplate("workers/detail") 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementWorker),
 
    Menu("machineLevel") / "management" / "machineLevel" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementMachineLevel),
    Menu("TodayOrder") / "todayOrder" 
      >> needLogin
      >> hasPermission(PermissionContent.ReportTodayOrder),

    Menu("ProductionStatusHistory") / "productionStatusHistory" / *
      >> needLogin
      >> hasPermission(PermissionContent.ReportTodayOrder)
      >> getTemplate("productionStatusHistory") >> needLogin,


    Menu("OrderStatus") / "orderStatus" / "index"
      >> needLogin
      >> hasPermission(PermissionContent.ReportOrderStatus),
    Menu("OrderStatusDetail") / "orderStatus" / *
      >> getTemplate("orderStatus/dateCard")
      >> needLogin
      >> hasPermission(PermissionContent.ReportOrderStatus),
    Menu("OrderStatusDetail") / "orderStatus" / * / *
      >> getTemplate("orderStatus/detail")
      >> needLogin
      >> hasPermission(PermissionContent.ReportOrderStatus),

    Menu("MachineMaintenance") / "maintenanceLog" / "index"
      >> needLogin
      >> hasPermission(PermissionContent.ReportMaintainLog),
    Menu("MachineMaintenanceDetail") / "maintenanceLog" / *
      >> getTemplate("maintenanceLog/detail")
      >> needLogin
      >> hasPermission(PermissionContent.ReportMaintainLog),

    Menu("Managemen4") / "management" / "maintenanceCodePDF" 
      >> needLogin
      >> MaintenanceCode.barcodePDF
      >> hasPermission(PermissionContent.ReportMaintainLog),

    Menu("ProductionCard") / "productionCard"  / "index"
      >> needLogin
      >> hasPermission(PermissionContent.ReportOrderStatus),
    Menu("ProductionCard2") / "productionCard"  / *
      >> getTemplate("productionCard/detail")
      >> needLogin
      >> hasPermission(PermissionContent.ReportOrderStatus),

    Menu("LossRateQueryIndex") / "lossRate"  / "index"
      >> needLogin
      >> hasPermission(PermissionContent.ReportOrderStatus),
    Menu("LossRateResult") / "lossRate"  / "result"
      >> needLogin
      >> hasPermission(PermissionContent.ReportOrderStatus),
    Menu("LossRateResultDetial") / "lossRate"  / "detail"
      >> needLogin
      >> hasPermission(PermissionContent.ReportOrderStatus),


    Menu("Announcement") / "management" / "announcement" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAnnouncement)
  )

}
