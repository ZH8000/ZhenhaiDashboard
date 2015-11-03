package bootstrap.liftweb

import code.lib._
import code.model._
import com.mongodb.MongoClient
import javax.mail.{Authenticator,PasswordAuthentication}
import net.liftweb.common.{Full, Empty}
import net.liftweb.http._
import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.http.S
import net.liftweb.http.Templates
import net.liftweb.mongodb.MongoDB
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.sitemap.Loc.If
import net.liftweb.sitemap.Loc.Template
import net.liftweb.sitemap.Loc.Unless
import net.liftweb.util.BasicTypesHelpers._
import net.liftweb.util.DefaultConnectionIdentifier
import net.liftweb.util.Props.RunModes
import net.liftweb.util.{Props, Mailer}
import scala.xml.NodeSeq

class Boot 
{
  private def getTemplate(path: String) = Template(() => Templates(path.split("/").toList) openOr NodeSeq.Empty)
  private def needLogin = If(() => User.isLoggedIn, () => S.redirectTo("/", () => S.error("請先登入")))
  private def hasPermission(permission: PermissionContent.Value) = If(
    () => { User.CurrentUser.get.map(_.hasPermission(permission)).openOr(false) }, 
    () => S.redirectTo("/", () => S.error("權限不足"))
  )

  private def redirectToDashboardIfLoggedIn = If(() => !User.isLoggedIn, () => S.redirectTo("/dashboard"))
  private def logout = EarlyResponse{ () =>
    User.isLoggedIn match {
      case false => Full(NotFoundResponse("NotFound"))
      case true => 
        User.CurrentUser(Empty)
        S.redirectTo("/", () => S.notice("已登出"))
    }
  }

  val editWorkerMenu = Menu.param[Worker]("EditWorker", "EditWorker", id => Worker.find(id), worker => worker.id.get.toString)
  val editUserMenu = Menu.param[User]("EditUser", "EditUser", id => User.find(id), user => user.id.get.toString)
 
  val editAlarmMenu = Menu.param[Alarm]("EditAlarm", "EditAlarm", id => Alarm.find(id), alarm => alarm.id.get.toString)
  def workerMenu(menuID: String) = Menu.param[Worker](menuID, menuID, id => Worker.find(id), worker => worker.id.get.toString)


  val siteMap = SiteMap(
    Menu("assets") / "test" / **,
    Menu("Home") / "index" >> redirectToDashboardIfLoggedIn,
    Menu("Logout") / "user" / "logout" >> logout,
    Menu("ChangePassword") / "user" / "changePassword" 
      >> needLogin
      >> hasPermission(PermissionContent.ManagementAccount),
    Menu("Dashboard") / "dashboard" >> needLogin,
    Menu("ViewDetail") / "viewDetail" >> needLogin,

    Menu("ViewDetail") / "excel" / "monthly" / "index" >> needLogin,
    Menu("ExcelMonthlyRange") / "excel" / "monthly" / * / * >> getTemplate("excel/monthly/capacityRange") >> needLogin,
    Menu("ExcelMonthlyDetail") / "excel" / "monthly" / * / * / * >> getTemplate("excel/monthly/detail") >> needLogin,
    Menu("DailyMorning") / "excel" / "morning" / * / * >> getTemplate("excel/morning/detail") >> needLogin,
    Menu("WorkerPerformance") / "excel" / "workerPerformance" / * / * >> getTemplate("excel/workerPerformance/detail") >> needLogin,
    Menu("Kadou") / "excel" / "kadou" / * / * >> getTemplate("excel/kadou/detail") >> needLogin,

    Menu("RawDataMachineList") / "rawData" / * >> getTemplate("rawData/machineList") >> needLogin,
    Menu("RawDataDetaiList") / "rawData" / * / * >> getTemplate("rawData/detailList") >> needLogin,

    Menu("DefactSummaryIndex") / "machineDefactSummary" / * / * / * >> getTemplate("machineDefactSummary/index") >> needLogin,
    Menu("DefactSummarySort") / "machineDefactSummary" / * / * / * / * >> getTemplate("machineDefactSummary/sort") >> needLogin,
    Menu("DefactSummaryDetail") / "machineDefactSummary" / * / * / * / * / * >> getTemplate("machineDefactSummary/detail") >> needLogin,

    Menu("Alert") / "alert" / "index" >> needLogin,
    Menu("Alert") / "alert" / "strangeQty" >> needLogin,
    Menu("Alert") / "alert" / "alertDate" >> needLogin,
    Menu("Alert") / "alert" / "alert" / * >> getTemplate("alert/alert") >> needLogin,
    Menu("Alive") / "alive",
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
      >> hasPermission(PermissionContent.ReportOrderStatus)

  )

  val ensureLogin: PartialFunction[Req, Unit] = {
    case req if User.isLoggedIn =>
  }

  def errorPageResponse(req: Req, code: Int) = {
    val content = S.render(<lift:embed what={code.toString} />, req.request)
    XmlResponse(content.head, code, "text/html", req.cookies)
  }

  def boot 
  {
    MongoDB.defineDb(DefaultConnectionIdentifier, new MongoClient, "zhenhai")

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.addToPackages("code")
    LiftRules.setSiteMap(siteMap)
    LiftRules.dispatch.append(ensureLogin guard JsonRestAPI)
    LiftRules.dispatch.append(ensureLogin guard CsvRestAPI)
    LiftRules.dispatch.append(ensureLogin guard ExcelRestAPI)
    LiftRules.dispatch.append(MachineStatusRestAPI)

    LiftRules.uriNotFound.prepend({
      case (req,failure) => NotFoundAsResponse(errorPageResponse(req, 404))
    })

    LiftRules.loggedInTest = Full(() => !User.CurrentUser.get.isEmpty)
    LiftRules.exceptionHandler.prepend {
      case (runMode, req, exception) if runMode == RunModes.Production =>
        println(runMode)
        println(s"========== ${req.uri} =============")
        println(s"DateTime: ${new java.util.Date}")
        exception.printStackTrace()
        println(s"===================================")
        errorPageResponse(req, 500)
    }

    Mailer.authenticator = for {
      user <- Props.get("mail.user")
      pass <- Props.get("mail.password")
    } yield new Authenticator {
      override def getPasswordAuthentication =
        new PasswordAuthentication(user,pass)
    }
  }
}
