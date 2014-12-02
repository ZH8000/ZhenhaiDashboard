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

import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat

object Alarm extends Alarm with MongoMetaRecord[Alarm] {
  override def collectionName = "alarm"
}

class Alarm extends MongoRecord[Alarm] with ObjectIdPk[Alarm] {
  def meta = Alarm

  val name = new StringField(this, 10)
  val workerID = new StringField(this, 20)
  val workerMongoID = new StringField(this, 32)
  val startDate = new DateField(this)
  val countdownDays = new IntField(this)
  val machineID = new StringField(this, 10)
  val description = new StringField(this, 60)

  val isDone = new BooleanField(this, false)
  val doneTime = new DateField(this)
  val doneWorkerID = new StringField(this, 20)
  val doneWorkerMongoID = new StringField(this, 32)

  def dueDate: Date = {
    val calendar = Calendar.getInstance
    calendar.setTime(startDate.get)
    calendar.add(Calendar.DAY_OF_MONTH, countdownDays.get)
    calendar.getTime
  }

  def dueDateString = new SimpleDateFormat("yyyy-MM-dd").format(dueDate)
  def isUrgentEvent = {
    val marginDate = Calendar.getInstance
    marginDate.add(Calendar.DAY_OF_MONTH, 7)
    dueDate.before(marginDate.getTime)
  }

}

