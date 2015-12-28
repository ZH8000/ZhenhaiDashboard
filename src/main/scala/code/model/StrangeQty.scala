package code.model

import com.mongodb.casbah.Imports._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  用來記錄異常數量的資料表
 *
 *  當 RaspberryPi 傳過來的一筆資料的良品數或事件數超過 2000 的時候，
 *  會記錄到此資料表。
 *
 */
object StrangeQty extends StrangeQty with MongoMetaRecord[StrangeQty] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "strangeQty"
}

/**
 *  用來記錄異常數量的資料表
 *
 *  當 RaspberryPi 傳過來的一筆資料的良品數或事件數超過 2000 的時候，
 *  會記錄到此資料表。
 *
 */
class StrangeQty extends MongoRecord[StrangeQty] with ObjectIdPk[StrangeQty] {

  /**
   *  此資料表對應到的 MongoMetaRecord 物件
   */
  def meta = StrangeQty

  /**
   *  良品數
   */
  val count_qty = new LongField(this)

  /**
   *  時間戳記
   */
  val emb_date = new LongField(this)

  /**
   *  事件數
   */
  val event_qty = new LongField(this)

  /**
   *  事件 ID
   */
  val defact_id = new IntField(this)

  /**
   *  機台編號
   */
  val mach_id = new StringField(this, 15)

  /**
   *  原始的事件 ID
   */
  val originEventID = new IntField(this, -1)
}

