package code.model

import com.mongodb.casbah.Imports._
import net.liftweb.util.Props

/**
 *  MongoDB 資料庫設定
 */
object MongoDB {

  /**
   *  在 MongoDB 是哪個資料庫
   */
  val DatabaseName: String = Props.get("dbName").getOrElse("zhenhai")

  /**
   *  MongoDB 連線的 URI
   */
  val DatabaseURI = new MongoClientURI(Props.get("dbURL").getOrElse("mongodb://localhost"))

  /**
   *  Casbah 直接用來操作 MongoDB 的連線物件
   */
  lazy val client = MongoClient(DatabaseURI)

  /**
   *  主要的統計資料庫
   */
  lazy val zhenhaiDB = client(DatabaseName)

  /**
   *  放置原始資料的資料庫
   */
  lazy val zhenhaiDaily = client("zhenhaiDaily")
}
