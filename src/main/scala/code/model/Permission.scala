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

object Permission extends Permission with MongoMetaRecord[Permission]

class Permission extends MongoRecord[Permission] with ObjectIdPk[Permission] {
  def meta = Permission

  val permissionName = new StringField(this,150)
  val permissionContent = new MongoListField[Permission, String](this)

  def setPermission(permission: PermissionContent.Value)(hasPermission: Boolean) = {
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

