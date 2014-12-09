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
  def workerMenu(menuID: String) = Menu.param[Worker](menuID, menuID, id => { val t = Worker.find(id); println(t); t}, worker => worker.id.get.toString)

  val t1 = workerMenu("t1")
  val t2 = workerMenu("t2")
  val t3 = workerMenu("t3")
  val t4 = workerMenu("t4")

  val siteMap = SiteMap(
    Menu("Home") / "index" >> redirectToDashboardIfLoggedIn,
    Menu("Logout") / "user" / "logout" >> logout,
    Menu("Dashboard") / "dashboard" >> needLogin,
    Menu("Dashboard") / "alert" >> needLogin,
    Menu("Alive") / "alive",
    Menu("Total1") / "total" >> getTemplate("total/overview") >> needLogin,
    Menu("Total2") / "total" / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total3") / "total" / * / * / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total4") / "total" / * / * / * / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total5") / "total" / * / * / * / * / * >> getTemplate("total/overview") >> needLogin,
    Menu("Total6") / "total" / * / * / * / * / * / * >> getTemplate("total/machine") >> needLogin,
    Menu("Monthly1") / "monthly" / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly2") / "monthly" / * / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly3") / "monthly" / * / * / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly4") / "monthly" / * / * / * / * >> getTemplate("monthly/overview") >> needLogin,
    Menu("Monthly4") / "monthly" / * / * / * / * / * >> getTemplate("monthly/machine") >> needLogin,
    Menu("Daily1") / "daily" / * / * >> getTemplate("daily/overview") >> needLogin,
    Menu("Daily2") / "daily" / * / * / * >> getTemplate("daily/overview") >> needLogin,
    Menu("Daily2") / "daily" / * / * / * / * >> getTemplate("daily/machine") >> needLogin,
    Menu("Machine1") / "machine" >> getTemplate("machine/overview") >> needLogin,
    Menu("Machine1") / "machine" / * >> getTemplate("machine/overview") >> needLogin,
    Menu("Machine1") / "machine" / * / * >> getTemplate("machine/overview") >> needLogin,
    Menu("Machine1") / "machine" / * / * / * >> getTemplate("machine/detail") >> needLogin,
    Menu("Management1") / "management" / "index" >> needLogin,
    Menu("Management1") / "management" / "workers" / "add" >> needLogin,
    Menu("Management1") / "management" / "workers" / "index" >> needLogin,
    Menu("Management1") / "management" / "workers" / "barcode" >> Worker.barcodePDF,
    editWorkerMenu / "management" / "workers" / "edit" / * >> getTemplate("management/workers/edit") >> needLogin,
    Menu("Management1") / "management" / "alarms" / "add" >> needLogin,
    Menu("Management1") / "management" / "alarms" / "index" >> needLogin,
    editAlarmMenu / "management" / "alarms" / "edit" / * >> getTemplate("management/alarms/edit") >> needLogin,
    Menu("Workers") / "workers" / "index",
    Menu("Workers1") / "workers" / * >> getTemplate("workers/worker"),
    Menu("Workers2") / "workers" / * / * >> getTemplate("workers/weekly"),
    Menu("Workers3") / "workers" / * / * / * >> getTemplate("workers/daily"),
    Menu("Workers4") / "workers" / * / * / * / * >> getTemplate("workers/detail"),
    Menu("machineLevel") / "management" / "machineLevel"
  )

  val ensureLogin: PartialFunction[Req, Unit] = {
    case req if User.isLoggedIn =>
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
  }
}
