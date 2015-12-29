package code.model

import net.liftweb.common._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  「每日晨間報表」中，記錄 Excel 中需由台容填寫的欄位的設定值
 */
object DailySummaryExcelSaved extends DailySummaryExcelSaved with MongoMetaRecord[DailySummaryExcelSaved] {

  /**
   *  此資料表在 MongoDB 中的欄位
   */
  override def collectionName = "dailySummaryExcelSaved"
  
  /**
   *  取得特定日期中，特定電容尺吋的特定欄位名稱的數量
   *
   *  @param    date      工班日期
   *  @param    product   產品尺吋
   *  @param    name      欄位名稱
   *  @return             若有資料的話，則為 Some(數量)，若無的話則為 None
   */
  def get(date: String, product: String, name: String): Option[Long] = {
    val dataRow = this.find(("date" -> date) ~ ("product" -> product) ~ ("name" -> name))
    dataRow.map(_.value.get)
  }

  /**
   *  更新特定日期中，特定電容尺吋的特定欄位名稱的數量
   *
   *  @param    date      工班日期
   *  @param    product   產品尺吋
   *  @param    name      欄位名稱
   *  @return             若儲存成功的話，更新過後的 Record
   */
  def updateValue(date: String, product: String, name: String, value: Long): Box[DailySummaryExcelSaved] = {
    val dataRow = this.find(("date" -> date) ~ ("product" -> product) ~ ("name" -> name))
    val updatedRow = dataRow match {
      case Full(data) => data.value(value)
      case _ => DailySummaryExcelSaved.createRecord.date(date).product(product).name(name).value(value)
    }
    updatedRow.saveTheRecord()
  }

}

/**
 *  「每日晨間報表」中，記錄 Excel 中需由台容填寫的欄位的設定值
 */
class DailySummaryExcelSaved extends MongoRecord[DailySummaryExcelSaved] with ObjectIdPk[DailySummaryExcelSaved] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = DailySummaryExcelSaved

  /**
   *  工班日期
   */
  val date = new StringField(this, 10)

  /**
   *  產品尺吋
   */
  val product = new StringField(this, 10)

  /**
   *  欄位名稱
   */
  val name = new StringField(this, 10)

  /**
   *  良品數量
   */
  val value = new LongField(this)
}

