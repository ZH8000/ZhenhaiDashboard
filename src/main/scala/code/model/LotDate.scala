package code.model

import com.mongodb.casbah.Imports._

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import scala.collection.JavaConversions._


object LotDate extends LotDate with MongoMetaRecord[LotDate] {
  override def collectionName = "lotDate"

  def monthList = LotDate.useColl(collection => collection.distinct("shiftDate")).toList.map(_.toString).sortWith(_ > _)

}

class LotDate extends MongoRecord[LotDate] with ObjectIdPk[LotDate] {
  def meta = LotDate
  val lotNo = new StringField(this, 128)
  val insertDate = new StringField(this, 10)
  val shiftDate = new StringField(this, 10)
}

