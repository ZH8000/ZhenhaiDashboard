package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object OrderStatus extends OrderStatus with MongoMetaRecord[OrderStatus] {
  override def collectionName = "orderStatus"
}

class OrderStatus extends MongoRecord[OrderStatus] with ObjectIdPk[OrderStatus] {
  def meta = OrderStatus

  val lastUpdated = new DateField(this)
  val lotNo = new StringField(this, 100)
  val partNo = new StringField(this, 100)
  val product = new StringField(this, 20)
  val customer = new StringField(this, 20)
  val inputCount = new LongField(this)

  val step1WorkerID = new StringField(this, 100)
  val step2WorkerID = new StringField(this, 100)
  val step3WorkerID = new StringField(this, 100)
  val step4WorkerID = new StringField(this, 100)
  val step5WorkerID = new StringField(this, 100)

  val step1MachineID = new StringField(this, 10)
  val step2MachineID = new StringField(this, 10)
  val step3MachineID = new StringField(this, 10)
  val step4MachineID = new StringField(this, 10)
  val step5MachineID = new StringField(this, 10)

  val step1 = new LongField(this)
  val step2 = new LongField(this)
  val step3 = new LongField(this)
  val step4 = new LongField(this)
  val step5 = new LongField(this)

  val step1DoneTime = new LongField(this)
  val step2DoneTime = new LongField(this)
  val step3DoneTime = new LongField(this)
  val step4DoneTime = new LongField(this)
  val step5DoneTime = new LongField(this)

  val step1StartTime = new LongField(this)
  val step2StartTime = new LongField(this)
  val step3StartTime = new LongField(this)
  val step4StartTime = new LongField(this)
  val step5StartTime = new LongField(this)

}

