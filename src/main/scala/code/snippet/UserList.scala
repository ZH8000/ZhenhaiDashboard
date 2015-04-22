package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.js.JE._

class UserList {

  def onDelete(userID: String)(value: String) = {
    User.find(userID).foreach(_.delete_!)
    JsRaw(s"""jQuery('#row-$userID').remove()""").cmd
  }

  def render = {
    ".row" #> User.findAll.map { user =>

      ".row [id]" #> s"row-${user.id}" &
      ".username *" #> user.username &
      ".workID *" #> user.employeeID &
      ".email *" #> user.email &
      ".permission *" #> user.permission &
      ".editLink [href]" #> s"/management/account/edit/${user.id}" &
      ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${user.username}】這個帳號嗎？", onDelete(user.id.get.toString)_)
    }
  }

}

