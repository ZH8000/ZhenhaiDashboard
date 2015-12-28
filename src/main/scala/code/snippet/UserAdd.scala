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

/**
 *  用來處理「網站管理」－＞「帳號管理」中「新增帳號」表單的 Snippet
 */
class UserAdd extends StatefulSnippet {
  
  private var username: String = _            // 用來儲存表單上傳入的使用者帳號
  private var workerID: String = _            // 用來儲存表單上傳入的台容的工號
  private var email: String = _               // 用來儲存表單上傳入的EMail
  private var password: String = ""           // 用來儲存表單上傳入的密碼
  private var confirmPassword: String = ""    // 用來儲存表單上傳入的確認密碼
  private var permission: String = _          // 用來儲存表單上傳入的權限群組

  /**
   *  檢查使用者名稱是否在資料庫內已存在
   *
   *  @param    username      要新增的使用者名稱
   *  @return                 如果已經有這個使用者名稱則為 true，否則為 false
   */
  def hasNoDuplicateUsername(username: String) = User.find("username", username).isEmpty

  /**
   *  新增使用者至資料庫中
   */
  def addUser() = {

    val newUser = for {
      usernameValue   <- Option(username).filterNot(_.trim.isEmpty) ?~ "請輸入帳號"
      _               <- Option(username).filter(hasNoDuplicateUsername) ?~ "系統內已有重覆帳號"
      workerIDValue   <- Option(workerID).filterNot(_.trim.isEmpty) ?~ "請輸入工號"
      emailValue      <- Option(email).filterNot(_.trim.isEmpty) ?~ "請輸入電子郵件帳號"
      permissionValue <- Option(permission).filterNot(_.trim.isEmpty) ?~ "請選擇帳號的權限"
      passwordValue   <- Option(password).filterNot(_.trim.isEmpty) ?~ "請輸入密碼"
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
                   | 網址：http://221.4.141.146
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

  /**
   *  設定新增使用者的 HTML 表單
   */
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
