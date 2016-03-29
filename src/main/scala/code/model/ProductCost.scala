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


