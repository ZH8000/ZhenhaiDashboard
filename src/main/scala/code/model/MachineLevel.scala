package code.model

import code.lib._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

/**
 *  記錄機台生產均線的資料表
 */
object MachineLevel extends MachineLevel with MongoMetaRecord[MachineLevel] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "machineLevel"
}

/**
 *  記錄機台生產均線的資料表
 */
class MachineLevel extends MongoRecord[MachineLevel] with ObjectIdPk[MachineLevel] {

  /**
   *  此資料表對應到的 MongoMetaRecord
   */ 
  def meta = MachineLevel

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)

  /**
   *  LevelA 的良品數（最高）下限
   */
  val levelA = new LongField(this)

  /**
   *  LevelB 的良品數（中）下限
   */
  val levelB = new LongField(this)

  /**
   *  LevelC 的良品數（低）下限
   */
  val levelC = new LongField(this)

  /**
   *  計算某個良品數在此機台上屬於哪個效率等級
   *
   *  若小於 levelC 的設定值為 LevelD
   *
   *  @param    count     良品數
   *  @return             A / B / C / D 中的其中一個等級
   */
  def level(count: Long) = {
    if (count > levelA.get) { "A" }
    else if (count > levelB.get) { "B" }
    else if (count > levelC.get) { "C" }
    else { "D" }
  }

  /**
   *  計算某個良品數在此機台上屬於哪個效率等級
   *
   *  若小於 levelC 的設定值為 LevelD
   *
   *  @param    count     目前良品數
   *  @return             {{{Some(還要做多少, 目前達成的 % 數)}}}，或如果本來就是 LevelA，那就是 {{{None}}}
   */
  def nextLevel(count: Long) = {

    // 從目前這個等級到下個等級中間的良品數差距，
    // 以前以 count 這個良品數，還要做多少個才能
    // 到下個良品數
    val diff = level(count) match {
      case "A" => None
      case "B" => Some((levelA.get - levelB.get, (levelA.get - count)))
      case "C" => Some((levelB.get - levelC.get, (levelB.get - count)))
      case _   => Some((levelC.get, (levelC.get - count)))
    }

    diff.map { case (totalTarget, targetToNextLevel) =>
      val scale = Scale(0, totalTarget, 0, 100)
      val percent = count match {
        case 0 => 0
        case _ => scale(targetToNextLevel)
      }

      (targetToNextLevel, percent)
    }
  }
}
