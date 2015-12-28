package code.model

import com.mongodb.casbah.Imports._

import net.liftweb.util.BCrypt
import net.liftweb.common.{Box, Full, Empty, Failure}
import net.liftweb.http.SessionVar
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

import code.lib._
import net.liftweb.http.S

/**
 *  此資料表記錄網頁上使用者可以使用哪些權限群組
 */
object Permission extends Permission with MongoMetaRecord[Permission] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "permissions"
}

/**
 *  此資料表記錄網頁上使用者可以使用哪些權限群組
 */
class Permission extends MongoRecord[Permission] with ObjectIdPk[Permission] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = Permission

  /**
   *  權限群組名稱
   */
  val permissionName = new StringField(this,150)

  /**
   *  此權限群組具有哪些權限
   */
  val permissionContent = new MongoListField[Permission, String](this)

  /**
   *  設定此權限群組是否有某個特定權限
   *
   *  @param      permission        哪個權限
   *  @param      hasPermission     這個群組是否具有此權限（true = 新增此權限 / false = 移除此權限）
   */
  def setPermission(permission: PermissionContent.Value)(hasPermission: Boolean) = {

    // 新的權限列表
    val newPermissionContent = hasPermission match {
      case false => permissionContent.get.filterNot(_ == permission.toString)
      case true  => permission.toString :: permissionContent.get
    }

    permissionContent(newPermissionContent)

    val verb = if (hasPermission) "新增" else "移除"
 
    this.saveTheRecord() match {
      case Full(_) => S.notice(s"已$verb" + s"$permissionName 的 $permission" + "權限")
      case _ => S.error("無法存檔，請稍候再試")
    }
  }
}
