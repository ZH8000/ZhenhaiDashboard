package code.model

import code.lib._
import net.liftweb.common._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  此資料表儲存網站上用來顯示「今日工單」的資料表
 */
object ProductionStatus extends ProductionStatus with MongoMetaRecord[ProductionStatus] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "productionStatus"

  /**
   *  機台狀態對應到機台說明
   */
  private val statusToDescription = Map(
    1 -> "生產中",
    2 -> "維修中",
    3 -> "維修完成",
    4 -> "生產完成",
    5 -> "生產中",
    6 -> "生產中",
    7 -> "強制停機（已達目標數）",
    8 -> "強制停機（未達目標數）"
  )

  /**
   *  依照訂單狀態的 Record 取得特定製程中的訂單狀態說明
   *
   *  @param    orderStatusHolder       訂單狀態的 Record
   *  @param    step                    製程（1 = 加締 / 2 = 組立 / 3 = 老化 / 4 = 選別 / 5 = 加工切腳）
   *  @param    status                  該製程的狀態代碼
   *  @return                           狀態說明
   */
  def getStatus(orderStatusHolder: Box[OrderStatus], step: Int, status: Int) = {
    if (status == 7 || status == 8) {
      val isDone = orderStatusHolder.map(record => record.isStepDone(step)).getOrElse(false)

      isDone match {
        case true  => "生產完成"
        case false => "生產中"
      }

    } else {
      statusToDescription.get(status).getOrElse("-")
    }
  }

}

/**
 *  此資料表儲存網站上用來顯示「今日工單」的資料表
 */
class ProductionStatus extends MongoRecord[ProductionStatus] with ObjectIdPk[ProductionStatus] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = ProductionStatus

  /**
   *  料號
   */
  val partNo = new StringField(this, 100)

  /**
   *  工單號
   */
  val lotNo = new StringField(this, 100)

  /**
   *  產品尺吋
   */
  val product = new StringField(this, 100)

  /**
   *  加締機的狀態
   */
  val step1Status = new IntField(this, -1)

  /**
   *  組立機的狀態
   */
  val step2Status = new IntField(this, -1)

  /**
   *  老化機的狀態
   */
  val step3Status = new IntField(this, -1)

  /**
   *  選別機的狀態
   */
  val step4Status = new IntField(this, -1)

  /**
   *  加工切腳機的狀態
   */
  val step5Status = new IntField(this, -1)

  /**
   *  最後更新的日期
   */
  val lastUpdated = new StringField(this, 10)

  /**
   *  最後更新的工班日期
   */
  val lastUpdatedShifted = new StringField(this, 10)

  /**
   *  從料號取出的客戶名稱
   */
  def customer = Customer.fromPartNo(partNo.get)
}

