package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  員工效率資料表
 *
 *  此資料表用在網站上的「產量統計」－＞「員工效率」的 Excel 報表中。
 *
 */
object WorkerPerformance extends WorkerPerformance with MongoMetaRecord[WorkerPerformance] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "workerPerformance"
}

/**
 *  員工效率資料表
 *
 *  此資料表用在網站上的「產量統計」－＞「員工效率」的 Excel 報表中。
 */
class WorkerPerformance extends MongoRecord[WorkerPerformance] with ObjectIdPk[WorkerPerformance] {

  /**
   *  對應到哪個 MongoMetaRecord
   */
  def meta = WorkerPerformance

  /**
   *  員工在 MongoID 中的 ID
   */
  val workerMongoID = new StringField(this, 255)

  /**
   *  時間戳記
   */
  val timestamp = new StringField(this, 10)

  /**
   *  工班日期
   */
  val shiftDate = new StringField(this, 10)

  /**
   *  產品尺吋代碼
   */
  val productCode = new StringField(this, 20)

  /**
   *  月份
   */
  val month = new StringField(this, 10)

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)

  /**
   *  工單號
   */
  val lotNo = new StringField(this, 100)

  /**
   *  良品數
   */
  val countQty = new LongField(this)

}

