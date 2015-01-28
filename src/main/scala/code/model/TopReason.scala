package code.model

import com.mongodb.casbah.Imports._

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._


object TopReason extends TopReason with MongoMetaRecord[TopReason] {
  override def collectionName = "topReason"
}

class TopReason extends MongoRecord[TopReason] with ObjectIdPk[TopReason] {
  def meta = TopReason

  val date = new StringField(this, 10)
  val shiftDate = new StringField(this, 10)
  val defact_id = new IntField(this)
  val mach_id = new StringField(this, 15)
  val mach_model = new StringField(this, 20)
  val bad_qty = new LongField(this)

}

