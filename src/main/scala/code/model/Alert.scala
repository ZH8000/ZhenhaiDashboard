package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  這個資料表紀錄了良品數為 -1 的異常資料
 */
object Alert extends Alert with MongoMetaRecord[Alert] {

  /**
   *  這個資料表在 MongoDB 中的名稱
   */
  override def collectionName = "alert"
}

/**
 *  這個資料表紀錄了良品數為 -1 的異常資料
 */
class Alert extends MongoRecord[Alert] with ObjectIdPk[Alert] {

  /**
   *  這個資料表對應到哪個 MongoMetaRecord
   */
  def meta = Alert

  /**
   *  該筆資料的 UNIX 時間戳記
   */
  val timestamp = new StringField(this, 20)

  /**
   *  是哪個 event 發生異常
   */
  val eventID = new IntField(this)

  /**
   *  該筆資料屬於哪台機台
   */
  val mach_id = new StringField(this, 15)
}

