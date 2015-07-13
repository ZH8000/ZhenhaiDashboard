package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

import net.liftweb.json.JsonDSL._
import net.liftweb.common._

object DailySummaryExcelSaved extends DailySummaryExcelSaved with MongoMetaRecord[DailySummaryExcelSaved] {


  override def collectionName = "dailySummaryExcelSaved"
  
  def get(date: String, product: String, name: String): Option[Long] = {
    val dataRow = this.find(("date" -> date) ~ ("product" -> product) ~ ("name" -> name))
    dataRow.map(_.value.get)
  }

  def updateValue(date: String, product: String, name: String, value: Long): Box[DailySummaryExcelSaved] = {
    val dataRow = this.find(("date" -> date) ~ ("product" -> product) ~ ("name" -> name))
    val updatedRow = dataRow match {
      case Full(data) => data.value(value)
      case _ => DailySummaryExcelSaved.createRecord.date(date).product(product).name(name).value(value)
    }
    updatedRow.saveTheRecord()
  }

}

class DailySummaryExcelSaved extends MongoRecord[DailySummaryExcelSaved] with ObjectIdPk[DailySummaryExcelSaved] {
  def meta = DailySummaryExcelSaved

  val date = new StringField(this, 10)
  val product = new StringField(this, 10)
  val name = new StringField(this, 10)
  val value = new LongField(this)
}

