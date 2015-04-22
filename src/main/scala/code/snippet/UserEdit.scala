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

class UserEdit(user: User) extends StatefulSnippet {

  
  private var username: String = user.username.get
  private var workerID: String = user.employeeID.get
  private var email: String = user.email.get
  private var password: String = ""
  private var confirmPassword: String = ""
  private var permission: String = user.permission.get

  def dispatch = {
    case "render" => render
  }

  def editUser() = {

    val updatedUser = for {
      workerIDValue <- Option(workerID).filterNot(_.trim.isEmpty) ?~ "請輸入工號"
      emailValue    <- Option(email).filterNot(_.trim.isEmpty) ?~ "請輸入電子郵件帳號"
      permissionValue <- Option(permission).filterNot(_.trim.isEmpty) ?~ "請選擇帳號的權限"
      passwordValue <- Option(password).filterNot(_.trim.isEmpty) ?~ "請輸入密碼"
      confirmPasswordValue <- Option(confirmPassword).filter(_ == passwordValue) ?~ "兩個密碼不符，請重新檢查"
    } yield {
      user.employeeID(workerIDValue)
          .email(emailValue)
          .permission(permissionValue)
          .password(passwordValue)
    }


    updatedUser match {
      case Empty => S.error("無法儲存至資料庫，請稍候再試")
      case Failure(message, _, _) => S.error(message)
      case Full(user) => 
        user.saveTheRecord() match {
          case Full(_) => S.redirectTo("/management/account/", () => S.notice(s"已成功新增帳號【${user.username}】"))
          case _ => S.error("無法儲存至資料庫，請稍候再試")
        }
    }

  }

  def render = {

    ".permissionItem" #> Permission.findAll.map { permission =>
      val notSelected = 
        ".permissionItem *" #> permission.permissionName &
        ".permissionItem [value]" #> permission.permissionName
      val selected = 
        ".permissionItem *" #> permission.permissionName &
        ".permissionItem [value]" #> permission.permissionName &
        ".permissionItem [selected]" #> "selected"

      if (permission.permissionName.get == user.permission.get) selected else notSelected

    } andThen
    "name=username [value]" #> username &
    "name=workerID" #> SHtml.text(workerID, workerID = _) &
    "name=email"    #> SHtml.text(email, email = _) &
    "name=password" #> SHtml.password(password, password = _) &
    "name=confirmPassword" #> SHtml.password(confirmPassword, confirmPassword = _) &
    "#permissionSelect" #> SHtml.onSubmit(permission = _) &
    "#submitButton" #> SHtml.onSubmitUnit(editUser _)
  }

}
