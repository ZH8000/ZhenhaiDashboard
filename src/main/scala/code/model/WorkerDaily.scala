package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  記錄每日員工生產量的資料表
 */
object WorkerDaily extends WorkerDaily with MongoMetaRecord[WorkerDaily] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "workerDaily"
}

/**
 *  記錄每日員工生產量的資料表
 */
class WorkerDaily extends MongoRecord[WorkerDaily] with ObjectIdPk[WorkerDaily] {

  /**
   *  此資料表對應到的 MongoMetaRecord
   */
  def meta = WorkerDaily

  /**
   *  員工在 MongoDB 中的 ID
   */
  val workerMongoID = new StringField(this, 24)

  /**
   *  良品數
   */
  val countQty = new LongField(this)

  /**
   *  日期
   */
  val timestamp = new StringField(this, 10)

  /**
   *  工班日期
   */
  val shiftDate = new StringField(this, 10)

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)
}


