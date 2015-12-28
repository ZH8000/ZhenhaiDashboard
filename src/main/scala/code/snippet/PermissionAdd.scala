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
 *  用來處理新增權限至權限群組的 Snippet
 */
class PermissionAdd extends StatefulSnippet {

  
  private var permissionName: String = _              // 用來儲存權限群組的名稱
  private var permissionContent: List[String] = Nil   // 此群組有哪些權限

  /**
   *  檢查是否有重覆的權限群組名稱
   *
   *  @param    permission        權限群組名稱
   *  @return                     如果有同樣的名稱為 true，否則為 false
   */
  def hasNoDuplicatePermission(permission: String) = Permission.find("permission", permission).isEmpty

  /**
   *  當按下新增按鈕時要執行的動作－新增權限群組至資料庫
   */
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

  /**
   *  綁定操作 HTML 表單時要進行的動作
   */
  def render = {

    def onCheck(selected: Seq[PermissionContent.Value]) = {
      permissionContent = selected.toList.map(_.toString)
    }

    "name=permissionName" #> SHtml.text(permissionName, permissionName = _) &
    "name=permissionContent" #> SHtml.checkbox(PermissionContent.allPermissions, Nil, onCheck _).toForm &
    "#submitButton" #> SHtml.onSubmitUnit(addPermission _)
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


