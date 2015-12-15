package bootstrap.liftweb

import code.model._
import com.mongodb.MongoClient
import javax.mail.{Authenticator,PasswordAuthentication}
import net.liftweb.common.Full
import net.liftweb.http._
import net.liftweb.http.LiftRules
import net.liftweb.http.Req
import net.liftweb.http.S
import net.liftweb.mongodb.MongoDB
import net.liftweb.util.BasicTypesHelpers._
import net.liftweb.util.DefaultConnectionIdentifier
import net.liftweb.util.Props.RunModes
import net.liftweb.util.{Props, Mailer}

class Boot 
{
  val ensureLogin: PartialFunction[Req, Unit] = {
    case req if User.isLoggedIn => 
  }

  def errorPageResponse(req: Req, code: Int) = {
    val content = S.render(<lift:embed what={code.toString} />, req.request)
    XmlResponse(content.head, code, "text/html", req.cookies)
  }

  def boot 
  {
    MongoDB.defineDb(DefaultConnectionIdentifier, new MongoClient, code.model.MongoDB.DatabaseName)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    LiftRules.addToPackages("code")
    LiftRules.setSiteMap(SiteMapDefine.siteMap)
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
