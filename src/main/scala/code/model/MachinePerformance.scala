package code.model

import net.liftweb.common._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

object ProductCost extends ProductCost with MongoMetaRecord[ProductCost] {
  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "productCost"

  /**
   *  刪除此資料表中的 productCode 產品尺吋的損耗金額設定
   *
   *  @param    productCode   要刪除的產品尺吋
   */
  def delete(productCode: String) = {
    this.find(("productCode" -> productCode)).map(_.delete_!)
  }

  def getProductTitle(productCode: String) = {
    this.find(("productCode" -> productCode)).map(_.productTitle.get).getOrElse("Unknown")
  }

  def getProductCost(productCode: String): Box[BigDecimal] = {
    this.find(("productCode" -> productCode)).map(_.productCost.get)
  }

}

class ProductCost extends MongoRecord[ProductCost] with ObjectIdPk[ProductCost] {

  /**
   *  此資料表對應到哪 MongoMetaRecord
   */
  def meta = ProductCost

  /**
   *  產品尺吋
   */
  val productCode = new StringField(this, 10)

  /**
   *  產品名稱
   */
  val productTitle = new StringField(this, 10)

  /**
   *  金額
   */
  val productCost = new DecimalField(this, 0)
}



/**
 *  機台產能設定資料表
 *
 *  此資料表用在網頁上「產量統計」－＞「員工效率」該頁當中，下方用來設定
 *  各機台中各尺吋產品的產能設定輸入表。
 */
object MachinePerformance extends MachinePerformance with MongoMetaRecord[MachinePerformance] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "machinePerformance"

  /**
   *  刪除此資料表中的 machineID 機台的 productCode 產品尺吋的設定
   *
   *  @param    machineID     要刪除的機台編號
   *  @param    productCode   要刪除的產品尺吋
   */
  def delete(machineID: String, productCode: String) = {
    this.find(("machineID" -> machineID) ~ ("productCode" -> productCode)).map(_.delete_!)
  }

  /**
   *  取得 machineID 此台機台上的 productCode 此產品尺吋的產能設定
   *
   *  @param    machineID     機台編號
   *  @param    productCode   產品尺吋
   *  @return                 產能設定
   */
  def find(machineID: String, productCode: String): Box[MachinePerformance] = {
    this.find(("machineID" -> machineID) ~ ("productCode" -> productCode))
  }

  /**
   *  更新 machineID 此台機台上的 productCode 此產品尺吋的產能設定
   *
   *  @param    machineID           機台編號
   *  @param    productCode         產品尺吋
   *  @param    managementCount     日管理標準量
   *  @param    performanceCount    日效率標準量
   *  @return                       新的產能設定
   */
  def update(machineID: String, productCode: String, managementCount: Long, performanceCount: Long) = {
    find(machineID, productCode) match {
      case Full(record) => 
        record.managementCount(managementCount)
              .performanceCount(performanceCount)

      case _ =>

        MachinePerformance.machineID(machineID)
                          .productCode(productCode)
                          .managementCount(managementCount)
                          .performanceCount(performanceCount)
    }

  }
}

/**
 *  機台產能設定資料表
 *
 *  此資料表用在網頁上「產量統計」－＞「員工效率」該頁當中，下方用來設定
 *  各機台中各尺吋產品的產能設定輸入表。
 */
class MachinePerformance extends MongoRecord[MachinePerformance] with ObjectIdPk[MachinePerformance] {

  /**
   *  此資料表對應到哪 MongoMetaRecord
   */
  def meta = MachinePerformance

  /**
   *  機台編號
   */
  val machineID = new StringField(this, 10)

  /**
   *  產品尺吋
   */
  val productCode = new StringField(this, 10)

  /**
   *  日管理標準量
   */
  val managementCount = new LongField(this)

  /**
   *  日效率標準量
   */
  val performanceCount = new LongField(this)
}

