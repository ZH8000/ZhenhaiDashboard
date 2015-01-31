package code.model

import com.mongodb.casbah.Imports._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object StrangeQty extends StrangeQty with MongoMetaRecord[StrangeQty] {
  override def collectionName = "strangeQty"
}

class StrangeQty extends MongoRecord[StrangeQty] with ObjectIdPk[StrangeQty] {
  def meta = StrangeQty

  val count_qty = new LongField(this)
  val emb_date = new LongField(this)
  val event_qty = new LongField(this)
  val defact_id = new IntField(this)
  val mach_id = new StringField(this, 15)
}

