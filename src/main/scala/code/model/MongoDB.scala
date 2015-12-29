package code.model

import com.mongodb.casbah.Imports._

/**
 *  MongoDB 資料庫設定
 */
object MongoDB {

  /**
   *  在 MongoDB 是哪個資料庫
   */
  val DatabaseName: String = "zhenhai"

  lazy val client = MongoClient()

  /**
   *  主要的統計資料庫
   */
  lazy val zhenhaiDB = client(DatabaseName)

  /**
   *  放置原始資料的資料庫
   */
  lazy val zhenhaiDaily = client("zhenhaiDaily")
}
