package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object DailyOrder extends DailyOrder with MongoMetaRecord[DailyOrder] {
  override def collectionName = "dailyOrder"
}

class DailyOrder extends MongoRecord[DailyOrder] with ObjectIdPk[DailyOrder] {
  def meta = DailyOrder

  val timestamp = new StringField(this, 10)
  val shiftDate = new StringField(this, 10)
  val partNo = new StringField(this, 100)
  val lotNo = new StringField(this, 100)
  val product = new StringField(this, 100)
  val status = new IntField(this, -1)
  val step1Status = new IntField(this, -1)
  val step2Status = new IntField(this, -1)
  val step3Status = new IntField(this, -1)
  val step4Status = new IntField(this, -1)
  val step5Status = new IntField(this, -1)

  def customer = Customer.fromPartNo(partNo.get)

}

