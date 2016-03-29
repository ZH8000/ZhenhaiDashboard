package code.model

import net.liftweb.common._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  每日各機台工號料及最後狀態表
 *
 *  此資料表用在網頁上各機台詳細頁中，列出該機台在工班日中所進行的工單和
 *  料號，以及最後的狀態。
 */
object DailyLotNoPartNo extends DailyLotNoPartNo with MongoMetaRecord[DailyLotNoPartNo] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "dailyLotNoPartNo"

  /**
   *  列出特定機台的特定工班日的所有工單
   */
  def findBy(machineID: String, shiftDate: String) = {
    DailyLotNoPartNo.findAll(("machineID" -> machineID) ~ ("shiftDate" -> shiftDate)).sortBy(_.lastUpdated.get)
  }
}

/**
 *  每日各機台工號料及最後狀態表
 *
 *  此資料表用在網頁上各機台詳細頁中，列出該機台在工班日中所進行的工單和
 *  料號，以及最後的狀態。
 */
class DailyLotNoPartNo extends MongoRecord[DailyLotNoPartNo] with ObjectIdPk[DailyLotNoPartNo] {

  /**
   *  此資料表對應到哪 MongoMetaRecord
   */
  def meta = DailyLotNoPartNo

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)

  /**
   *  工單號
   */
  val lotNo = new StringField(this, 10)

  /**
   *  料號
   */
  val partNo = new StringField(this, 10)

  /**
   *  工班日期
   */
  val shiftDate = new StringField(this, 10)

  /**
   *  最後狀態更新時間
   */
  val lastUpdated = new LongField(this)

  /**
   *  最後狀態
   */
  val lastStatus = new StringField(this, 10)
}

