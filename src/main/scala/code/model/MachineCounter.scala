package code.model

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  儲存每台機台的累計良品數的資料表
 */
object MachineCounter extends MachineCounter with MongoMetaRecord[MachineCounter] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "machineCounter"

  /**
   *  取得某台機台的良品數
   *
   *  @param    machineID     機台編號
   *  @return                 該機台的累計良品數
   */
  def getCount(machineID: String): Long = MachineCounter.find("machineID", machineID).map(_.counter.get).getOrElse(0)

  /**
   *  將此資料表轉為 {{{Map[機台編號, 累計良品數]}}}
   */
  def toHashMap: Map[String, Long] = MachineCounter.findAll.map(x => x.machineID.get -> x.counter.get).toMap
}

/**
 *  儲存每台機台的累計良品數的資料表
 */
class MachineCounter extends MongoRecord[MachineCounter] with ObjectIdPk[MachineCounter] {

  /**
   *  此資料表對應到哪個 MongoMetaRecord
   */
  def meta = MachineCounter

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)

  /**
   *  累計良品數
   */
  val counter = new LongField(this, 0)
}

