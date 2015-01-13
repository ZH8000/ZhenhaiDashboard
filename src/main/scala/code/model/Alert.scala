package code.model

import com.mongodb.casbah.Imports._

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object Alert extends Alert with MongoMetaRecord[Alert] {
  override def collectionName = "alert"
}

class Alert extends MongoRecord[Alert] with ObjectIdPk[Alert] {
  def meta = Alert

  val timestamp = new StringField(this, 20)
  val defact_id = new IntField(this)
  val mach_id = new StringField(this, 15)
}

