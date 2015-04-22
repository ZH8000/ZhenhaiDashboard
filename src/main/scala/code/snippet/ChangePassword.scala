package code.snippet

import code.model._

import net.liftweb.common.{Box, Full, Empty, Failure}

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.StatefulSnippet

import net.liftweb.util.Helpers._
import java.util.Date
import java.text.SimpleDateFormat
import scala.util.Try

class ChangePassword extends StatefulSnippet {
  
  private var oldPassword: String = ""
  private var password: String = ""
  private var confirmPassword: String = ""

  def dispatch = {
    case "render" => render
  }

  def process(value: String): Unit = {
    val result = for {
      oldPassword <- Full(oldPassword).filterNot(_.trim.isEmpty) ?~ "請輸入舊的密碼"
      currentUser <- User.CurrentUser.get ?~ "查無此使用者"
      _ <- Full(oldPassword).filter(x => currentUser.password.match_?(x)) ?~ "舊密碼錯誤"
      passwordValue <- Full(password).filterNot(_.trim.isEmpty) ?~ "請輸入密碼"
      confirmPasswordValue <- Full(confirmPassword).filter(_ == passwordValue) ?~ "兩個密碼不符，請重新檢查"
      updatedUser <- currentUser.password(confirmPasswordValue).saveTheRecord()
    } yield {
      updatedUser
    }

    result match {
      case Full(worker) => S.redirectTo("/", () => S.notice("成功變更密碼"))
      case Failure(x, _, _) => S.error(x)
      case _ =>
    }

  }

  def render = {
    "#oldPassword" #> SHtml.password(oldPassword, oldPassword = _) &
    "#password" #> SHtml.password(password, password = _) &
    "#confirmPassword" #> SHtml.password(confirmPassword, confirmPassword = _) &
    "type=submit" #> SHtml.onSubmit(process _)
  }
}
