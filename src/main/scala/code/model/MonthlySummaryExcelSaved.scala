package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

import net.liftweb.json.JsonDSL._
import net.liftweb.common._

/**
 *  用來儲存「產量統計」－＞「重點統計」中網頁下面輸入表格的資料
 */
object MonthlySummaryExcelSaved extends MonthlySummaryExcelSaved with MongoMetaRecord[MonthlySummaryExcelSaved] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "monthlySummaryExcelSaved"

  /**
   *  取得 date 此日期中在 step 製程中，product 此 φ 別的數量
   *
   *  @param    date        日期
   *  @param    step        製程
   *  @param    product     產品 φ 別的直徑
   *  @return               設定的值
   */
  def get(date: String, step: Int, product: String): Option[Long] = {
    val dataRow = this.find(("date" -> date) ~ ("step" -> step) ~ ("product" -> product))
    dataRow.map(_.value.get)
  }

  /**
   *  更新 date 此日期中在 step 製程中，product 此 φ 別的數量
   *
   *  @param    date        日期
   *  @param    step        製程
   *  @param    product     產品 φ 別的直徑
   *  @return               新的設定的值
   */
  def updateValue(date: String, step: Int, product: String, value: Long): Box[MonthlySummaryExcelSaved] = {
    val dataRow = this.find(("date" -> date) ~ ("step" -> step) ~ ("product" -> product))
    val updatedRow = dataRow match {
      case Full(data) => data.value(value)
      case _ => MonthlySummaryExcelSaved.createRecord.date(date).step(step).product(product).value(value)
    }
    updatedRow.saveTheRecord()
  }

}

/**
 *  用來儲存「產量統計」－＞「重點統計」中網頁下面輸入表格的資料
 */
class MonthlySummaryExcelSaved extends MongoRecord[MonthlySummaryExcelSaved] with ObjectIdPk[MonthlySummaryExcelSaved] {

  /**
   *  此資料表對應到 MongoMetaRecord
   */
  def meta = MonthlySummaryExcelSaved

  /**
   *  日期
   */
  val date = new StringField(this, 10)

  /**
   *  製程
   */
  val step = new IntField(this)

  /**
   *  產品尺吋
   */
  val product = new StringField(this, 10)

  /**
   *  設定值
   */
  val value = new LongField(this)
}
