package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.common._
import net.liftweb.json.JsonDSL._

import scala.collection.JavaConversions._

object DefactByLotNo extends DefactByLotNo with MongoMetaRecord[DefactByLotNo] {
  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "defactByLotNo"

  def getCount(lotNo: String, machineID: String): Box[Long] = {
    this.find((("lotNo" -> lotNo) ~ ("mach_id" -> machineID))).map(_.event_qty.get.toLong)
  }
}

class DefactByLotNo extends MongoRecord[DefactByLotNo] with ObjectIdPk[DefactByLotNo] {
  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = DefactByLotNo

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

