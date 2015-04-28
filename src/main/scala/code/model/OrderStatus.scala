package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import java.text.SimpleDateFormat
import java.util.Date

object OrderStatus extends OrderStatus with MongoMetaRecord[OrderStatus] {
  override def collectionName = "orderStatus"
}

class OrderStatus extends MongoRecord[OrderStatus] with ObjectIdPk[OrderStatus] {
  def meta = OrderStatus

  lazy val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  def toDateString(timestamp: Long) = {
    timestamp match {
      case -1 => "尚無資料"
      case _  => dateFormatter.format(new Date(timestamp * 1000L))
    }
  }

  def getWorker(mongoID: String) = {
    Worker.find(mongoID).map(worker => s"[${worker.workerID}] ${worker.name}").getOrElse("查無此人")
  }

  lazy val step1DoneTimeString = toDateString(step1DoneTime.get)
  lazy val step2DoneTimeString = toDateString(step2DoneTime.get)
  lazy val step3DoneTimeString = toDateString(step3DoneTime.get)
  lazy val step4DoneTimeString = toDateString(step4DoneTime.get)
  lazy val step5DoneTimeString = toDateString(step5DoneTime.get)

  lazy val step1StartTimeString = toDateString(step1StartTime.get)
  lazy val step2StartTimeString = toDateString(step2StartTime.get)
  lazy val step3StartTimeString = toDateString(step3StartTime.get)
  lazy val step4StartTimeString = toDateString(step4StartTime.get)
  lazy val step5StartTimeString = toDateString(step5StartTime.get)

  lazy val step1WorkerName = getWorker(step1workerID.get)
  lazy val step2WorkerName = getWorker(step2workerID.get)
  lazy val step3WorkerName = getWorker(step3workerID.get)
  lazy val step4WorkerName = getWorker(step4workerID.get)
  lazy val step5WorkerName = getWorker(step5workerID.get)

  val lastUpdated = new DateField(this)
  val lotNo = new StringField(this, 100)
  val partNo = new StringField(this, 100)
  val product = new StringField(this, 20)
  val inputCount = new LongField(this)
  val shiftDate = new StringField(this, 10)
  val insertDate = new StringField(this, 10)
  val customer = new StringField(this, 100)

  val step1workerID = new StringField(this, 100)
  val step2workerID = new StringField(this, 100)
  val step3workerID = new StringField(this, 100)
  val step4workerID = new StringField(this, 100)
  val step5workerID = new StringField(this, 100)

  val step1machineID = new StringField(this, 10)
  val step2machineID = new StringField(this, 10)
  val step3machineID = new StringField(this, 10)
  val step4machineID = new StringField(this, 10)
  val step5machineID = new StringField(this, 10)

  val step1 = new LongField(this)
  val step2 = new LongField(this)
  val step3 = new LongField(this)
  val step4 = new LongField(this)
  val step5 = new LongField(this)

  val step1DoneTime = new LongField(this, -1)
  val step2DoneTime = new LongField(this, -1)
  val step3DoneTime = new LongField(this, -1)
  val step4DoneTime = new LongField(this, -1)
  val step5DoneTime = new LongField(this, -1)

  val step1StartTime = new LongField(this, -1)
  val step2StartTime = new LongField(this, -1)
  val step3StartTime = new LongField(this, -1)
  val step4StartTime = new LongField(this, -1)
  val step5StartTime = new LongField(this, -1)

  def isStepDone(step: Int) = step match {
    case 1 => step1.get >= inputCount.get
    case 2 => step2.get >= (inputCount.get / 1.04).toLong
    case 3 => step3.get >= (inputCount.get / 1.04).toLong
    case 4 => step4.get >= (inputCount.get / 1.04).toLong
    case 5 => step5.get >= (inputCount.get / 1.04).toLong
  }


}

