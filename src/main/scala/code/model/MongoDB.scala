package code.model

import com.mongodb.casbah.Imports._
import net.liftweb.util.Props

object MongoDB {
  val DatabaseName: String = "zhenhai"

  lazy val client = MongoClient()
  lazy val zhenhaiDB = client(DatabaseName)
  lazy val zhenhaiDaily = client("zhenhaiDaily")
}
