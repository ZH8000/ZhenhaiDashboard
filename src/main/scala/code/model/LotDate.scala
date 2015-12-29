package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

import scala.collection.JavaConversions._

/**
 *  此資料儲存每個工單號第一次出現的月份
 */
object LotDate extends LotDate with MongoMetaRecord[LotDate] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "lotDate"

  /**
   *  取得所有有工單的月份
   *
   *  @return       有工單的月份的 List，例如 List(2015-11, 2015-12, 2016-01)
   */
  def monthList = LotDate.useColl(collection => collection.distinct("shiftDate")).toList.map(_.toString).sortWith(_ > _)

}

/**
 *  此資料儲存每個工單號第一次出現的月份
 */
class LotDate extends MongoRecord[LotDate] with ObjectIdPk[LotDate] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = LotDate

  /**
   *  工單號
   */
  val lotNo = new StringField(this, 128)

  /**
   *  此工單號第一次出現的實際月份
   */
  val insertDate = new StringField(this, 10)

  /**
   *  此工單號第一次出現的工班月份
   *
   *  例如在 2015-04-01 06:00 第一次刷的工單號，因為就工班日期來說（以謝崗廠來說，
   *  是由當日早上的七點計算到當日晚上的六點九十九分），是屬於 2015-03-31 這天的，
   *  所以其工班月份是 2015-03。
   */
  val shiftDate = new StringField(this, 10)
}

