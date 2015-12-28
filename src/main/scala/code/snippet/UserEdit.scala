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

/**
 *  用來處理「網站管理」－＞「帳號管理」－＞「帳號」－＞「編輯」的表單的 Snippet
 *
 *  @param    user    要編輯的使用者
 */
class UserEdit(user: User) extends StatefulSnippet {
  
  private var username: String = user.username.get      // 用來儲存從 HTML 表單傳入的使用者帳號
  private var workerID: String = user.employeeID.get    // 用來儲存從 HTML 表單傳入的台容的工號
  private var email: String = user.email.get            // 用來儲存從 HTML 表單傳入的Email
  private var password: String = ""                     // 用來儲存從 HTML 表單傳入的密碼
  private var confirmPassword: String = ""              // 用來儲存從 HTML 表單傳入的確認密碼
  private var permission: String = user.permission.get  // 用來儲存從 HTML 表單傳入的權限群組

  /**
   *  更新資料庫裡的使用者的資料
   */
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

  /**
   *  HTML 表單的設定
   */
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

  /**
   *  指定 HTML 中呼叫的 Snippet 名稱要對應到哪些函式
   *
   *  為了讓使用者在輸入表單後，若有錯誤而無法進行時，原先輸入的值還會留在表單上，需
   *  要使用 StatefulSnippet 的機制，也就是讓此類別繼承自 StatefulSnippet 這個 trait。
   *
   *  但如果是 StatefulSnippet，會需要自行指定 HTML 模板中， data-lift="ChangePassword.render" 裡
   *  面的 "render" 對應到哪個函式。
   */
  def dispatch = {
    case "render" => render
  }

}
