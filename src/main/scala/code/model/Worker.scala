package code.model

import com.mongodb.casbah.Imports._

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.sitemap.Loc._
import net.liftweb.http.OutputStreamResponse
import net.liftweb.common._
import net.liftweb.util.Helpers._


object Worker extends Worker with MongoMetaRecord[Worker] {
  override def collectionName = "worker"

  def hasNoDuplicateID(workerID: String, workerBox: Option[Worker] = None) = workerBox match {
    case Some(worker) if workerID == worker.workerID.get => false
    case _ => findByWorkerID(workerID).filterNot(_.isDeleted.get).isEmpty
  }

  def hasNoDuplicateName(name: String, workerBox: Option[Worker] = None) = workerBox match {
    case Some(worker) if name == worker.name.get => false
    case _ => findByWorkerName(name).filterNot(_.isDeleted.get).isEmpty
  }

  def findByWorkerID(workerID: String) = Worker.find(MongoDBObject("workerID" -> workerID, "isDeleted" -> false))
  def findByWorkerName(name: String) = Worker.find(MongoDBObject("name" -> name, "isDeleted" -> false))
  def findByMongoID(mongoID: String) = Worker.find(mongoID).filterNot(_.isDeleted.get)

  def barcodePDF = new EarlyResponse(() => 
    Full(OutputStreamResponse(WorkerBarcodePDF.createPDF _, -1, List("Content-Type" -> "application/pdf")))
  )
}

class Worker extends MongoRecord[Worker] with ObjectIdPk[Worker] {
  def meta = Worker
  val name = new StringField(this, 10)
  val workerID = new StringField(this, 20)
  val department = new StringField(this, 20)
  val team = new StringField(this, 20)
  val workerType = new StringField(this, 20)
  val isDeleted = new BooleanField(this, false)
  val onBoardDate = new DateField(this)

  def workingYears: Int = {
    import org.joda.time.Days
    import org.joda.time.DateTime
    val daysBetween = Days.daysBetween(new DateTime(onBoardDate.get), new DateTime(now)).getDays
    daysBetween match {
      case day if day <= 0 => 1
      case day             => (day / 365.0).ceil.toInt
    }
  }

  def workerTypeTitle = workerType.get match {
    case "maintain" => "維修人員"
    case "normal" => "生產人員"
  }
}


