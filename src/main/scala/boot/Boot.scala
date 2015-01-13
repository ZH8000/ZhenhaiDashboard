package bootstrap.liftweb

import code.model._

import com.mongodb.MongoClient

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
import net.liftweb.util.BasicTypesHelpers._
import net.liftweb.util.DefaultConnectionIdentifier
import net.liftweb.util.Props.RunModes

import scala.xml.NodeSeq

class Boot 
{
  private def getTemplate(path: String) = Template(() => Templates(path.split("/").toList) openOr NodeSeq.Empty)
  private def needLogin = If(() => User.isLoggedIn, () => S.redirectTo("/", () => S.error("請先登入")))
  //private def needLogin = If(() => true, () => S.redirectTo("/", () => S.error("請先登入")))

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
  val editAlarmMenu = Menu.param[Alarm]("EditAlarm", "EditAlarm", id => Alarm.find(id), alarm => alarm.id.get.toString)
  def workerMenu(menuID: String) = Menu.param[Worker](menuID, menuID, id => Worker.find(id), worker => worker.id.get.toString)


  val siteMap = SiteMap(
    Menu("assets") / "test" / **,
    Menu("Home") / "index" >> redirectToDashboardIfLoggedIn,
    Menu("Logout") / "user" / "logout" >> logout,
    Menu("Dashboard") / "dashboard" >> needLogin,
    Menu("ViewDetail") / "viewDetail" >> needLogin,
    Menu("Alert") / "alert" / "index" >> needLogin,
    Menu("Alert") / "alert" / "strangeQty" >> needLogin,
    Menu("Alert") / "alert" / "alertDate" >> needLogin,
    Menu("Alert") / "alert" / "alert" / * >> getTemplate("alert/alert") >> needLogin,
    Menu("Alive") / "alive",
    Menu("Capacity1") / "capacity" >> getTemplate("capacity/overview") >> needLogin,
    Menu("Capacity2") / "capacity" / * >> getTemplate("capacity/overview") >> needLogin,
    Menu("Capacity3") / "capacity" / * / * >> getTemplate("capacity/overview") >> needLogin,
    Menu("Capacity4") / "capacity" / * / * / * / * >> getTemplate("capacity/overview") >> needLogin,
    Menu("Capacity5") / "capacity" / * / * / * / * / * >> getTemplate("capacity/overview") >> needLogin,
    Menu("Capacity6") / "capacity" / * / * / * / * / * / * >> getTemplate("capacity/overview") >> needLogin,
    Menu("Capacity7") / "capacity" / * / * / * / * / * / * / * >> getTemplate("capacity/machine") >> needLogin,
    Menu("Total1") / "total" >> getTemplate("total/overview") >> needLogin,
    Menu("Total2") / "total" / * >> getTemplate("total/phi") >> needLogin,
    Menu("Total3") / "total" / * / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total3") / "total" / * / * / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total4") / "total" / * / * / * / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total5") / "total" / * / * / * / * / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total6") / "total" / * / * / * / * / * / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total7") / "total" / * / * / * / * / * / * / * >> getTemplate("total/machine") >> needLogin,
    Menu("Monthly1") / "monthly" / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly2") / "monthly" / * / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly3") / "monthly" / * / * / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly4") / "monthly" / * / * / * / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly5") / "monthly" / * / * / * / * / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly6") / "monthly" / * / * / * / * / * / * >> getTemplate("monthly/machine") >> needLogin,
    Menu("Daily1") / "daily" / * / * >> getTemplate("daily/overview") >> needLogin,
    Menu("Daily2") / "daily" / * / * / * >> getTemplate("daily/overview") >> needLogin,
    Menu("Daily3") / "daily" / * / * / * / * >> getTemplate("daily/overview") >> needLogin,
    Menu("Daily4") / "daily" / * / * / * / * / * >> getTemplate("daily/machine") >> needLogin,
    Menu("Machine1") / "machine" >> getTemplate("machine/index") >> needLogin,
    Menu("Machine2") / "machine" / * >> getTemplate("machine/overview") >> needLogin,
    Menu("Machine3") / "machine" / * / * >> getTemplate("machine/overview") >> needLogin,
    Menu("Machine4") / "machine" / * / * / * >> getTemplate("machine/detail") >> needLogin,
    Menu("Managemen1") / "management" / "index" >> needLogin,
    Menu("Managemen2") / "management" / "workers" / "add" >> needLogin,
    Menu("Managemen3") / "management" / "workers" / "index" >> needLogin,
    Menu("Managemen4") / "management" / "workers" / "barcode" >> Worker.barcodePDF,
    editWorkerMenu / "management" / "workers" / "edit" / * >> getTemplate("management/workers/edit") >> needLogin,
    Menu("Managemen5") / "management" / "alarms" / "add" >> needLogin,
    Menu("Managemen6") / "management" / "alarms" / "index" >> needLogin,
    editAlarmMenu / "management" / "alarms" / "edit" / * >> getTemplate("management/alarms/edit") >> needLogin,
    Menu("Workers") / "workers" / "index" >> needLogin,
    Menu("Workers1") / "workers" / * >> getTemplate("workers/worker") >> needLogin,
    Menu("Workers2") / "workers" / * / * >> getTemplate("workers/weekly") >> needLogin,
    Menu("Workers3") / "workers" / * / * / * >> getTemplate("workers/daily") >> needLogin,
    Menu("Workers4") / "workers" / * / * / * / * >> getTemplate("workers/detail") >> needLogin,
    Menu("machineLevel") / "management" / "machineLevel" >> needLogin,
    Menu("TodayOrder") / "todayOrder" >> needLogin,
    Menu("OrderStatus") / "orderStatus" >> needLogin,
    Menu("MachineMaintainLog") / "machineMaintainLog"
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
  }
}
