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
  val customer = new StringField(this, 100)
  val lotNo = new StringField(this, 100)
  val product = new StringField(this, 20)
  val inputCount = new LongField(this)
  val step1 = new LongField(this)
  val step2 = new LongField(this)
  val step3 = new LongField(this)
  val step4 = new LongField(this)
  val step5 = new LongField(this)
}

