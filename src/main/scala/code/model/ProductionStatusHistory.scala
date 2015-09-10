package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.common._

object ProductionStatusHistory extends ProductionStatusHistory with MongoMetaRecord[ProductionStatusHistory] {
  override def collectionName = "productionStatusHistory"

  private val statusToDescription = Map(
    1 -> "生產中",
    2 -> "維修中",
    3 -> "維修完成",
    4 -> "生產完成",
    5 -> "生產中",
    6 -> "生產中",
    7 -> "強制停機（已達目標數）",
    8 -> "強制停機（未達目標數）"
  )

  def getStatus(orderStatusHolder: Box[OrderStatus], step: Int, status: Int) = {
    if (status == 7 || status == 8) {
      val isDone = orderStatusHolder.map(status => status.isStepDone(step)).getOrElse(false)

      isDone match {
        case true  => "生產完成"
        case false => "生產中"
      }

    } else {
      statusToDescription.get(status).getOrElse("-")
    }
  }

}

class ProductionStatusHistory extends MongoRecord[ProductionStatusHistory] with ObjectIdPk[ProductionStatusHistory] {
  def meta = ProductionStatusHistory

  val partNo = new StringField(this, 100)
  val lotNo = new StringField(this, 100)
  val product = new StringField(this, 100)
  val status = new IntField(this, -1)
  val step1Status = new IntField(this, -1)
  val step2Status = new IntField(this, -1)
  val step3Status = new IntField(this, -1)
  val step4Status = new IntField(this, -1)
  val step5Status = new IntField(this, -1)
  val shiftDate = new StringField(this, 10)

  def customer = Customer.fromPartNo(partNo.get)
}

