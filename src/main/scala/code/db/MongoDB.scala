package code.db

import com.mongodb.casbah.Imports._

object MongoDB {
  lazy val client = MongoClient()
  lazy val zhenhaiDB = client("zhenhai")
}
