package code.model

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  記錄
 */
object Alarm extends Alarm with MongoMetaRecord[Alarm] {
  override def collectionName = "alarm"
}

class Alarm extends MongoRecord[Alarm] with ObjectIdPk[Alarm] {
  def meta = Alarm

  val step = new StringField(this, 5)
  val countdownQty = new LongField(this)
  val machineID = new StringField(this, 10)
  val description = new StringField(this, 60)

  val isDone = new BooleanField(this, false)
  val doneTime = new DateField(this)
  val doneUser = new StringField(this, 50)
  val replacedCounter = new IntField(this, 0)
  val lastReplaceCount = new LongField(this, 0)

  def isUrgentEvent = lastReplaceCount.get + countdownQty.get <= MachineCounter.getCount(machineID.get)
}

