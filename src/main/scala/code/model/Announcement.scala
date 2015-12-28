package code.model

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._


/**
 *  此資料表紀錄網頁上的跑馬燈公佈欄
 */
object Announcement extends Announcement with MongoMetaRecord[Announcement] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "announcement"
}

/**
 *  此資料表紀錄網頁上的跑馬燈公佈欄
 *
 *  實際上此資料表只會有一筆資料，因為每次更新跑馬燈公佈欄時，
 *  都會直接把舊的資料刪除。
 *
 *  在網頁的顯示上，content 欄位的每一行會十秒輪播一次。
 */
class Announcement extends MongoRecord[Announcement] with ObjectIdPk[Announcement] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = Announcement

  /**
   *  公佈欄的內容
   */
  val content = new OptionalStringField(this, None)
}

