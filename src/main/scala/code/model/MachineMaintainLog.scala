package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object MachineMaintainLog extends MachineMaintainLog with MongoMetaRecord[MachineMaintainLog] {
  override def collectionName = "machineMaintainLog"
}

class MachineMaintainLog extends MongoRecord[MachineMaintainLog] with ObjectIdPk[MachineMaintainLog] {
  def meta = MachineMaintainLog

  val workerMongoID = new StringField(this, 24)
  val timestamp = new LongField(this)
  val maintenanceCode = new StringField(this, 100)
  val machineID = new StringField(this, 10)
  val status = new StringField(this, 10)
}


