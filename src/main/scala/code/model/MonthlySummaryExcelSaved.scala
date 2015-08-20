package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

import net.liftweb.json.JsonDSL._
import net.liftweb.common._

object MonthlySummaryExcelSaved extends MonthlySummaryExcelSaved with MongoMetaRecord[MonthlySummaryExcelSaved] {


  override def collectionName = "monthlySummaryExcelSaved"

  def get(date: String, step: Int, product: String): Option[Long] = {
    val dataRow = this.find(("date" -> date) ~ ("step" -> step) ~ ("product" -> product))
    dataRow.map(_.value.get)
  }

  def updateValue(date: String, step: Int, product: String, value: Long): Box[MonthlySummaryExcelSaved] = {
    val dataRow = this.find(("date" -> date) ~ ("step" -> step) ~ ("product" -> product))
    val updatedRow = dataRow match {
      case Full(data) => data.value(value)
      case _ => MonthlySummaryExcelSaved.createRecord.date(date).step(step).product(product).value(value)
    }
    updatedRow.saveTheRecord()
  }

}

class MonthlySummaryExcelSaved extends MongoRecord[MonthlySummaryExcelSaved] with ObjectIdPk[MonthlySummaryExcelSaved] {
  def meta = MonthlySummaryExcelSaved

  val date = new StringField(this, 10)
  val step = new IntField(this)
  val product = new StringField(this, 10)
  val value = new LongField(this)
}

object KadouExcelSaved extends KadouExcelSaved with MongoMetaRecord[KadouExcelSaved] {
  override def collectionName = "kadouExcelSaved"

  def updateValue(date: String, step: Int, value: Long): Box[KadouExcelSaved] = {
    val dataRow = this.find(("date" -> date) ~ ("step" -> step))
    val updatedRow = dataRow match {
      case Full(data) => data.value(value)
      case _ => KadouExcelSaved.createRecord.date(date).step(step).value(value)
    }
    updatedRow.saveTheRecord()
  }

  def get(date: String, step: Int): Option[Long] = {
    val dataRow = this.find(("date" -> date) ~ ("step" -> step))
    dataRow.map(_.value.get)
  }

}

class KadouExcelSaved extends MongoRecord[KadouExcelSaved] with ObjectIdPk[KadouExcelSaved] {
  def meta = KadouExcelSaved

  val date  = new StringField(this, 10)
  val step  = new LongField(this)
  val value = new LongField(this)

}
