package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import scala.collection.JavaConversions._

/**
 *  維修紀錄資料表
 */
object MachineMaintainLog extends MachineMaintainLog with MongoMetaRecord[MachineMaintainLog] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "machineMaintainLog"

  /**
   *  所有有維修紀錄的日期的列表
   */
  def dateList = MachineMaintainLog.useColl(collection => collection.distinct("insertDate")).toList.map(_.toString).sortWith(_ > _)
}

/**
 *  維修紀錄資料表
 */
class MachineMaintainLog extends MongoRecord[MachineMaintainLog] with ObjectIdPk[MachineMaintainLog] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = MachineMaintainLog

  /**
   *  員工在 MongoDB 中的 ID
   */
  val workerMongoID = new StringField(this, 24)

  /**
   *  維修紀錄的時間戳記
   */
  val timestamp = new LongField(this)

  /**
   *  此次維修是什麼時候刷進維修模式的？
   */
  val startTimestamp = new LongField(this)

  /**
   *  維修項目代碼
   */
  val maintenanceCode = new StringField(this, 100)

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)

  /**
   *  此筆維修紀時時的機台狀態代碼
   */
  val status = new StringField(this, 10)

  /**
   *  維修紀錄日期
   */
  val insertDate = new StringField(this, 10)

  /**
   *  維修紀錄工班　日期
   */
  val shiftDate = new StringField(this, 10)
}


