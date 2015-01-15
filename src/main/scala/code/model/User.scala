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

object User extends User with MongoMetaRecord[User] {

  override def collectionName = "user"

  object CurrentUser extends SessionVar[Box[User]](Empty)

  def isLoggedIn = !CurrentUser.is.isEmpty

  def loginAs(username: String, password: String): Box[User] = {
    User.find("username", username).filter(_.password.match_?(password))
  }

}

class User extends MongoRecord[User] with ObjectIdPk[User] {
  def meta = User

  val username = new StringField(this,20)
  val employeeID = new StringField(this, 20)
  val email = new EmailField(this, 255)
  val password = new PasswordField(this)
  val permission = new StringField(this, "administrator")
  val createdAt = new DateTimeField(this)
  val updateAt = new DateTimeField(this)
}

