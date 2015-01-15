package code.snippet

import code.model._
import code.lib._

import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds


import scala.xml.NodeSeq
import scala.collection.JavaConversions._
import java.text.SimpleDateFormat

class PermissionList {

  def render = {

    def basicInfoBinding(permission: Permission) = {
      ".permissionName *"  #> permission.permissionName &
      ".permissionContent" #> PermissionContent.allPermissions.map { content =>
        val hasPermission = permission.permissionContent.get.contains(content.toString)
        ".checkbox" #> SHtml.ajaxCheckbox(hasPermission, permission.setPermission(content)_) &
        ".checkboxLabel *" #> content.toString
      }
    }

    def onDelete(permissionID: String)(value: String) = {
      Permission.find(permissionID).foreach(_.delete_!)
      JsRaw(s"""jQuery('#row-$permissionID').remove()""").cmd
    }


    def enableDeleteButton(permission: Permission) = {
      val mongoID = permission.id.toString
      val permissionName = permission.permissionName.get
      ".deleteLink [onclick]" #> 
        SHtml.onEventIf(s"確認要刪除【$permissionName】權限嗎？", onDelete(mongoID)_)
    }

    def disableDeleteButton = {
      ".deleteLink [onclick]" #> SHtml.onEvent(s => JsCmds.Alert("尚有帳號在使用此權限，必需在無帳號使用這個權限時才能刪除"))
    }

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
      ".deleteLink [onclick]" #> SHtml.onEventIf(s"確定要刪除【${user.username}】這個帳號嗎？", onDelete(user.id.get.toString)_)
    }
  }

}

class AlertList {

  def showEmptyBox() = {
     S.error("查無機台異常")
     ".dataBlock" #> NodeSeq.Empty
  }

  def dateList = {
    val dateList = Alert.useColl(collection => collection.distinct("date")).toList

    ".alertDate" #> dateList.map { date =>
      "a [href]" #> s"/alert/alert/$date" &
      "a *"      #> date.toString
    }
  }

  def render = {

    val Array(_, _, date) = S.uri.drop(1).split("/")
    val alertList = Alert.findAll("date", date).toList.sortWith(_.timestamp.get < _.timestamp.get)

    alertList.isEmpty match {
      case true => showEmptyBox()
      case false =>
        ".row" #> alertList.map { item =>

          val errorDesc = MachineInfo.getErrorDesc(item.mach_id.get, item.defact_id.get)

          ".timestamp *" #> item.timestamp &
          ".machineID *" #> item.mach_id &
          ".defactID *" #> errorDesc
        }
    }
  }
}

