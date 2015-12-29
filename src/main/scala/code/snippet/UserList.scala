package code.snippet

import code.model._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE._
import net.liftweb.util.Helpers._

/**
 *  「網站管理」－＞「帳號管理」中的使用者列表的 Snippet
 */
class UserList {

  /**
   *  刪除資料庫內的使用者帳號資料
   *
   *  @param    userID    要刪除的使用者名稱
   */
  def onDelete(userID: String)(value: String) = {
    User.find(userID).foreach(_.delete_!)
    JsRaw(s"""jQuery('#row-$userID').remove()""").cmd
  }

  /**
   *  用來列出帳號列表
   */
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

