package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.StatefulSnippet

import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.common.Box._
import net.liftweb.common._
import net.liftweb.util.Mailer
import net.liftweb.util.Mailer._

class UserAdd extends StatefulSnippet {

  
  private var username: String = _
  private var workerID: String = _
  private var email: String = _
  private var password: String = ""
  private var confirmPassword: String = ""
  private var permission: String = _

  def dispatch = {
    case "render" => render
  }

  def hasNoDuplicateUsername(username: String) = User.find("username", username).isEmpty
  def addUser() = {

    val newUser = for {
      usernameValue <- Option(username).filterNot(_.trim.isEmpty) ?~ "請輸入帳號"
      _             <- Option(username).filter(hasNoDuplicateUsername) ?~ "系統內已有重覆帳號"
      workerIDValue <- Option(workerID).filterNot(_.trim.isEmpty) ?~ "請輸入工號"
      emailValue    <- Option(email).filterNot(_.trim.isEmpty) ?~ "請輸入電子郵件帳號"
      permissionValue <- Option(permission).filterNot(_.trim.isEmpty) ?~ "請選擇帳號的權限"
      passwordValue <- Option(password).filterNot(_.trim.isEmpty) ?~ "請輸入密碼"
      confirmPasswordValue <- Option(confirmPassword).filter(_ == passwordValue) ?~ "兩個密碼不符，請重新檢查"
    } yield {
      User.createRecord.username(usernameValue)
          .employeeID(workerIDValue)
          .email(emailValue)
          .permission(permissionValue)
          .password(passwordValue)
    }


    newUser match {
      case Empty => S.error("無法儲存至資料庫，請稍候再試")
      case Failure(message, _, _) => S.error(message)
      case Full(user) => 
        user.saveTheRecord() match {
          case Full(_) => 

            Mailer.sendMail(
              From("admin@zhenhai.com.tw"),
              Subject("雲端系統帳號已建立"),
              To(user.email.get),
              PlainMailBodyType(
                """| Dear %s
                   |
                   | 已建立雲端系統帳號，請使用以下帳號密碼登入：
                   |
                   | 帳號：%s
                   | 密碼：%s
                   |""".stripMargin.format(user.username.get, user.username.get, password)
              ) 
            )

            S.redirectTo(
              "/management/account/", 
              () => S.notice(s"已成功新增帳號【${user.username}】")
            )
          case _ => S.error("無法儲存至資料庫，請稍候再試")
        }
    }

  }

  def render = {

    ".permissionItem" #> Permission.findAll.map { permission =>
      ".permissionItem *" #> permission.permissionName &
      ".permissionItem [value]" #> permission.permissionName
    } andThen
    "name=username" #> SHtml.text(username, username = _) &
    "name=workerID" #> SHtml.text(workerID, workerID = _) &
    "name=email"    #> SHtml.text(email, email = _) &
    "name=password" #> SHtml.password(password, password = _) &
    "name=confirmPassword" #> SHtml.password(confirmPassword, confirmPassword = _) &
    "#permissionSelect" #> SHtml.onSubmit(permission = _) &
    "#submitButton" #> SHtml.onSubmitUnit(addUser _)
  }

}
