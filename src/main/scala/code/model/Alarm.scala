package code.model

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  記錄維修行事曆的資料表
 */
object Alarm extends Alarm with MongoMetaRecord[Alarm] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "alarm"
}

/**
 *  記錄維修行事曆的資料表
 */
class Alarm extends MongoRecord[Alarm] with ObjectIdPk[Alarm] {

  /**
   *  對應到哪個 MongoMetaRecord 物件
   */
  def meta = Alarm

  /**
   *  哪一個製程
   */
  val step = new StringField(this, 5)

  /**
   *  多少個良品要換一次
   */
  val countdownQty = new LongField(this)

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)

  /**
   *  要更換什麼東西的描述
   */
  val description = new StringField(this, 60)

  /**
   *  是否已經更換
   */
  val isDone = new BooleanField(this, false)

  /**
   *  若已更換，是在什麼時候更換
   */
  val doneTime = new DateField(this)

  /**
   *  若已更換，是由誰更換（打勾者帳號在 MongoDB 中的 ID）
   */
  val doneUser = new StringField(this, 50)

  /**
   *  已經換了幾次
   */
  val replacedCounter = new IntField(this, 0)

  /**
   *  上次更換時的良品數
   */
  val lastReplaceCount = new LongField(this, 0)

  /**
   *  是否已經到了需要更換的情況
   */
  def isUrgentEvent = lastReplaceCount.get + countdownQty.get <= MachineCounter.getCount(machineID.get)
}

