package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object MachineLevel extends MachineLevel with MongoMetaRecord[MachineLevel] {
  override def collectionName = "machineLevel"
}

class MachineLevel extends MongoRecord[MachineLevel] with ObjectIdPk[MachineLevel] {
  def meta = MachineLevel

  val machineID = new StringField(this, 10)
  val levelA = new LongField(this)
  val levelB = new LongField(this)
  val levelC = new LongField(this)

  def level(count: Long) = {
    if (count > levelA.get) { "A" }
    else if (count > levelB.get) { "B" }
    else if (count > levelC.get) { "C" }
    else { "D" }
  }

  def nextLevel(count: Long) = {

    val diff = level(count) match {
      case "A" => None
      case "B" => Some((levelA.get - levelB.get, (levelA.get - count)))
      case "C" => Some((levelB.get - levelC.get, (levelB.get - count)))
      case _   => Some((levelC.get, (levelC.get - count)))
    }

    diff.map { case (totalTarget, targetToNextLevel) =>
      val scale = Scale(0, totalTarget, 0, 100)
      val percent = scale(targetToNextLevel)
      (targetToNextLevel, percent)
    }
  }
}
