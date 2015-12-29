package code.model

import code.lib._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  記錄生產紀錄歷史的資料表
 */
object ProductionStatusHistory extends ProductionStatusHistory with MongoMetaRecord[ProductionStatusHistory] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "productionStatusHistory"
}

/**
 *  記錄生產紀錄歷史的資料表
 */
class ProductionStatusHistory extends MongoRecord[ProductionStatusHistory] with ObjectIdPk[ProductionStatusHistory] {

  /**
   *  此資料表對應到的 MongoMetaRecord
   */
  def meta = ProductionStatusHistory

  /**
   *  料號
   */
  val partNo = new StringField(this, 100)

  /**
   *  工單號
   */
  val lotNo = new StringField(this, 100)

  /**
   *  產品尺吋
   */
  val product = new StringField(this, 100)

  /**
   *  加締機的狀態
   */
  val step1Status = new IntField(this, -1)

  /**
   *  組立機的狀態
   */
  val step2Status = new IntField(this, -1)

  /**
   *  老化機的狀態
   */
  val step3Status = new IntField(this, -1)

  /**
   *  選別機的狀態
   */
  val step4Status = new IntField(this, -1)

  /**
   *  加工切腳機的狀態
   */
  val step5Status = new IntField(this, -1)

  /**
   *  工班日期
   */
  val shiftDate = new StringField(this, 10)

  /**
   *  從料號取得的客戶名稱
   */
  def customer = Customer.fromPartNo(partNo.get)
}

