package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  紀錄每日每台機台的良品數／事件數及最後一個狀態的資料表
 */
object DailyMachineCount extends DailyMachineCount with MongoMetaRecord[DailyMachineCount] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "dailyMachineCount"
}

/**
 *  紀錄每日每台機台的良品數／事件數及最後一個狀態的資料表
 */
class DailyMachineCount extends MongoRecord[DailyMachineCount] with ObjectIdPk[DailyMachineCount] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = DailyMachineCount

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)

  /**
   *  日期
   */
  val insertDate = new StringField(this, 10)

  /**
   *  當日良品數
   */
  val count_qty = new LongField(this)

  /**
   *  當日事件數
   */
  val event_qty = new LongField(this)

  /**
   *  當日該台機台的最後一筆機台狀態
   */
  val status = new StringField(this, 10)
}

