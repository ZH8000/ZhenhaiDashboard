package code.snippet

import code.model._

import net.liftweb.common.{Box, Full, Empty, Failure}

import net.liftweb.http.S
import net.liftweb.http.SHtml

import net.liftweb.util.Helpers._

class Login {

  private def setupSessionAndRedirect(user: User) = {
    User.CurrentUser(Full(user))
    S.redirectTo("/dashboard")
  }

  def login = {

    val loggedInUser = for {
      username <- S.param("username").filterNot(_.trim.isEmpty) ?~ "請輸入帳號"
      password <- S.param("password").filterNot(_.trim.isEmpty) ?~ "請輸入密碼"
      user <- User.loginAs(username, password) ?~ "帳號或密碼錯誤"
    } yield user

    loggedInUser match {
      case Full(user) => setupSessionAndRedirect(user)
      case Failure(msg, _, _) => S.error(msg)
      case Empty => S.error("系統異常無法登入，請連絡管理者")
    }

  }

  def render = {
    "type=submit" #> SHtml.onSubmitUnit(login _)
  }
}
