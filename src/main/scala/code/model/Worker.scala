package code.model

import com.mongodb.casbah.Imports._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object Worker extends Worker with MongoMetaRecord[Worker] {
  override def collectionName = "worker"
  def hasNoDuplicateID(workerID: String) = findByWorkerID(workerID).filterNot(_.isDeleted.get).isEmpty
  def findByWorkerID(workerID: String) = Worker.find(MongoDBObject("workerID" -> workerID, "isDeleted" -> false))
}

class Worker extends MongoRecord[Worker] with ObjectIdPk[Worker] {
  def meta = Worker
  val name = new StringField(this, 10)
  val workerID = new StringField(this, 20)
  val department = new StringField(this, 20)
  val team = new StringField(this, 20)
  val workerType = new StringField(this, 20)
  val isDeleted = new BooleanField(this, false)
}

