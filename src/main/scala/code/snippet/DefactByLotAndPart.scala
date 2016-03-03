package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

import scala.collection.JavaConversions._

object DefactByLotAndPart extends DefactByLotAndPart with MongoMetaRecord[DefactByLotAndPart] {
  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "defactByLotAndPart"
}

class DefactByLotAndPart extends MongoRecord[DefactByLotAndPart] with ObjectIdPk[DefactByLotAndPart] {
  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = DefactByLotAndPart

  /**
   *  工單號
   */
  val lotNo = new StringField(this, 128)

  /**
   *  機台編號
   */
  val mach_id = new StringField(this, 10)

  /**
   *  事件數量
   */
  val event_qty = new LongField(this, 10)

}

