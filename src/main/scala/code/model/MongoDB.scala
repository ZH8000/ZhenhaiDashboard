package code.model

import com.mongodb.casbah.Imports._

object MongoDB {
  lazy val client = MongoClient()
  lazy val zhenhaiDB = client("zhenhai")
  lazy val zhenhaiDaily = client("zhenhaiDaily")
}
