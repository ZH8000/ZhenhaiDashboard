package code.model

import com.mongodb.casbah.Imports._

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.sitemap.Loc._
import net.liftweb.http.OutputStreamResponse
import net.liftweb.common._
import net.liftweb.util.Helpers._

/**
 *  記錄員工資料的資料表
 */
object Worker extends Worker with MongoMetaRecord[Worker] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "worker"

  /**
   *  檢查是否已有重複的員工編號
   *
   *  @param    worker      要檢查的員工編號
   *  @param    workerBox   如果是新增使用者需傳入 None，若是更新使用者的員工編號的話則需傳入 Some(原本的 Worker 物件)
   *  @return               如果沒有重複的員工編號則為 true，否則為 false
   */
  def hasNoDuplicateID(workerID: String, workerBox: Option[Worker] = None) = workerBox match {
    case Some(worker) if workerID == worker.workerID.get => false
    case _ => findByWorkerID(workerID).filterNot(_.isDeleted.get).isEmpty
  }

  /**
   *  檢查是否已有重複的員工姓名
   *
   *  @param    worker      要檢查的員工姓名
   *  @param    workerBox   如果是新增使用者需傳入 None，若是更新使用者的員工姓名的話則需傳入 Some(原本的 Worker 物件)
   *  @return               如果沒有重複的員工姓名則為 true，否則為 false
   */
  def hasNoDuplicateName(name: String, workerBox: Option[Worker] = None) = workerBox match {
    case Some(worker) if name == worker.name.get => false
    case _ => findByWorkerName(name).filterNot(_.isDeleted.get).isEmpty
  }

  /**
   *  依照台容的員工編號取得 Worker 物件
   *
   *  @param    workerID      台容的員工編號
   *  @return                 相對應的 Worker 物件
   */
  def findByWorkerID(workerID: String) = Worker.find(MongoDBObject("workerID" -> workerID, "isDeleted" -> false))

  /**
   *  依照員工的姓名取得 Worker 物件
   *
   *  @param    workerID      台容的員工姓名
   *  @return                 相對應的 Worker 物件
   */
  def findByWorkerName(name: String) = Worker.find(MongoDBObject("name" -> name, "isDeleted" -> false))

  /**
   *  依照員工的 MongoDB ID 取得 Worker 物件
   *
   *  @param    workerMongoID   員工在此 MongoDB 資料表中的 ID
   *  @return                   相對應的 Worker 物件
   */
  def findByMongoID(workerMongoID: String) = Worker.find(workerMongoID).filterNot(_.isDeleted.get)

  /**
   *  輸出員工編號條碼的 PDF
   */
  def barcodePDF = new EarlyResponse(() => 
    Full(OutputStreamResponse(WorkerBarcodePDF.createPDF _, -1, List("Content-Type" -> "application/pdf")))
  )
}

/**
 *  記錄員工資料的資料表
 */
class Worker extends MongoRecord[Worker] with ObjectIdPk[Worker] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = Worker

  /**
   *  姓名
   */
  val name = new StringField(this, 10)

  /**
   *  員工編號
   */
  val workerID = new StringField(this, 20)

  /**
   *  部門
   */
  val department = new StringField(this, 20)

  /**
   *  組別
   */
  val team = new StringField(this, 20)

  /**
   *  員工類型（"normal" = 生產人員 / "maintain" = 維修人員）
   */
  val workerType = new StringField(this, 20)

  /**
   *  是否已被刪除
   */
  val isDeleted = new BooleanField(this, false)

  /**
   *  到職日期
   */
  val onBoardDate = new DateField(this)

  /**
   *  依照到職日期計算的年資
   */
  def workingYears: Int = {
    import org.joda.time.Days
    import org.joda.time.DateTime
    val daysBetween = Days.daysBetween(new DateTime(onBoardDate.get), new DateTime(now)).getDays
    daysBetween match {
      case day if day <= 0 => 1
      case day             => (day / 365.0).ceil.toInt
    }
  }

  /**
   *  員工類型的中文名稱
   */
  def workerTypeTitle = workerType.get match {
    case "maintain" => "維修人員"
    case "normal"   => "生產人員"
  }
}


