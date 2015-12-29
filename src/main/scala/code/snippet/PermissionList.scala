package code.snippet

import code.lib._
import code.model._
import net.liftweb.http.SHtml
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds
import net.liftweb.util.Helpers._

/**
 *  用來顯示網頁上「網站管理」－＞「帳號管理」－＞「權限」的列表
 */
class PermissionList {

  /**
   *  顯示群限群組的名稱／具有的權限內容
   *
   *  @param    permission    代表該權限群組的資料庫 Record 物件
   */
  def basicInfoBinding(permission: Permission) = {
    ".permissionName *"  #> permission.permissionName &
    ".permissionContent" #> PermissionContent.allPermissions.map { content =>
      val hasPermission = permission.permissionContent.get.contains(content.toString)
      ".checkbox" #> SHtml.ajaxCheckbox(hasPermission, permission.setPermission(content)_) &
      ".checkboxLabel *" #> content.toString
    }
  }

  /**
   *  刪除在資料庫中的權限群組
   *
   *  @param    permissionID      要刪除的權限群組的 ID
   *  @param    value             從 HTML 傳進來的參數，沒用到
   */
  def onDelete(permissionID: String)(value: String) = {
    Permission.find(permissionID).foreach(_.delete_!)
    JsRaw(s"""jQuery('#row-$permissionID').remove()""").cmd
  }


  /**
   *  設定「刪除」按鈕按下去後要進行的動作
   *
   *  @param    permission      是哪個相對應的 Permission 物件要被刪除
   */
  def enableDeleteButton(permission: Permission) = {
    val mongoID = permission.id.toString
    val permissionName = permission.permissionName.get
    ".deleteLink [onclick]" #> 
      SHtml.onEventIf(s"確認要刪除【$permissionName】權限嗎？", onDelete(mongoID)_)
  }

  /**
   *  將刪除按鈕 disable 掉
   */
  def disableDeleteButton = {
    ".deleteLink [onclick]" #> SHtml.onEvent(s => JsCmds.Alert("尚有帳號在使用此權限，必需在無帳號使用這個權限時才能刪除"))
  }

  /**
   *  顯示權限列表
   */
  def render = {

    ".row" #> Permission.findAll.map { permission =>

      val isInUse = !User.find("permission", permission.permissionName.get).isEmpty
      val deleteButtonBinding = isInUse match {
        case true => disableDeleteButton
        case false => enableDeleteButton(permission)
      }

      ".row [id]" #> s"row-${permission.id.get.toString}" &
      basicInfoBinding(permission) & deleteButtonBinding
    }
  }

}

