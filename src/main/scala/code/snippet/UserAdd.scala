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
          case Full(_) => S.redirectTo("/management/account/", () => S.notice(s"已成功新增帳號【${user.username}】"))
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


class PermissionAdd extends StatefulSnippet {

  
  private var permissionName: String = _
  private var permissionContent: List[String] = Nil

  def dispatch = {
    case "render" => render
  }

  def hasNoDuplicatePermission(permission: String) = Permission.find("permission", permission).isEmpty
  def addPermission() = {

    val newPermission = for {
      permissionNameValue <- Option(permissionName).filterNot(_.trim.isEmpty) ?~ "請輸入權限名稱"
      _                   <- Option(permissionName).filter(hasNoDuplicatePermission) ?~ "系統內已有重覆權限"
    } yield {
      Permission.createRecord
                .permissionName(permissionNameValue)
                .permissionContent(permissionContent)
    }


    newPermission match {
      case Empty => S.error("無法儲存至資料庫，請稍候再試")
      case Failure(message, _, _) => S.error(message)
      case Full(permission) => 
        permission.saveTheRecord() match {
          case Full(_) => S.redirectTo("/management/account/", () => S.notice(s"已成功新增權限【${permission.permissionName}】"))
          case _ => S.error("無法儲存至資料庫，請稍候再試")
        }
    }

  }

  def render = {

    def onCheck(selected: Seq[PermissionContent.Value]) = {
      permissionContent = selected.toList.map(_.toString)
    }

    "name=permissionName" #> SHtml.text(permissionName, permissionName = _) &
    "name=permissionContent" #> SHtml.checkbox(PermissionContent.allPermissions, Nil, onCheck _).toForm &
    "#submitButton" #> SHtml.onSubmitUnit(addPermission _)

  }

}


