package code.snippet

import code.model._
import net.liftweb.common.{Empty, Failure, Full}
import net.liftweb.http.{S, SHtml}
import net.liftweb.util.Helpers._

/**
 *  用來處理使用者登入頁面表單的 Snippet
 */
class Login {

  /**
   *  設定目前登入使用者的 Session 變數，並導到 /dashboard 主頁
   *
   *  @param    user      目前登入的使用者
   */
  private def setupSessionAndRedirect(user: User) = {
    User.CurrentUser(Full(user))
    S.redirectTo("/dashboard")
  }

  /**
   *  使用者按下「登入」按鈕後會執行此函式來檢查帳密並登入
   */
  def login = {

    val loggedInUser = for {
      username <- S.param("username").filterNot(_.trim.isEmpty) ?~ "請輸入帳號"
      password <- S.param("password").filterNot(_.trim.isEmpty) ?~ "請輸入密碼"
      user     <- User.loginAs(username, password) ?~ "帳號或密碼錯誤"
    } yield user

    loggedInUser match {
      case Full(user) => setupSessionAndRedirect(user)
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("系統異常無法登入，請連絡管理者")
    }

  }

  /**
   *  登入表單的設定
   */
  def render = {
    "type=submit" #> SHtml.onSubmitUnit(login _)
  }
}
