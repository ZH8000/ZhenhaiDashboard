package code.model

import net.liftweb.common._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  記錄「產量統計」－＞「稼動率」下方設定表格的資料
 */
object KadouExcelSaved extends KadouExcelSaved with MongoMetaRecord[KadouExcelSaved] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "kadouExcelSaved"

  /**
   *  更新設定值
   *
   *  @param    date      日期
   *  @param    step      製程
   *  @param    value     新的設定的數值
   *  @return             更新過後的 Record 物件
   */
  def updateValue(date: String, step: Int, value: Long): Box[KadouExcelSaved] = {
    val dataRow = this.find(("date" -> date) ~ ("step" -> step))
    val updatedRow = dataRow match {
      case Full(data) => data.value(value)
      case _ => KadouExcelSaved.createRecord.date(date).step(step).value(value)
    }
    updatedRow.saveTheRecord()
  }

  /**
   *  取得設定值
   *
   *  @param      date      日期
   *  @param      step      製程
   *  @return               該日期中該製程設定的數量
   */
  def get(date: String, step: Int): Option[Long] = {
    val dataRow = this.find(("date" -> date) ~ ("step" -> step))
    dataRow.map(_.value.get)
  }

}

/**
 *  記錄「產量統計」－＞「稼動率」下方設定表格的資料
 */
class KadouExcelSaved extends MongoRecord[KadouExcelSaved] with ObjectIdPk[KadouExcelSaved] {

  /**
   *  此資料表對應到的 MongoMetaRecord
   */
  def meta = KadouExcelSaved

  /**
   *  日期
   */
  val date  = new StringField(this, 10)

  /**
   *  製程
   */
  val step  = new LongField(this)

  /**
   *  設定的數值
   */
  val value = new LongField(this)
}
