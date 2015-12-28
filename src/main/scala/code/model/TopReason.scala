package code.model

import com.mongodb.casbah.Imports._

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  儲存用來顯示網站上「錯誤分析」－＞「今日五大錯誤」的資料表
 */
object TopReason extends TopReason with MongoMetaRecord[TopReason] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "topReason"
}

/**
 *  儲存用來顯示網站上「錯誤分析」－＞「今日五大錯誤」的資料表
 */
class TopReason extends MongoRecord[TopReason] with ObjectIdPk[TopReason] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = TopReason

  /**
   *  日期
   */
  val date = new StringField(this, 10)

  /**
   *  工班日期
   */
  val shiftDate = new StringField(this, 10)

  /**
   *  統一錯誤 ID
   */
  val defact_id = new IntField(this)

  /**
   *  錯誤事件數量
   */
  val event_qty = new LongField(this)

  /**
   *  機台編號
   */
  val mach_id = new StringField(this, 15)

  /**
   *  機台型號
   */
  val mach_model = new StringField(this, 20)

  /**
   *  機台製程（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   */
  val machine_type = new IntField(this, -1)

}

