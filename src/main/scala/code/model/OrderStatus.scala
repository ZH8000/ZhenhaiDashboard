package code.model

import java.text.SimpleDateFormat
import java.util.Date

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import scala.util.Try

/**
 *  網狀上計算「訂單狀單」和「今日工單」用的資料表
 */
object OrderStatus extends OrderStatus with MongoMetaRecord[OrderStatus] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "orderStatus"
}

/**
 *  網狀上計算「訂單狀單」和「今日工單」用的資料表
 */
class OrderStatus extends MongoRecord[OrderStatus] with ObjectIdPk[OrderStatus] {

  /**
   *  此資表相對應到的 MongoMetaRecord
   */
  def meta = OrderStatus

  lazy val dateFormatter = new SimpleDateFormat("yyyy-MM-dd")

  /**
   *  將時間戳記轉成 yyyy-MM-dd 的日期格式
   *
   *  @param    timestamp     UNIX  時間戳記
   *  @return                 轉換過格式後的日期
   */
  def toDateString(timestamp: Long) = {
    timestamp match {
      case -1 => "尚無資料"
      case _  => dateFormatter.format(new Date(timestamp * 1000L))
    }
  }

  /**
   *  取得員工的台容員工編號與姓名
   *
   *  @param    workerMongoID         員工在 MongoDB 中的 ID
   *  @return                         工號與姓名
   */
  def getWorker(workerMongoID: String) = {
    Worker.find(workerMongoID).map(worker => s"[${worker.workerID}] ${worker.name}").getOrElse("查無此人")
  }

  /**
   *  加締的完成時間
   */
  lazy val step1DoneTimeString = toDateString(step1DoneTime.get)

  /**
   *  組立的完成時間
   */
  lazy val step2DoneTimeString = toDateString(step2DoneTime.get)

  /**
   *  老化的完成時間
   */
  lazy val step3DoneTimeString = toDateString(step3DoneTime.get)

  /**
   *  選別的完成時間
   */
  lazy val step4DoneTimeString = toDateString(step4DoneTime.get)

  /**
   *  加工切腳的完成時間
   */
  lazy val step5DoneTimeString = toDateString(step5DoneTime.get)

  /**
   *  加締的開始時間
   */
  lazy val step1StartTimeString = toDateString(step1StartTime.get)

  /**
   *  組立的開始時間
   */
  lazy val step2StartTimeString = toDateString(step2StartTime.get)

  /**
   *  老化的開始時間
   */
  lazy val step3StartTimeString = toDateString(step3StartTime.get)

  /**
   *  選別的開始時間
   */
  lazy val step4StartTimeString = toDateString(step4StartTime.get)

  /**
   *  加工切腳的開始時間
   */
  lazy val step5StartTimeString = toDateString(step5StartTime.get)

  /**
   *  進行加締的員工編號與姓名
   */
  lazy val step1WorkerName = getWorker(step1workerID.get)

  /**
   *  進行組立的員工編號與姓名
   */
  lazy val step2WorkerName = getWorker(step2workerID.get)

  /**
   *  進行老化的員工編號與姓名
   */
  lazy val step3WorkerName = getWorker(step3workerID.get)

  /**
   *  進行選別的員工編號與姓名
   */
  lazy val step4WorkerName = getWorker(step4workerID.get)

  /**
   *  進行加工切腳的員工編號與姓名
   */
  lazy val step5WorkerName = getWorker(step5workerID.get)

  /**
   *  最後一筆記錄的時間戳記
   */
  val lastUpdated = new DateField(this)

  /**
   *  工單號
   */
  val lotNo = new StringField(this, 100)

  /**
   *  料號
   */
  val partNo = new StringField(this, 100)

  /**
   *  產品尺吋代碼
   */
  def productCode = Try{partNo.get.substring(10, 15)}.getOrElse("Unknown")

  /**
   *  產品尺吋代碼
   */
  def productTitle = ProductCost.getProductTitle(productCode)

  /**
   *  投入量（比真正的需求量多 4%）
   */
  val inputCount = new LongField(this)

  /**
   *  工班日期
   */
  val shiftDate = new StringField(this, 10)

  /**
   *  原本的日期
   */
  val insertDate = new StringField(this, 10)

  /**
   *  客戶代碼
   */
  val customer = new StringField(this, 100)

  /**
   *  進行加締的員工在 MongoDB 中的 ID
   */
  val step1workerID = new StringField(this, 100)

  /**
   *  進行組立的員工在 MongoDB 中的 ID
   */
  val step2workerID = new StringField(this, 100)

  /**
   *  進行老化的員工在 MongoDB 中的 ID
   */
  val step3workerID = new StringField(this, 100)

  /**
   *  進行選別的員工在 MongoDB 中的 ID
   */
  val step4workerID = new StringField(this, 100)

  /**
   *  進行加工切腳的員工在 MongoDB 中的 ID
   */
  val step5workerID = new StringField(this, 100)

  /**
   *  進行加締的機台編號
   */
  val step1machineID = new StringField(this, 10)

  /**
   *  進行組立的機台編號
   */
  val step2machineID = new StringField(this, 10)

  /**
   *  進行老化的機台編號
   */
  val step3machineID = new StringField(this, 10)

  /**
   *  進行選別的機台編號
   */
  val step4machineID = new StringField(this, 10)

  /**
   *  進行加工切腳的機台編號
   */
  val step5machineID = new StringField(this, 10)

  /**
   *  加締的良品數
   */
  val step1 = new LongField(this)

  /**
   *  組立的良品數
   */
  val step2 = new LongField(this)

  /**
   *  老化的良品數
   */
  val step3 = new LongField(this)

  /**
   *  選別的良品數
   */
  val step4 = new LongField(this)

  /**
   *  加工切腳的良品數
   */
  val step5 = new LongField(this)

  /**
   *  加締的結束時間的時間戳記
   */
  val step1DoneTime = new LongField(this, -1)

  /**
   *  組立的結束時間的時間戳記
   */
  val step2DoneTime = new LongField(this, -1)

  /**
   *  老化的結束時間的時間戳記
   */
  val step3DoneTime = new LongField(this, -1)

  /**
   *  選別的結束時間的時間戳記
   */
  val step4DoneTime = new LongField(this, -1)

  /**
   *  加工切腳的結束時間的時間戳記
   */
  val step5DoneTime = new LongField(this, -1)

  /**
   *  加締的開始時間的時間戳記
   */
  val step1StartTime = new LongField(this, -1)

  /**
   *  組立的開始時間的時間戳記
   */
  val step2StartTime = new LongField(this, -1)

  /**
   *  老化的開始時間的時間戳記
   */
  val step3StartTime = new LongField(this, -1)

  /**
   *  選別的開始時間的時間戳記
   */
  val step4StartTime = new LongField(this, -1)

  /**
   *  加工切腳的開始時間的時間戳記
   */
  val step5StartTime = new LongField(this, -1)

  /**
   *  某個製程是否完成
   *
   *  @param      step        製程
   *  @return                 是否完成
   */
  def isStepDone(step: Int) = step match {
    case 1 => step1.get >= inputCount.get
    case 2 => step2.get >= (inputCount.get / 1.03).toLong
    case 3 => step3.get >= (inputCount.get / 1.03).toLong
    case 4 => step4.get >= (inputCount.get / 1.03).toLong
    case 5 => step5.get >= (inputCount.get / 1.03).toLong
  }
}

