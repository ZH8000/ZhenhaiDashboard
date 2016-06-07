package code.model

import net.liftweb.common._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.common._
import net.liftweb.json.JsonDSL._
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._


object CustomerCode extends CustomerCode with MongoMetaRecord[CustomerCode] {

  /**
   *  此資料表在 MongoDB 中的名稱
   */
  override def collectionName = "customerCode"

  /**
   *  刪除此資料表中的客戶代碼
   *
   *  @param    customerCode   要刪除的客戶代碼
   */
  def delete(customerCode: String) = {
    this.find(("customerCode" -> customerCode)).map(_.delete_!)
  }

}

class CustomerCode extends MongoRecord[CustomerCode] with ObjectIdPk[CustomerCode] {

  /**
   *  此資料表對應到哪 MongoMetaRecord
   */
  def meta = CustomerCode

  /**
   *  顧客代碼
   */
  val customerCode = new StringField(this, 10)

  /**
   *  顧客名稱
   */
  val customerTitle = new StringField(this, 10)
}

