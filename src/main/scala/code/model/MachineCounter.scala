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

object MachineCounter extends MachineCounter with MongoMetaRecord[MachineCounter] {
  override def collectionName = "machineCounter"

  def getCount(machineID: String): Long = MachineCounter.find("machineID", machineID).map(_.counter.get).getOrElse(0)
  def toHashMap: Map[String, Long] = MachineCounter.findAll.map(x => x.machineID.get -> x.counter.get).toMap
}

class MachineCounter extends MongoRecord[MachineCounter] with ObjectIdPk[MachineCounter] {
  def meta = MachineCounter

  val machineID = new StringField(this, 10)
  val counter = new LongField(this, 0)
}

