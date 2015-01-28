package code.model

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object WorkerDaily extends WorkerDaily with MongoMetaRecord[WorkerDaily] {
  override def collectionName = "workerDaily"
}

class WorkerDaily extends MongoRecord[WorkerDaily] with ObjectIdPk[WorkerDaily] {
  def meta = WorkerDaily

  val workerMongoID = new StringField(this, 24)
  val countQty = new LongField(this)
  val timestamp = new StringField(this, 10)
  val shiftDate = new StringField(this, 10)
  val machineID = new StringField(this, 10)
}


