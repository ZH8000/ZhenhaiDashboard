package code.model

import code.lib._
import net.liftweb.common.{Box, Empty}
import net.liftweb.http.SessionVar
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  此資料表記錄使用的帳號密碼
 */
object User extends User with MongoMetaRecord[User] {

  /**
   *  記錄目前登入的使用者的 Session 變數物件
   */
  object CurrentUser extends SessionVar[Box[User]](Empty)

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "user"

  /**
   *  目前瀏覽的使用者是否已登入（有相對應的 Session 變數）
   */
  def isLoggedIn = !CurrentUser.is.isEmpty

  /**
   *  測試使用者帳密並取得使用者物件
   *
   *  此函式會測試使用者輸入的帳密，若帳號密碼正確，則會回傳 Full[User]，
   *  否則會回傳 Empty 物件。
   *
   *  @param    username    使用者帳號
   *  @param    password    使用者密碼
   *  @return               若帳號密碼正確，則會回傳 Full[User]，否則會回傳 Empty 物件。
   */
  def loginAs(username: String, password: String): Box[User] = {
    User.find("username", username).filter(_.password.match_?(password))
  }

}

/**
 *  此資料表記錄使用的帳號密碼
 */
class User extends MongoRecord[User] with ObjectIdPk[User] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = User

  /**
   *  使用者名稱
   */
  val username = new StringField(this,20)

  /**
   *  姓名
   */
  val name = new StringField(this, 20)

  /**
   *  使用者的台容員工編號
   */
  val employeeID = new StringField(this, 20)

  /**
   *  該使用者的 EMail
   */
  val email = new EmailField(this, 255)

  /**
   *  經過 Salt+Hash 的使用者密碼
   */
  val password = new PasswordField(this)

  /**
   *  該使用者的權限群組
   */
  val permission = new StringField(this, "administrator")

  /**
   *  該使用者帳號的建立時間
   */
  val createdAt = new DateTimeField(this)

  /**
   *  此帳號資訊最後一次更新的時間
   */
  val updateAt = new DateTimeField(this)

  /**
   *  檢查使用者是否具有特定權限
   *
   *  @param    requiredPermission    要檢查的權限
   *  @return                         如果有此權限則為 true，否則為 false
   */
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

