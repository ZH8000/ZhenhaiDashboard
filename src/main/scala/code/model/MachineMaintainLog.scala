package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import scala.collection.JavaConversions._

object DailyMachineCount extends DailyMachineCount with MongoMetaRecord[DailyMachineCount] {
  override def collectionName = "dailyMachineCount"
}

class DailyMachineCount extends MongoRecord[DailyMachineCount] with ObjectIdPk[DailyMachineCount] {
  def meta = DailyMachineCount

  val machineID = new StringField(this, 10)
  val insertDate = new StringField(this, 10)
  val count_qty = new LongField(this)
  val event_qty = new LongField(this)
  val status = new StringField(this, 10)
}


object MachineMaintainLog extends MachineMaintainLog with MongoMetaRecord[MachineMaintainLog] {
  override def collectionName = "machineMaintainLog"

  def dateList = MachineMaintainLog.useColl(collection => collection.distinct("insertDate")).toList.map(_.toString).sortWith(_ > _)
}

class MachineMaintainLog extends MongoRecord[MachineMaintainLog] with ObjectIdPk[MachineMaintainLog] {
  def meta = MachineMaintainLog

  val workerMongoID = new StringField(this, 24)
  val timestamp = new LongField(this)
  val startTimestamp = new LongField(this)
  val maintenanceCode = new StringField(this, 100)
  val machineID = new StringField(this, 10)
  val status = new StringField(this, 10)
  val insertDate = new StringField(this, 10)
  val shiftDate = new StringField(this, 10)

}


