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


