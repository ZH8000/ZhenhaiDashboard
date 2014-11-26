package bootstrap.liftweb

import code.model.User

import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.http.S
import net.liftweb.http.Templates
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.If
import net.liftweb.sitemap.Loc.EarlyResponse
import net.liftweb.sitemap.Loc.Template

import net.liftweb.util.BasicTypesHelpers._

import scala.xml.NodeSeq

import net.liftweb.common.{Full, Empty}
import net.liftweb.http._


class Boot 
{
  private def getTemplate(path: String) = Template(() => Templates(path.split("/").toList) openOr NodeSeq.Empty)
  private def needLogin = If(() => User.isLoggedIn, () => S.redirectTo("/", () => S.notice("請先登入")))
  private def redirectToDashboardIfLoggedIn = If(() => !User.isLoggedIn, () => S.redirectTo("/dashboard"))
  private def logout = EarlyResponse{ () =>
    User.isLoggedIn match {
      case false => Full(NotFoundResponse("NotFound"))
      case true => 
        User.CurrentUser(Empty)
        S.redirectTo("/", () => S.notice("已登出"))
    }
  }

  lazy val siteMap = SiteMap(
    Menu("Home") / "index" >> redirectToDashboardIfLoggedIn,
    Menu("Logout") / "user" / "logout" >> logout,
    Menu("Dashboard") / "dashboard" >> needLogin,
    Menu("Dashboard") / "alert" >> needLogin,
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
    Menu("Machine1") / "machine" / * / * / * >> getTemplate("machine/detail") >> needLogin
  )

  val ensureLogin: PartialFunction[Req, Unit] = {
    case req if User.isLoggedIn =>
  }

  def boot 
  {
    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.addToPackages("code")
    LiftRules.setSiteMap(siteMap)
    LiftRules.dispatch.append(ensureLogin guard JsonRestAPI)
    LiftRules.dispatch.append(ensureLogin guard CsvRestAPI)
  }
}
