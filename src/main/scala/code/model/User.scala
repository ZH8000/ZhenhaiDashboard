package code.model

import com.mongodb.casbah.Imports._

import net.liftweb.util.BCrypt
import net.liftweb.common.{Box, Full, Empty, Failure}
import net.liftweb.http.SessionVar

case class User(id: String, username: String)

object User {

  object CurrentUser extends SessionVar[Box[User]](Empty)

  def isLoggedIn = !CurrentUser.is.isEmpty

  def loginAs(username: String, password: String): Box[User] = {

    val users = MongoDB.zhenhaiDB("user")
    val loggedInUser = for {
      userRecord <- users.findOne(MongoDBObject("username" -> username))
      id <- userRecord._id
      username <- userRecord.getAs[String]("username")
      hashedPassword <- userRecord.getAs[String]("password") if BCrypt.checkpw(password, hashedPassword)
    } yield { 
      User(id.toString, username) 
    }

    loggedInUser
  }
}

