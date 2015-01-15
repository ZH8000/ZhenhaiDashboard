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

  def hasPermission(requiredPermission: PermissionContent.Value): Boolean = {

    permission.get match {
      case "administrator" => true
      case _ =>
        val permissions = Permission.find("permissionName", permission.toString)
                                    .map(_.permissionContent.get)
                                    .openOr(Nil)

        permissions.contains(requiredPermission.toString)
    }
  }
}

